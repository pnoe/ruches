package ooioo.ruches.hausse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheService;

@Controller
@RequestMapping("/hausse")
public class HausseController {

	private static final String HAUSSE_HAUSSESLISTE = "hausse/haussesListe";
	private static final String HAUSSE_HAUSSEFORM = "hausse/hausseForm";

	private final Logger logger = LoggerFactory.getLogger(HausseController.class);

	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private RucheService rucheService;

	/**
	 * Clonage multiple d'une hausse (appel XMLHttpRequest de la page détail d'une hausse).
	 * 	 
	 * @param hausseId  l'id de la hausse à cloner
	 * @param nomclones les noms des clones séparés par des virgules
	 * @return String liste des hausses créées ou erreur
	 */
	@PostMapping("/clone/{hausseId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String clone(Model model, @PathVariable long hausseId, @RequestParam String nomclones) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			List<String> noms = new ArrayList<>();
			for (Nom hausseNom : hausseRepository.findAllProjectedBy()) {
				noms.add(hausseNom.nom());
			}
			// split avec le séparateur "," et trim des chaines
			String[] nomarray = nomclones.trim().split("\\s*,\\s*");
			List<String> nomsCrees = new ArrayList<>();
			for (String nom : nomarray) {
				if ("".equals(nom)) {
					// Si le nom de hausse est vide on l'ignore et on passe à la suivante
					continue;
				}
				if (noms.contains(nom)) {
					logger.error("Clone d'une hausse : {} nom existant", nom);
				} else {
					Hausse hausseclone = new Hausse(hausse, nom);
					hausseRepository.save(hausseclone);
					nomsCrees.add(nom);
					// pour éviter clone "a,a" : 2 fois le même nom dans la liste
					noms.add(nom);
				}
			}
			String nomsJoin = String.join(",", nomsCrees);
			if (!nomsCrees.isEmpty()) {
				logger.info("Hausses(s) {} créée(s)", nomsJoin);
				return messageSource.getMessage("clonehaussecreees", new Object[] { nomsJoin },
						LocaleContextHolder.getLocale());
			} else {
				return messageSource.getMessage("PasDeHausseCree", null, LocaleContextHolder.getLocale());
			}
		}
		logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
		return messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale());
	}

	/**
	 * Liste des hausses et lien pour l'ajout de hausse.
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Hausse> hausses;
		if (voirInactif != null && (boolean) voirInactif) {
			hausses = hausseRepository.findAllByOrderByNom();
		} else {
			hausses = hausseRepository.findByActiveOrderByNom(true);
		}
		model.addAttribute(Const.HAUSSES, hausses);
		return HAUSSE_HAUSSESLISTE;
	}

	/**
	 * Appel du formulaire pour l'ajout d'une hausse.
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		List<String> noms = new ArrayList<>();
		for (Nom hausseNom : hausseRepository.findAllProjectedBy()) {
			noms.add(hausseNom.nom());
		}
		model.addAttribute(Const.HAUSSENOMS, noms);
		Hausse hausse = new Hausse();
		hausse.setDateAcquisition(Utils.dateDecal(session));
		model.addAttribute(Const.HAUSSE, hausse);
		return HAUSSE_HAUSSEFORM;
	}

	/**
	 * Appel du formulaire pour modifier une hausse.
	 */
	@GetMapping("/modifie/{hausseId}")
	public String modifie(Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute(Const.HAUSSENOMS, hausseRepository.findAllProjectedBy().stream().map(Nom::nom)
					.filter(nom -> !nom.equals(hausse.getNom())).toList());
			model.addAttribute(Const.HAUSSE, hausseOpt.get());
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return HAUSSE_HAUSSEFORM;
	}

	/**
	 * Supprimer une hausse.
	 */
	@GetMapping("/supprime/{hausseId}")
	public String supprime(Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Iterable<Evenement> evenements = evenementRepository.findByHausseId(hausseId);
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByHausseId(hausseId);
			if (recolteHausses.isEmpty()) {
				// on supprime les événements associées à cette hausse
				for (Evenement evenement : evenements) {
					evenementRepository.delete(evenement);
				}
				Hausse hausse = hausseOpt.get();
				Ruche ruche = hausse.getRuche();
				if (ruche != null) {
					hausse.setRuche(null);
					// On sauve la hausse après avoir mis hausse.ruche à null
					//  pour que ordonneHaussesRuche ne trouve plus cette hausse
					hausseRepository.save(hausse);
					rucheService.ordonneHaussesRuche(ruche.getId());
				}
				hausseRepository.delete(hausse);
				logger.info("{} supprimée", hausse);
			} else {
				model.addAttribute(Const.MESSAGE, "Cette hausse ne peut être supprimée");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/hausse/liste";
	}

	/**
	 * Enregistrement de la hausse crée ou modifiée.
	 */
	@PostMapping("/sauve")
	public String sauveHausse(Model model, @ModelAttribute Hausse hausse, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return HAUSSE_HAUSSEFORM;
		}
		
		// On enlève les blancs aux extémités du nom.
		hausse.setNom(hausse.getNom().trim());
		if ("".equals(hausse.getNom())) {
			logger.error("{} nom incorrect.", hausse);
			model.addAttribute(Const.MESSAGE, "Nom de hausse incorrect.");
			return Const.INDEX;
		}
		
		// Vérification de l'unicité du nom
		Optional <Hausse> optH = hausseRepository.findByNom(hausse.getNom());
		if (optH.isPresent()) {
			Hausse hNom = optH.get();
			if (!hNom.getId().equals(hausse.getId())) {
				logger.error("{} nom existant.", hausse);
				model.addAttribute(Const.MESSAGE, "Nom de hausse existant.");
				return Const.INDEX;
			}
		}
		String action = (hausse.getId() == null) ? "créée" : "modifiée";
		hausseRepository.save(hausse);
		logger.info("{} {}", hausse, action);
		return "redirect:/hausse/" + hausse.getId();
	}

	/**
	 * Afficher le détail d'une hausse
	 */
	@GetMapping("/{hausseId}")
	public String hausse(Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute(Const.HAUSSE, hausse);
			List<String> noms = new ArrayList<>();
			for (Nom hausseNom : hausseRepository.findAllProjectedBy()) {
				noms.add(hausseNom.nom());
			}
			model.addAttribute("haussenoms", noms);
			// Si des hausses de récolte référencent cette hausse, on ne pourra la supprimer
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByHausseId(hausseId);
			model.addAttribute("recolteHausses", recolteHausses.iterator().hasNext());
			// Si des événements référencent cette hausse, il faudra les supprimer si on
			// supprime la hausse
			Iterable<Evenement> evenements = evenementRepository.findByHausseId(hausseId);
			model.addAttribute(Const.EVENEMENTS, evenements.iterator().hasNext());
			Ruche ruche = hausse.getRuche();
			if (ruche != null) {
				model.addAttribute("eveHausse", evenementRepository
						.findFirstByRucheAndHausseAndTypeOrderByDateDesc(ruche, hausse, TypeEvenement.HAUSSEPOSERUCHE));
				model.addAttribute("eveRucher", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
						TypeEvenement.RUCHEAJOUTRUCHER));
				model.addAttribute("eveRuche", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
						TypeEvenement.AJOUTESSAIMRUCHE));
			}
			model.addAttribute("eveRempl", evenementRepository.findFirstByHausseAndTypeOrderByDateDesc(hausse,
					TypeEvenement.HAUSSEREMPLISSAGE));
			model.addAttribute("eveComm", evenementRepository.findFirstByHausseAndTypeOrderByDateDesc(hausse,
					TypeEvenement.COMMENTAIREHAUSSE));
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "hausse/hausseDetail";
	}
}
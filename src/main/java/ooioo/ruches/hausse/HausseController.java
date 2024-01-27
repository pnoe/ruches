package ooioo.ruches.hausse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheService;

@Controller
@RequestMapping("/hausse")
public class HausseController {

	private static final String HAUSSE_HAUSSESLISTE = "hausse/haussesListe";
	private static final String HAUSSE_HAUSSEFORM = "hausse/hausseForm";

	private final Logger logger = LoggerFactory.getLogger(HausseController.class);

	private final HausseRepository hausseRepository;
	private final EvenementRepository evenementRepository;
	private final RecolteHausseRepository recolteHausseRepository;
	private final MessageSource messageSource;
	private final RucheService rucheService;
	private final HausseService hausseService;

	public HausseController(HausseRepository hausseRepository, EvenementRepository evenementRepository,
			RecolteHausseRepository recolteHausseRepository, MessageSource messageSource, RucheService rucheService,
			HausseService hausseService) {
		this.hausseRepository = hausseRepository;
		this.evenementRepository = evenementRepository;
		this.recolteHausseRepository = recolteHausseRepository;
		this.messageSource = messageSource;
		this.rucheService = rucheService;
		this.hausseService = hausseService;
	}

	/**
	 * Graphique affichant la courbe du nombre de hausse posées. En abscisse le
	 * temps.
	 */
	@GetMapping("/graphehaussepose")
	public String grapheHaussePose(Model model) {
		hausseService.grapheHaussePose(model);
		return "hausse/hausseGraphePose";
	}

	/**
	 * Clonage multiple d'une hausse (appel XMLHttpRequest de la page détail d'une
	 * hausse).
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
			List<Nom> nomsRecords = hausseRepository.findAllProjectedBy();
			// split avec le séparateur "," et trim des chaines
			String[] nomarray = nomclones.trim().split("\\s*,\\s*");
			List<String> nomsCrees = new ArrayList<>();
			for (String nom : nomarray) {
				if ("".equals(nom)) {
					// Si le nom de hausse est vide on l'ignore et on passe à la suivante
					continue;
				}
				if (nomsRecords.contains(new Nom(nom))) {
					logger.error("Clone d'une hausse : {} nom existant", nom);
				} else {
					Hausse hausseclone = new Hausse(hausse, nom);
					hausseRepository.save(hausseclone);
					logger.info(Const.CREE, hausseclone);
					nomsCrees.add(nom);
					// pour éviter clone "a,a" : 2 fois le même nom dans la liste
					nomsRecords.add(new Nom(nom));
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
			Hausse hausse = hausseOpt.get();
			if (recolteHausseRepository.existsByHausse(hausse)) {
				model.addAttribute(Const.MESSAGE,
						"Cette hausse ne peut être supprimée, elle est référencée dans une récolte");
				return Const.INDEX;
			}
			if (evenementRepository.existsByHausse(hausse)) {
				model.addAttribute(Const.MESSAGE,
						"Cette hausse ne peut être supprimée, elle est référencée dans des événements");
				return Const.INDEX;
			}
			Ruche ruche = hausse.getRuche();
			if (ruche != null) {
				hausse.setRuche(null);
				// On sauve la hausse après avoir mis hausse.ruche à null
				// pour que ordonneHaussesRuche ne trouve plus cette hausse
				hausseRepository.save(hausse);
				rucheService.ordonneHaussesRuche(ruche.getId());
			}
			hausseRepository.delete(hausse);
			logger.info("{} supprimée", hausse);

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
		// On enlève les blancs aux extémités du commentaire.
		hausse.setCommentaire(hausse.getCommentaire().trim());
		// On enlève les blancs aux extémités du nom.
		hausse.setNom(hausse.getNom().trim());
		if ("".equals(hausse.getNom())) {
			logger.error("{} nom incorrect.", hausse);
			model.addAttribute(Const.MESSAGE, "Nom de hausse incorrect.");
			return Const.INDEX;
		}
		// Vérification de l'unicité du nom
		Optional<Hausse> optH = hausseRepository.findByNom(hausse.getNom());
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
	 * Afficher le détail d'une hausse.
	 */
	@GetMapping("/{hausseId}")
	public String hausse(Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute(Const.HAUSSE, hausse);
			// Liste des noms pour unicité avec la commande clone.
			List<Nom> nomsRecords = hausseRepository.findAllProjectedBy();
			List<String> noms = new ArrayList<>(nomsRecords.size());
			for (Nom hausseNom : nomsRecords) {
				noms.add(hausseNom.nom());
			}
			model.addAttribute("haussenoms", noms);
			// Si des hausses de récolte référencent cette hausse, on ne pourra la supprimer
			model.addAttribute("recolteHausses", recolteHausseRepository.existsByHausse(hausse));
			// Si des événements référencent cette hausse, on ne peut la supprimer
			model.addAttribute(Const.EVENEMENTS, evenementRepository.existsByHausse(hausse));
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
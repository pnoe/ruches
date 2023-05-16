package ooioo.ruches.ruche;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.ruche.type.RucheType;
import ooioo.ruches.ruche.type.RucheTypeRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Controller
@RequestMapping("/ruche")
public class RucheController {

	private static final String RUCHE_RUCHEFORM = "ruche/rucheForm";
	private static final String RUCHE_RUCHESLISTE = "ruche/ruchesListe";

	private final Logger logger = LoggerFactory.getLogger(RucheController.class);

	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private RucheTypeRepository rucheTypeRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;

	@Autowired
	private RucheService rucheService;
	@Autowired
	private RucherService rucherService;

	@Autowired
	private MessageSource messageSource;

	public static final String eveCree = "{} créé";

	/**
	 * Historique de l'ajout des hausses sur une ruche
	 */
	@GetMapping("/historique/{rucheId}")
	public String historique(Model model, @PathVariable long rucheId) {
		if (rucheService.historique(model, rucheId)) {
			return "ruche/rucheHisto";
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/**
	 * Clonage multiple d'une ruche (appel XMLHttpRequest de la page détail d'une
	 * ruche).
	 *
	 * @param rucheId   l'id de la ruche à cloner
	 * @param nomclones les noms des clones séparés par des virgules
	 * @return String liste des ruches créées ou erreur
	 */
	@PostMapping("/clone/{rucheId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String clone(HttpSession session, Model model, @PathVariable long rucheId,
			@RequestParam String nomclones) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			List<String> noms = new ArrayList<>();
			for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
				noms.add(rucheNom.nom());
			}
			Float longitude = ruche.getLongitude();
			// split avec le séparateur "," et trim des chaines
			String[] nomarray = nomclones.trim().split("\\s*,\\s*");
			// TODO internationalisation clone ruche
			String commentaire = "Clone ruche " + ruche.getNom();
			List<String> nomsCrees = new ArrayList<>();
			for (String nom : nomarray) {
				if ("".equals(nom)) {
					// si le nom de la ruche est vide on l'ignore et on passe à la suivante
					continue;
				}
				if (noms.contains(nom)) {
					logger.error("Clone d'une ruche : {} nom existant", nom);
				} else {
					longitude += 0.00004f;
					Ruche clone = new Ruche(ruche, nom, longitude);
					rucheRepository.save(clone);
					Evenement eveAjout = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.RUCHEAJOUTRUCHER,
							clone, null, ruche.getRucher(), null, null, commentaire);
					evenementRepository.save(eveAjout);
					logger.info(eveCree, eveAjout);
					nomsCrees.add(nom);
					// pour éviter clone "a,a" : 2 fois le même nom dans la liste
					noms.add(nom);
				}
			}
			String nomsJoin = String.join(",", nomsCrees);
			if (!nomsCrees.isEmpty()) {
				logger.info("Ruche(s) {} créée(s)", nomsJoin);
				return messageSource.getMessage("cloneruchecreees", new Object[] { nomsJoin },
						LocaleContextHolder.getLocale());
			} else {
				return messageSource.getMessage("PasDeRucheCree", null, LocaleContextHolder.getLocale());
			}
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		return messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale());
	}

	/**
	 * Liste des ruches.
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		rucheService.liste(session, model, false);
		return RUCHE_RUCHESLISTE;
	}

	/**
	 * Liste plus détaillée des ruches.
	 */
	@GetMapping("/listeplus")
	public String listePlus(HttpSession session, Model model) {
		rucheService.liste(session, model, true);
		return "ruche/ruchesListePlus";
	}

	/**
	 * Liste des ruches d'un rucher paramètres parcours : pour l'ordre des ruches.
	 * plus : liste détaillée si true
	 */
	@GetMapping("/liste/{rucherId}")
	public String listeRucher(HttpSession session, Model model, @PathVariable long rucherId, @RequestParam boolean plus,
			@RequestParam String parcours) {
		ObjectMapper objectMapper = new ObjectMapper();
		List<RucheParcours> chemin;
		try {
			chemin = objectMapper.readValue(parcours, new TypeReference<List<RucheParcours>>() {
			});
		} catch (IOException e) {
			logger.error("Paramètre parcours incorrect");
			model.addAttribute(Const.MESSAGE, "Paramètre incorect");
			return Const.INDEX;
		}
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucheService.listePlusRucher(session, model, rucher, chemin, plus);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		if (plus) {
			return "ruche/ruchesListePlusRucher";
		}
		return "ruche/ruchesListeRucher";

	}

	/**
	 * Appel du formulaire pour la création d'une ruche.
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		List<String> noms = new ArrayList<>();
		for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
			noms.add(rucheNom.nom());
		}
		model.addAttribute(Const.RUCHENOMS, noms);
		// récupération des coordonnées du dépôt
		Ruche ruche = new Ruche();
		ruche.setDateAcquisition(Utils.dateDecal(session));
		Rucher rucher = rucherRepository.findByDepotIsTrue();
		LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
		ruche.setLatitude(latLon.lat());
		ruche.setLongitude(latLon.lon());
		model.addAttribute(Const.RUCHE, ruche);
		model.addAttribute(Const.RUCHETYPES, rucheTypeRepository.findAll());
		return RUCHE_RUCHEFORM;
	}

	/**
	 * Appel du formulaire pour le changement de rucher d'une ruche.
	 */
	@GetMapping("/rucher/{rucheId}")
	public String rucher(HttpSession session, Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHE, ruche);
			model.addAttribute(Const.RUCHERS, rucherRepository.findProjectedIdNomByActifAndIdNotOrderByNom(true,
					(ruche.getRucher() == null) ? null : ruche.getRucher().getId()));
			model.addAttribute("depotId", rucherRepository.findByDepotIsTrue().getId());
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			Evenement evenFirst = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
			model.addAttribute("dateTime", evenFirst.getDate());
			LocalDateTime dateTimeFirst = evenFirst.getDate().plusMinutes(1);
			model.addAttribute("dateFirst", dateTimeFirst.format(dateFormat));
			model.addAttribute("timeFirst", dateTimeFirst.format(timeFormat));
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "/ruche/rucheRucherForm";
	}

	/**
	 * Changement de rucher d'une ruche Création de l'événement RUCHEAJOUTRUCHER.
	 */
	@PostMapping("/sauverucher/{rucheId}")
	public String sauveRucher(Model model, @RequestParam String date, @PathVariable long rucheId,
			@RequestParam String commentaire, @RequestParam Rucher rucher) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			// si la ruche n'a pas été changée de rucher on ne crée pas d'événement
			// ajouter éventuellement message "Pas de changement de rucher"
			if (!ruche.getRucher().getId().equals(rucher.getId())) {
				Long[] ruchesIds = { rucheId };
				rucherService.sauveAjouterRuches(rucher, ruchesIds, date, commentaire);
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/" + rucheId;
	}

	/**
	 * Appel du formulaire pour modifier une ruche.
	 */
	@GetMapping("/modifie/{rucheId}")
	public String modifie(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHENOMS, rucheRepository.findAllProjectedBy().stream().map(Nom::nom)
					.filter(nom -> !nom.equals(ruche.getNom())).toList());
			model.addAttribute(Const.RUCHE, ruche);
			Iterable<RucheType> rucheTypes = rucheTypeRepository.findAll();
			model.addAttribute(Const.RUCHETYPES, rucheTypes);
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return RUCHE_RUCHEFORM;
	}

	/**
	 * Suppression d'une ruche.
	 */
	@GetMapping("/supprime/{rucheId}")
	public String supprime(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Iterable<Evenement> evenements = evenementRepository.findByRucheId(rucheId);
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucheId(rucheId);
			Iterable<Hausse> hausses = hausseRepository.findCompletByRucheId(rucheId);
			if (recolteHausses.isEmpty()) {
				// on enlève les hausses de la ruche à supprimer
				for (Hausse hausse : hausses) {
					hausse.setRuche(null);
					hausse.setOrdreSurRuche(null);
				}
				// on supprime les événements associés à cette ruche
				for (Evenement evenement : evenements) {
					evenementRepository.delete(evenement);
				}
				Ruche ruche = rucheOpt.get();
				rucheRepository.delete(ruche);
				logger.info("{} supprimée", ruche);
			} else {
				model.addAttribute(Const.MESSAGE, "Cette ruche ne peut être supprimée");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/liste";
	}

	/**
	 * Enregistrement de la ruche crée ou modifiée. Si création de ruche, création
	 * de l'événement RUCHEAJOUTRUCHER dans le dépôt.
	 */
	@PostMapping("/sauve")
	public String sauve(Model model, @ModelAttribute Ruche ruche, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHE_RUCHEFORM;
		}
		// On enlève les blancs aux extémités du nom.
		ruche.setNom(ruche.getNom().trim());
		if ("".equals(ruche.getNom())) {
			logger.error("{} nom incorrect.", ruche);
			model.addAttribute(Const.MESSAGE, "Nom de ruche incorrect.");
			return Const.INDEX;
		}
		// Vérification de l'unicité du nom en modification et en création d'une ruche
		Ruche rNom = rucheRepository.findByNom(ruche.getNom());
		if (rNom != null && !rNom.getId().equals(ruche.getId())) {
			logger.error("{} nom existant.", ruche);
			model.addAttribute(Const.MESSAGE, "Nom de ruche existant.");
			return Const.INDEX;
		}
		// A sa création on met la ruche au dépôt.
		// Par contre on garde les coordonnées qui ont pu être
		// être modifiées dans le formulaire.
		String action = (ruche.getId() == null) ? "créée" : "modifiée";
		if (ruche.getRucher() == null) {
			Rucher rucher = rucherRepository.findByDepotIsTrue();
			ruche.setRucher(rucher);
			rucheRepository.save(ruche);
			// Date eve mis au dépôt = date acquisition (LocalDate) de la ruche ou
			// LocalDateTime.now()
			// (si date acquisition = LocalDate.now()).
			// La date d'acquisition est un LocalDate d'où le asStartOfDay
			// pour renseigner le heure, minutes...
			Evenement eveAjout = new Evenement(
					LocalDate.now().equals(ruche.getDateAcquisition()) ? LocalDateTime.now()
							: ruche.getDateAcquisition().atStartOfDay(),
					TypeEvenement.RUCHEAJOUTRUCHER, ruche, null, rucher, null, null, "Création de la ruche");
			evenementRepository.save(eveAjout);
			logger.info(eveCree, eveAjout);
		} else {
			rucheRepository.save(ruche);
		}
		logger.info("{} {}", ruche, action);
		return "redirect:/ruche/" + ruche.getId();
	}

	/**
	 * Afficher une ruche et ses hausses.
	 */
	@GetMapping("/{rucheId}")
	public String affiche(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.RUCHE, ruche);
			List<String> noms = new ArrayList<>();
			for (Nom rucheNom : rucheRepository.findAllProjectedBy()) {
				noms.add(rucheNom.nom());
			}
			model.addAttribute("ruchenoms", noms);
			Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
			model.addAttribute("haussesRuche", haussesRuche);
			// trouver la date de la pose des hausses sur la ruche dans les événements
			List<LocalDateTime> datesPoseHausse = new ArrayList<>();
			for (Hausse hausse : haussesRuche) {
				// trouver la date d'ajout de la hausse sur la ruche
				Evenement evenPoseHausse = evenementRepository.findFirstByRucheAndHausseAndTypeOrderByDateDesc(ruche,
						hausse, TypeEvenement.HAUSSEPOSERUCHE);
				datesPoseHausse.add((evenPoseHausse == null) ? null : evenPoseHausse.getDate());
			}
			model.addAttribute("datesPoseHausse", datesPoseHausse);
			// Si des hausses de récolte référencent cette ruche, on ne pourra la supprimer
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucheId(rucheId);
			model.addAttribute("recolteHausses", recolteHausses.iterator().hasNext());
			// Si des événements référencent cette ruche, il faudra les supprimer si on la
			// supprime
			List<Evenement> evenements = evenementRepository.findByRucheId(rucheId);
			model.addAttribute(Const.EVENEMENTS, evenements.size());
			// Trouver l'événement association essaim ruche
			if (ruche.getEssaim() != null) {
				model.addAttribute("eveEssaim", evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
						TypeEvenement.AJOUTESSAIMRUCHE));
			}
			model.addAttribute("eveRucher",
					evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche, TypeEvenement.RUCHEAJOUTRUCHER));
			model.addAttribute("eveComm",
					evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche, TypeEvenement.COMMENTAIRERUCHE));
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruche/rucheDetail";
	}

	/**
	 * Afficher une ruche pour le choix des hausses à ajouter/retirer
	 */
	@GetMapping("/hausses/{rucheId}")
	public String ajoutRetraitHausses(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			Iterable<Hausse> haussesRuche = hausseRepository.findByRucheIdOrderByOrdreSurRuche(rucheId);
			List<Hausse> haussesRucheAjout = hausseRepository.findHaussesPourAjout(rucheId);
			model.addAttribute(Const.RUCHE, ruche);
			model.addAttribute("haussesRuche", haussesRuche);
			model.addAttribute("haussesRucheAjout", haussesRucheAjout);
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "ruche/rucheAjoutRetrait";
	}

	/**
	 * Enregistrer en base l'ordre des hausses modifié par drag and drop (appel
	 * XMLHttpRequest).
	 * 
	 * @param hausses : tableau de ids des hausses dans l'ordre affiché dans la page
	 *                html
	 */
	@PostMapping("/ordreHausses/{rucheId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String ordreHausses(@PathVariable long rucheId,
			@RequestParam(value = "hausses[]") Long[] hausses) {
		for (int i = 0; i < hausses.length; i++) {
			Optional<Hausse> hausseOpt = hausseRepository.findById(hausses[i]);
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				if ((hausse.getRuche() != null) && (hausse.getRuche().getId() == rucheId)) {
					hausse.setOrdreSurRuche(i + 1);
					hausseRepository.save(hausse);
				} else {
					logger.error("Hausse {} id ruche incorrect", hausse.getNom());
					return "Id ruche incorrect";
				}
			} else {
				logger.error("Hausse {} id incorrect", hausses[i]);
				return "Hausse non trouvée";
			}
		}
		return "OK";
	}

	/**
	 * Ajoute une hausse sur une ruche. Si la hausse est déjà sur une ruche, on la
	 * retire de cette ruche.
	 */
	@PostMapping("/hausse/ajout/{rucheId}/{hausseId}")
	public String ajoutHausse(Model model, @PathVariable long rucheId, @PathVariable long hausseId,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (rucheOpt.isPresent()) {
			if (hausseOpt.isPresent()) {
				Ruche ruche = rucheOpt.get();
				Hausse hausse = hausseOpt.get();
				Evenement evenementRetrait = null;
				Ruche rucheHausse = hausse.getRuche();
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				if (rucheHausse != null) {
					// ajouter dans commentaire préfixe au commentaire d'ajout : "Pour ajout dans
					// ruche.getNom())"
					// localisation...
					evenementRetrait = new Evenement(dateEve.withSecond(0).withNano(0).minusMinutes(1L),
							TypeEvenement.HAUSSERETRAITRUCHE, rucheHausse, rucheHausse.getEssaim(),
							rucheHausse.getRucher(), hausse, hausse.getOrdreSurRuche().toString(), commentaire);
					evenementRepository.save(evenementRetrait);
					logger.info(eveCree, evenementRetrait);
					rucheService.ordonneHaussesRuche(rucheHausse.getId());
					logger.info("Hausse {} retirée de la ruche {}", hausse.getNom(), rucheHausse.getNom());
				}
				hausse.setRuche(ruche);
				// mettre ordreSurRuche au max des ordreSurRuche
				Integer nbHausses = hausseRepository.countByRucheId(rucheId);
				hausse.setOrdreSurRuche(nbHausses + 1);
				hausseRepository.save(hausse);
				Evenement evenementPose = new Evenement(dateEve, TypeEvenement.HAUSSEPOSERUCHE, ruche,
						ruche.getEssaim(), ruche.getRucher(), hausse, hausse.getOrdreSurRuche().toString(),
						commentaire);
				evenementRepository.save(evenementPose);
				logger.info(eveCree, evenementPose);
				logger.info("Hausse {} posée sur la ruche {}", hausse.getNom(), ruche.getNom());
			} else {
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
				model.addAttribute(Const.MESSAGE,
						messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/hausses/{rucheId}";
	}

	/**
	 * Retirer une hausse de sa ruche. Recalcule l'ordre des hausses de cette ruche.
	 */
	@PostMapping("/hausse/retrait/{rucheId}/{hausseId}")
	public String retraitHausse(Model model, @PathVariable long rucheId, @PathVariable long hausseId,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (rucheOpt.isPresent()) {
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				// création événement avant mise à null de la ruche
				Ruche ruche = rucheOpt.get();
				// Si la hausse n'est pas sur la ruche, abandon et message d'erreur
				if (hausse.getRuche() == null || !hausse.getRuche().getId().equals(ruche.getId())) {
					logger.error("La hausse {} n'est pas sur la ruche {}", hausse.getNom(), ruche.getNom());
					model.addAttribute(Const.MESSAGE, "Cette hausse n'est pas sur la ruche");
					// messageSource.getMessage(Const.IDHAUSSEINCONNU, null,
					// LocaleContextHolder.getLocale()));
					return Const.INDEX;
				}
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				Essaim essaim = null;
				Rucher rucher = null;
				String rucheNom = "";
				essaim = ruche.getEssaim();
				rucher = ruche.getRucher();
				rucheNom = ruche.getNom();
				Evenement evenementRetrait = new Evenement(dateEve, TypeEvenement.HAUSSERETRAITRUCHE, ruche, essaim,
						rucher, hausse, hausse.getOrdreSurRuche().toString(), commentaire);
				evenementRepository.save(evenementRetrait);
				logger.info(eveCree, evenementRetrait);
				hausse.setRuche(null);
				hausse.setOrdreSurRuche(null);
				hausseRepository.save(hausse);
				rucheService.ordonneHaussesRuche(rucheId);
				logger.info("Hausse {} retirée de la ruche {}", hausse.getNom(), rucheNom);
			} else {
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
				model.addAttribute(Const.MESSAGE,
						messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/ruche/hausses/{rucheId}";
	}

	/**
	 * Déplacer une ruche (appel XMLHttpRequest)
	 */
	@PostMapping("/deplace/{rucheId}/{lat}/{lng}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String deplace(@PathVariable Long rucheId, @PathVariable Float lat, @PathVariable Float lng) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			ruche.setLatitude(lat);
			ruche.setLongitude(lng);
			rucheRepository.save(ruche);
			return "OK";
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		return "Id ruche inconnu";
	}

}
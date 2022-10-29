package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.hausse.HausseService;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@Controller
@RequestMapping("/evenement/hausse")
public class EvenementHausseController {

	final Logger logger = LoggerFactory.getLogger(EvenementHausseController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private HausseService hausseService;

	@Autowired
	MessageSource messageSource;

	/*
	 * Liste événements remplissage hausse
	 */
	@GetMapping("/listeRemplissageHausse")
	public String listeRemplissageHausse(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext,
			@CookieValue(value = "p", defaultValue = "1") Integer pCookie,
			@CookieValue(value = "dx", defaultValue = "") String dxCookie,
			@CookieValue(value = "d1", defaultValue = "")  @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime d1Cookie,
			@CookieValue(value = "d2", defaultValue = "")  @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime d2Cookie
			) {
		if (periode == null) {
			periode = pCookie;
			if (pCookie == 6) {
				date1 = d1Cookie;
				date2 = d2Cookie;
				datestext = dxCookie;
			}
		}
		Iterable<Evenement> evenements;
		switch (periode) {
		case 1: // toute période
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.HAUSSEREMPLISSAGE);
			break;
		case 2: // moins d'un an
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		model.addAttribute(Const.EVENEMENTS, evenements);
		model.addAttribute("periode", periode);
		return "evenement/evenementRemplissageHausseListe";
	}

	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIREHAUSSE
	 */
	@GetMapping("/commentaire/cree/{hausseId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			Ruche ruche = hausse.getRuche();
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			Essaim essaim = null;
			if (ruche != null) {
				essaim = ruche.getEssaim();
			}
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.COMMENTAIREHAUSSE, ruche, essaim,
					rucher, hausse, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			return "hausse/hausseCommentaireForm";
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement COMMENTAIREHAUSSE
	 */
	@GetMapping("/commentaire/modifie/{evenementId}")
	public String commentaireModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "hausse/hausseCommentaireForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire pour créer des commentaires pour un lot de hausses
	 */
	@GetMapping("/commentaireLot/{haussesNoms}")
	public String commentaireLot(HttpSession session, Model model, @PathVariable String haussesNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute("haussesNoms", haussesNoms);
		return "hausse/hausseCommentaireLotForm";
	}

	/**
	 * Créations des événements pour un lot de hausses
	 */
	@PostMapping("/sauve/lot/{haussesNoms}")
	public String sauveLot(@PathVariable String[] haussesNoms, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String date, @RequestParam String valeur, @RequestParam String commentaire) {
		for (String hausseNoms : haussesNoms) {
			Optional<Hausse> hausseOpt = hausseRepository.findByNom(hausseNoms);
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				Ruche ruche = hausse.getRuche();
				Essaim essaim = null;
				Rucher rucher = null;
				if (ruche != null) {
					essaim = ruche.getEssaim();
					rucher = ruche.getRucher();
				}
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, essaim, rucher,
						hausse, valeur, commentaire);
				evenementRepository.save(evenement);
				logger.info("{} créé", evenement);
			} else {
				logger.error("Nom hausse {} inconnu", hausseNoms);
			}
		}
		return "redirect:/hausse/liste";
	}

	/**
	 * Appel du formulaire pour la création d'un événement HAUSSEREMPLISSAGE
	 */
	@GetMapping("/remplissage/cree/{hausseId}")
	public String creeRemplissage(HttpSession session, Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			Ruche ruche = hausse.getRuche();
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			Essaim essaim = null;
			if (ruche != null) {
				essaim = ruche.getEssaim();
			}
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.HAUSSEREMPLISSAGE, ruche, essaim,
					rucher, hausse, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			// pour affichage rappel du dernier événement de remplissage
			hausseService.modelAddEvenement(model, hausse, TypeEvenement.HAUSSEREMPLISSAGE);
			return "hausse/hausseRemplissageForm";
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement HAUSSEREMPLISSAGE
	 */
	@GetMapping("/remplissage/modifie/{evenementId}")
	public String remplissageModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "hausse/hausseRemplissageForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Préparation appel formulaire événement hausse
	 */
	public String prepareAppelFormulaire(HttpSession session, Model model, long hausseId, String template) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.HAUSSE, hausse);
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return template;
	}

	/**
	 * Sauvegarde d'un événement commentaire hausse. Récupère tous les champs de
	 * l'événement du formulaire
	 */
	@PostMapping("/commentaire/sauve")
	public String commentaireSauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "hausse/hausseCommentaireForm";
		}
		evenement.setValeur(Utils.notifIntFmt(evenement.getValeur()));
		String action = (evenement.getId() == null)?"créé":"modifié"; 
		evenementRepository.save(evenement);
		logger.info("{} " + action, evenement);
		return "redirect:/hausse/" + evenement.getHausse().getId();
	}

	/**
	 * Sauvegarde d'un événement hausse.
	 * Récupère tous les champs de l'événement du formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		// le template pour return doit être passé en paramètre si on veut
		// utiliser sauve pour tous les even essaim (sucre, varoa...)
		/*
		 * if (bindingResult.hasErrors()) { return "essaim/essaimSucreForm"; }
		 */
		String action = (evenement.getId() == null)?"créé":"modifié"; 
		evenementRepository.save(evenement);
		logger.info("{} " + action, evenement);
		return "redirect:/hausse/" + evenement.getHausse().getId();
	}

	/**
	 * Liste des événements d'une hausse
	 */
	@GetMapping("/{hausseId}")
	public String liste(Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByHausseId(hausseId));
			model.addAttribute("type", "hausse");
			model.addAttribute("hausseNom", hausseOpt.get().getNom());
			return Const.EVEN_EVENLISTE;
		}
		logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

}
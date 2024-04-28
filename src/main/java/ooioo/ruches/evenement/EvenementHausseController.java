package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@Controller
@RequestMapping("/evenement/hausse")
public class EvenementHausseController {

	final Logger logger = LoggerFactory.getLogger(EvenementHausseController.class);

	private final EvenementRepository evenementRepository;
	private final HausseRepository hausseRepository;
	private final MessageSource messageSource;

	public EvenementHausseController(EvenementRepository evenementRepository, HausseRepository hausseRepository,
			MessageSource messageSource) {
		this.evenementRepository = evenementRepository;
		this.hausseRepository = hausseRepository;
		this.messageSource = messageSource;
	}

	private static final String commForm = "hausse/hausseCommentaireForm";

	/*
	 * Liste des événements pose et retrait de hausse.
	 */
	@GetMapping("/poseRetraitHausse")
	public String poseRetraitHausseListe(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext,
			@CookieValue(value = "p", defaultValue = "1") Integer pCookie,
			@CookieValue(value = "dx", defaultValue = "") String dxCookie,
			@CookieValue(value = "d1", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d1Cookie,
			@CookieValue(value = "d2", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d2Cookie) {
		if (periode == null) {
			periode = pCookie;
			if (pCookie == 6) {
				date1 = d1Cookie;
				date2 = d2Cookie;
				datestext = dxCookie;
			}
		}
		model.addAttribute(Const.EVENEMENTS, switch (periode) {
		case 1 -> // toute période
			evenementRepository.findPoseHausseDateDesc();
		case 2 -> // moins d'un an
			evenementRepository.findTypePeriode(LocalDateTime.now().minusYears(1), TypeEvenement.HAUSSEPOSERUCHE,
					TypeEvenement.HAUSSERETRAITRUCHE);
		case 3 -> // moins d'un mois
			evenementRepository.findTypePeriode(LocalDateTime.now().minusMonths(1), TypeEvenement.HAUSSEPOSERUCHE,
					TypeEvenement.HAUSSERETRAITRUCHE);
		case 4 -> // moins d'une semaine
			evenementRepository.findTypePeriode(LocalDateTime.now().minusWeeks(1), TypeEvenement.HAUSSEPOSERUCHE,
					TypeEvenement.HAUSSERETRAITRUCHE);
		case 5 -> // moins d'un jour
			evenementRepository.findTypePeriode(LocalDateTime.now().minusDays(1), TypeEvenement.HAUSSEPOSERUCHE,
					TypeEvenement.HAUSSERETRAITRUCHE);
		default -> { // ajouter tests date1 et date2 non null
			model.addAttribute("datestext", datestext);
			yield evenementRepository.findTypePeriode(date1, date2, TypeEvenement.HAUSSEPOSERUCHE,
					TypeEvenement.HAUSSERETRAITRUCHE);
		}
		});
		model.addAttribute("periode", periode);
		return "evenement/evenementPoseHausseListe";
	}

	/*
	 * Liste événements remplissage hausse
	 */
	@GetMapping("/listeRemplissageHausse")
	public String listeRemplissageHausse(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext,
			@CookieValue(value = "p", defaultValue = "1") Integer pCookie,
			@CookieValue(value = "dx", defaultValue = "") String dxCookie,
			@CookieValue(value = "d1", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d1Cookie,
			@CookieValue(value = "d2", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d2Cookie) {
		if (periode == null) {
			periode = pCookie;
			if (pCookie == 6) {
				date1 = d1Cookie;
				date2 = d2Cookie;
				datestext = dxCookie;
			}
		}
		model.addAttribute(Const.EVENEMENTS, switch (periode) {
		case 1 -> // toute période
			evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.HAUSSEREMPLISSAGE);
		case 2 -> // moins d'un an
			evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusYears(1));
		case 3 -> // moins d'un mois
			evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusMonths(1));
		case 4 -> // moins d'une semaine
			evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusWeeks(1));
		case 5 -> // moins d'un jour
			evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, LocalDateTime.now().minusDays(1));
		default -> { // ajouter tests date1 et date2 non null
			model.addAttribute("datestext", datestext);
			yield evenementRepository.findTypePeriode(TypeEvenement.HAUSSEREMPLISSAGE, date1, date2);
		}
		});
		model.addAttribute("periode", periode);
		return "evenement/evenementRemplissageHausseListe";
	}

	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIREHAUSSE.
	 */
	@GetMapping("/commentaire/cree/{hausseId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			Ruche ruche = hausse.getRuche();
			Rucher rucher = null;
			Essaim essaim = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
				essaim = ruche.getEssaim();
			}
			model.addAttribute(Const.EVENEMENT, new Evenement(Utils.dateTimeDecal(session),
					TypeEvenement.COMMENTAIREHAUSSE, ruche, essaim, rucher, hausse, null, null));
			return commForm;
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
			return commForm;
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire pour créer des commentaires pour un lot de hausses
	 */
	@GetMapping("/commentaireLot/{hausseIds}")
	public String commentaireLot(HttpSession session, Model model, @PathVariable Long[] hausseIds) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		StringBuilder haussesNoms = new StringBuilder();
		StringBuilder hIds = new StringBuilder();
		for (Long hausseId : hausseIds) {
			Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
			if (hausseOpt.isPresent()) {
				Hausse hausse = hausseOpt.get();
				haussesNoms.append(hausse.getNom() + ", ");
				hIds.append(hausse.getId() + ",");
			} else {
				// on continue le traitement des autres hausses
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			}
		}
		if (haussesNoms.length() > 2) {
			haussesNoms.delete(haussesNoms.length() - 2, haussesNoms.length());
			hIds.deleteCharAt(hIds.length() - 1);
		}
		model.addAttribute("haussesNoms", haussesNoms);
		model.addAttribute("hIds", hIds);
		return "hausse/hausseCommentaireLotForm";
	}

	/**
	 * Créations des événements pour un lot de hausses
	 */
	@PostMapping("/sauve/lot/{hausseIds}")
	public String sauveLot(@PathVariable Long[] hausseIds, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String date, @RequestParam String valeur, @RequestParam String commentaire) {
		for (Long hausseId : hausseIds) {
			Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
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
				Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, essaim, rucher, hausse, valeur,
						commentaire);
				evenementRepository.save(evenement);
				logger.info("{} créé", evenement);
			} else {
				logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
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
			Evenement eveRemp = evenementRepository.findFirstByHausseAndTypeOrderByDateDesc(hausse,
					TypeEvenement.HAUSSEREMPLISSAGE);
			String type = TypeEvenement.HAUSSEREMPLISSAGE.toString();
			if (eveRemp == null) {
				model.addAttribute(Const.DATE + type, null);
				model.addAttribute(Const.VALEUR + type, null);
				model.addAttribute(Const.COMMENTAIRE + type, null);
			} else {
				model.addAttribute(Const.DATE + type, eveRemp.getDate());
				model.addAttribute(Const.VALEUR + type, eveRemp.getValeur());
				model.addAttribute(Const.COMMENTAIRE + type, eveRemp.getCommentaire());
			}
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
			return commForm;
		}
		evenement.setValeur(Utils.notifIntFmt(evenement.getValeur()));
		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} {}", evenement, action);
		return "redirect:/hausse/" + evenement.getHausse().getId();
	}

	/**
	 * Sauvegarde d'un événement hausse. Récupère tous les champs de l'événement du
	 * formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		// le template pour return doit être passé en paramètre si on veut
		// utiliser sauve pour tous les even essaim (sucre, varoa...)
		/*
		 * if (bindingResult.hasErrors()) { return "essaim/essaimSucreForm"; }
		 */
		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} {}", evenement, action);
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
package ooioo.ruches.evenement;

import java.math.BigDecimal;
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
import ooioo.ruches.essaim.EssaimService;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;

@Controller
@RequestMapping("/evenement/ruche")
public class EvenementRucheController {

	final Logger logger = LoggerFactory.getLogger(EvenementRucheController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	MessageSource messageSource;
	@Autowired
	private EssaimService essaimService;

	/*
	 * Liste événements poids ruche
	 */
	@GetMapping("/listePoidsRuche")
	public String listePoidsRuche(Model model, @RequestParam(required = false) Integer periode,
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
		Iterable<Evenement> evenements;
		switch (periode) {
		case 1: // toute période
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEPESEE);
			break;
		case 2: // moins d'un an
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE,
					LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE,
					LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE,
					LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE,
					LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		List<BigDecimal> diff = new ArrayList<>();
		for (Evenement evenement : evenements) {
			try {
				BigDecimal d = new BigDecimal(evenement.getValeur()).subtract(evenement.getRuche().getPoidsVide());
				diff.add(d);
			} catch (NumberFormatException e) {
				diff.add(BigDecimal.ZERO);
			}
		}
		model.addAttribute(Const.EVENEMENTS, evenements);
		model.addAttribute("diff", diff);
		model.addAttribute("periode", periode);
		return "evenement/evenementPoidsRucheListe";
	}

	/*
	 * Liste événements cadre ruche
	 */
	@GetMapping("/listeCadreRuche")
	public String listeCadreRuche(Model model, @RequestParam(required = false) Integer periode,
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
		Iterable<Evenement> evenements;
		switch (periode) {
		case 1: // toute période
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHECADRE);
			break;
		case 2: // moins d'un an
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE,
					LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE,
					LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE,
					LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE,
					LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements = evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		model.addAttribute(Const.EVENEMENTS, evenements);
		model.addAttribute("periode", periode);
		return "evenement/evenementCadreRucheListe";
	}

	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIRERUCHE
	 */
	@GetMapping("/commentaire/cree/{rucheId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			Essaim essaim = ruche.getEssaim();
			Rucher rucher = ruche.getRucher();
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.COMMENTAIRERUCHE, ruche, essaim,
					rucher, null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			return "ruche/rucheCommentaireForm";
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement ruche commentaire
	 */
	@GetMapping("/commentaire/modifie/{evenementId}")
	public String commentaireModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "ruche/rucheCommentaireForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire pour créer des commentaires pour un lot de ruches
	 */
	@GetMapping("/commentaireLot/{rucheIds}")
	public String commentaireLot(HttpSession session, Model model, @PathVariable Long[] rucheIds) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		// on reconstitue les liste de noms et d'ids de ruche séparés par des virgules
		StringBuilder ruchesNoms = new StringBuilder();
		StringBuilder rIds = new StringBuilder();
		for (Long rucheId : rucheIds) {
			Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
			if (rucheOpt.isPresent()) {
				Ruche ruche = rucheOpt.get();
				ruchesNoms.append(ruche.getNom() + ",");
				rIds.append(ruche.getId() + ",");
			} else {
				// on continue le traitement des autres ruches
				logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			}
		}
		ruchesNoms.deleteCharAt(ruchesNoms.length() - 1);
		rIds.deleteCharAt(rIds.length() - 1);
		model.addAttribute("ruchesNoms", ruchesNoms);
		model.addAttribute("rIds", rIds);
		return "ruche/rucheCommentaireLotForm";
	}

	/**
	 * Créations des événements pour un lot de ruches
	 */
	@PostMapping("/sauve/lot/{rucheIds}")
	public String sauveLot(@PathVariable Long[] rucheIds, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String date, @RequestParam String valeur, @RequestParam String commentaire) {
		for (Long rucheId : rucheIds) {
			Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
			if (rucheOpt.isPresent()) {
				Ruche ruche = rucheOpt.get();
				LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
				Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, ruche.getEssaim(), ruche.getRucher(),
						null, valeur, commentaire);
				evenementRepository.save(evenement);
				logger.info("{} créé", evenement);
			} else {
				// on continue le traitement des autres ruches
				logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			}
		}
		return "redirect:/ruche/liste";
	}

	/**
	 * Appel du formulaire pour la création d'un événement RUCHECADRE
	 */
	@GetMapping("/cadre/cree/{rucheId}")
	public String cadreCree(HttpSession session, Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			Essaim essaim = ruche.getEssaim();
			Rucher rucher = ruche.getRucher();
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.RUCHECADRE, ruche, essaim, rucher,
					null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);

			// essaimService.modelAddEvenement(model, essaim, TypeEvenement.RUCHECADRE);
			essaimService.modelAddEve(model, essaim, TypeEvenement.RUCHECADRE);

			return "ruche/rucheCadreForm";
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement RUCHECADRE
	 */
	@GetMapping("/cadre/modifie/{evenementId}")
	public String cadreModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "ruche/rucheCadreForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire pour la création d'un événement RUCHEPESEE
	 */
	@GetMapping("/pesee/cree/{rucheId}")
	public String peseeCree(HttpSession session, Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			Essaim essaim = ruche.getEssaim();
			Rucher rucher = ruche.getRucher();
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.RUCHEPESEE, ruche, essaim, rucher,
					null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			// rucheService.modelAddEvenement(model, ruche, TypeEvenement.RUCHEPESEE);
			// essaimService.modelAddEvenement(model, essaim, TypeEvenement.RUCHEPESEE);

			essaimService.modelAddEve(model, essaim, TypeEvenement.RUCHEPESEE);

			return "ruche/ruchePeseeForm";
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement RUCHEPESEE
	 */
	@GetMapping("/pesee/modifie/{evenementId}")
	public String peseeModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "ruche/ruchePeseeForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/*
	 * Appel du formulaire événement pour ajouter une hausse
	 */
	@GetMapping("/hausse/ajout/{rucheId}/{hausseId}")
	public String ajoutHausse(HttpSession session, Model model, @PathVariable long rucheId,
			@PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute("hausse", hausse);
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return prepareAppelFormulaire(session, model, rucheId, "ruche/rucheHausseForm");
	}

	/*
	 * Appel du formulaire événement pour retirer une hausse
	 */
	@GetMapping("/hausse/retrait/{rucheId}/{hausseId}")
	public String retraitHausse(HttpSession session, Model model, @PathVariable long rucheId,
			@PathVariable long hausseId) {
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			Hausse hausse = hausseOpt.get();
			model.addAttribute("hausse", hausse);
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return prepareAppelFormulaire(session, model, rucheId, "ruche/rucheRetraitHausseForm");
	}

	/**
	 * Préparation de l'appel du formulaire pour un événement ruche.
	 */
	public String prepareAppelFormulaire(HttpSession session, Model model, long rucheId, String template) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.RUCHE, ruche);
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return template;
	}

	/**
	 * Sauvegarde d'un événement commentaire ruche. Récupère tous les champs de
	 * l'événement du formulaire.
	 */
	@PostMapping("/commentaire/sauve")
	public String commentaireSauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "ruche/rucheCommentaireForm";
		}
		evenement.setValeur(Utils.notifIntFmt(evenement.getValeur()));
		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} " + action, evenement);
		return "redirect:/ruche/" + evenement.getRuche().getId();
	}

	/**
	 * Sauvegarde d'un événement ruche. Récupère tous les champs de l'événement du
	 * formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} " + action, evenement);
		return "redirect:/ruche/" + evenement.getRuche().getId();
	}

	/**
	 * Liste des événements d'une ruche
	 */
	@GetMapping("/{rucheId}")
	public String liste(Model model, @PathVariable long rucheId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByRucheId(rucheId));
			// rucheId @PathVariable est connu du template : ${rucheId}
			// mais type et itemId simplifient le template pour le retour du lien détail
			// d'un événement
			model.addAttribute("type", Const.RUCHE);
			// pour lien retour dans la liste vers détail ruche
			// evenements[0].getRuche().getNom() ne fonctionne pas dasns le template
			// s'il n'y a pas d'événement ruche !
			model.addAttribute("rucheNom", rucheOpt.get().getNom());
			return Const.EVEN_EVENLISTE;
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

}
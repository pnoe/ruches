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
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/evenement/rucher")
public class EvenementRucherController {

	private final Logger logger = LoggerFactory.getLogger(EvenementRucherController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	MessageSource messageSource;
	
	/*
	 * Liste événements ajout ruche rucher
	 */
	@GetMapping("/listeRucheAjout")
	public String listeRucheAjout(Model model, @RequestParam(required = false) Integer periode,
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
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEAJOUTRUCHER);
			break;
		case 2: // moins d'un an
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements =
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		model.addAttribute(Const.EVENEMENTS, evenements);
		model.addAttribute("periode", periode);
		return "evenement/evenementRucheAjoutListe";
	}

	/**
	 * Appel du formulaire pour un événement commentaire rucher
	 */
	/*
	@GetMapping("/commentaire/{rucherId}")
	public String creeEvenementCommentaireRucher(HttpSession session, Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.RUCHER, rucher);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherCommentaireForm";
	}
	*/
	
	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIRERUCHER
	 */
	@GetMapping("/commentaire/cree/{rucherId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.COMMENTAIRERUCHER, null, null,
					rucher, null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			return "rucher/rucherCommentaireForm";
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}
	
	/**
	 * Appel du formulaire de modification d'un événement rucher commentaire
	 */
	@GetMapping("/commentaire/modifie/{evenementId}")
	public String commentaireModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "rucher/rucherCommentaireForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Sauvegarde d'un événement ruche.
	 * Récupère tous les champs de l'événement du formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		evenementRepository.save(evenement);
		logger.info("Evénement {} enregistré, id {}", evenement.getDate(), evenement.getId());
		logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
		// return Const.REDIRECT_ESSAIM_ESSAIMID;
		return "redirect:/rucher/" + evenement.getRucher().getId();
	}

	/**
	 * Liste des événements d'un rucher
	 */
	@GetMapping("/{rucherId}")
	public String listeEvenementRucher(Model model, @PathVariable long rucherId) {
		model.addAttribute(Const.EVENEMENTS, evenementRepository.findByRucherId(rucherId));
		model.addAttribute("type", "rucher");
		// pour lien retour dans la liste vers détail rucher
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			return Const.EVEN_EVENLISTE;
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

}
package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.Optional;

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

import jakarta.servlet.http.HttpSession;
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
	private MessageSource messageSource;

	private static final String commForm = "rucher/rucherCommentaireForm";

	/*
	 * Liste événements ajout ruche rucher
	 */
	@GetMapping("/listeRucheAjout")
	public String listeRucheAjout(Model model, @RequestParam(required = false) Integer periode,
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
			evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEAJOUTRUCHER);
		case 2 -> // moins d'un an
			evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusYears(1));
		case 3 -> // moins d'un mois
			evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusMonths(1));
		case 4 -> // moins d'une semaine
			evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusWeeks(1));
		case 5 -> // moins d'un jour
			evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, LocalDateTime.now().minusDays(1));
		default -> {
			// ajouter tests date1 et date2 non null
			model.addAttribute("datestext", datestext);
			yield evenementRepository.findTypePeriode(TypeEvenement.RUCHEAJOUTRUCHER, date1, date2);
		}
		});
		model.addAttribute("periode", periode);
		return "evenement/evenementRucheAjoutListe";
	}

	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIRERUCHER.
	 */
	@GetMapping("/commentaire/cree/{rucherId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENT, new Evenement(Utils.dateTimeDecal(session),
					TypeEvenement.COMMENTAIRERUCHER, null, null, rucherOpt.get(), null, null, null));
			return commForm;
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement COMMENTAIRERUCHER.
	 */
	@GetMapping("/commentaire/modifie/{evenementId}")
	public String commentaireModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENT, evenementOpt.get());
			return commForm;
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Sauvegarde d'un événement commentaire rucher. Récupère tous les champs de
	 * l'événement du formulaire.
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
		return "redirect:/rucher/" + evenement.getRucher().getId();
	}

	/**
	 * Sauvegarde d'un événement ruche. Récupère tous les champs de l'événement du
	 * formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} {}", evenement, action);
		return "redirect:/rucher/" + evenement.getRucher().getId();
	}

	/**
	 * Liste des événements d'un rucher.
	 */
	@GetMapping("/{rucherId}")
	public String listeEvenementRucher(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByRucherId(rucherId));
			model.addAttribute("type", "rucher");
			// pour lien retour dans la liste vers détail rucher
			model.addAttribute("rucherNom", rucherOpt.get().getNom());
			return Const.EVEN_EVENLISTE;
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

}
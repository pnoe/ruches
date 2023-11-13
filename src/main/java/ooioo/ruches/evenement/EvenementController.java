package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/evenement")
public class EvenementController {

	private static final String EVEN_EVENFORM = "evenement/evenementForm";
	private static final String REDIRECT_EVEN_LISTE = "redirect:/evenement/liste";
	private static final String REDIRECT_EVEN = "redirect:/evenement/";

	final Logger logger = LoggerFactory.getLogger(EvenementController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private RucherRepository rucherRepository;

	@Autowired
	private MessageSource messageSource;

	@Value("${notification.destinataires}")
	private String[] notifDest;

	/**
	 * Liste événements par période de temps (par défaut 1 mois) la période est
	 * mémorisée dans des cookies. Les listes spécifiques : sucre, varoa... sont
	 * dans les autres controller événements et utilisent des templates de listes
	 * spécifiques
	 *
	 * @param periode   1 tous, moins d'un : 2 an, 3 mois, 4 semaine, 5 jour,
	 *                  default période entre date1 et date2
	 * @param date1     début de période (si periode != 1, 2, 3, 4 ou 5)
	 * @param date2     fin de période
	 * @param datestext le texte des dates de début et fin de période à afficher
	 * @param pCookie   période
	 * @param dxCookie  le texte des dates
	 * @param d1Cookie  date début
	 * @param d2Cookie  date fin
	 * @return
	 */
	@GetMapping("/liste")
	public String liste(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext,
			@CookieValue(value = "p", defaultValue = "3") Integer pCookie,
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
		// https://docs.oracle.com/en/java/javase/17/language/switch-expressions.html#GUID-BA4F63E3-4823-43C6-A5F3-BAA4A2EF3ADC
		// https://openjdk.org/jeps/361
		model.addAttribute(Const.EVENEMENTS, switch (periode) {
		case 1 -> // toute période
			evenementRepository.findAll();
		case 2 -> // moins d'un an
			evenementRepository.findPeriode(LocalDateTime.now().minusYears(1));
		case 3 -> // moins d'un mois
			evenementRepository.findPeriode(LocalDateTime.now().minusMonths(1));
		case 4 -> // moins d'une semaine
			evenementRepository.findPeriode(LocalDateTime.now().minusWeeks(1));
		case 5 -> // moins d'un jour
			evenementRepository.findPeriode(LocalDateTime.now().minusDays(1));
		default -> {
			model.addAttribute("datestext", datestext);
			yield (evenementRepository.findPeriode(date1, date2));
		}
		});
		model.addAttribute("periode", periode);
		return Const.EVEN_EVENLISTE;
	}

	/**
	 * Liste événements notifications
	 */
	@GetMapping("/listeNotif/{tous}")
	public String listenotif(Model model, @PathVariable boolean tous) {
		LocalDateTime dateNow = LocalDateTime.now();
		List<Evenement> evenements = evenementRepository.findNotification();
		List<Integer> jAvants = new ArrayList<>();
		List<Evenement> evens = new ArrayList<>();
		for (Evenement evenement : evenements) {
			int joursAvant = Integer.parseInt(evenement.getValeur());
			LocalDateTime min;
			LocalDateTime max;
			if (joursAvant >= 0) {
				min = evenement.getDate().minusDays(joursAvant);
				max = evenement.getDate();
			} else {
				// si joursAvant est négatif la plage de notification
				// est la date de l'évenement - (la date eve plus le nb de jours)
				// fonctionnalité : notif apès date de l'événement
				max = evenement.getDate().minusDays(joursAvant);
				min = evenement.getDate();
			}
			// Si on est dans la plage de notification
			if (tous || (dateNow.isAfter(min) && (dateNow.isBefore(max)))) {
				evens.add(evenement);
				jAvants.add(joursAvant);
			}
		}
		model.addAttribute(Const.EVENEMENTS, evens);
		model.addAttribute("jAvants", jAvants);
		model.addAttribute("dests", messageSource.getMessage("Destinataires", null, LocaleContextHolder.getLocale())
				+ " :<br/>" + String.join("<br/>", notifDest));
		return "evenement/evenementNotifListe";
	}

	/*
	 * Détail d'un événement d'id evenementId
	 * 
	 * @param type : "" pour retour liste tous even, essaim/ruche/rucher/hausse pour
	 * retour liste spécifique à l'objet dont l'id est dans itemId,
	 * sucre/traitement/... pour retour liste even/sucre...
	 * 
	 * @param itemId l'id de l'objet de retour
	 */
	@GetMapping("/{evenementId}")
	public String evenement(Model model, @PathVariable long evenementId,
			@RequestParam(defaultValue = "") @Nullable String type,
			@RequestParam(defaultValue = "0") @Nullable Long itemId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			model.addAttribute("type", type);
			// récupérer le type de l'événement
			// pour return vers template spécifique sucre, commentaire...
			// les modelAttribute servent au retour vers les listes par type
			// du menu Evenement
			switch (evenement.getType()) {
			case HAUSSEREMPLISSAGE:
				return "evenement/evenementRemplissageDetail";
			case ESSAIMTRAITEMENT, ESSAIMTRAITEMENTFIN:
				return "evenement/evenementTraitementDetail";
			case ESSAIMSUCRE:
				return "evenement/evenementSucreDetail";
			case COMMENTAIRERUCHE:
				return "evenement/evenementCommRucheDetail";
			case COMMENTAIREHAUSSE:
				return "evenement/evenementCommHausseDetail";
			case COMMENTAIREESSAIM:
				return "evenement/evenementCommEssaimDetail";
			case COMMENTAIRERUCHER:
				return "evenement/evenementCommRucherDetail";
			case RUCHEPESEE:
				return "evenement/evenementPeseeDetail";
			case RUCHECADRE:
				return "evenement/evenementCadreDetail";
			default:
				// pour faciliter l'ajout d'un type non traité spécifiquement
				return "evenement/evenementDetail";
			}
		}
		logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
		model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
		return Const.INDEX;
	}

	/*
	 * Suppression d'un événement
	 */
	@GetMapping("/supprime/{evenementId}")
	public String supprime(@PathVariable long evenementId, Model model) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			evenementRepository.delete(evenement);
			logger.info("{} supprimé", evenement);
			return REDIRECT_EVEN_LISTE;
		}
		logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
		model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
		return Const.INDEX;
	}

	/*
	 * Appel du formulaire de création d'un événement appel du formulaire complet
	 * avec tous les champs. Une case à cocher permet de choisir ruche, rucher,
	 * hausse et essaim librement.
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		model.addAttribute(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
		model.addAttribute(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
		model.addAttribute(Const.HAUSSES, hausseRepository.findAllProjectedIdNomByOrderByNom());
		model.addAttribute(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
		model.addAttribute("complet", "true");
		Evenement evenement = new Evenement();
		evenement.setDate(Utils.dateTimeDecal(session));
		model.addAttribute(Const.EVENEMENT, evenement);
		return EVEN_EVENFORM;
	}

	/*
	 * Appel du formulaire de modification d'un événement
	 */
	@GetMapping("/modifie/{evenementId}")
	public String modifie(Model model, HttpServletRequest request, @PathVariable long evenementId,
			@RequestParam(defaultValue = "false") @Nullable String complet) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			model.addAttribute(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.HAUSSES, hausseRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute("complet", complet);
			model.addAttribute(Const.EVENEMENT, evenementOpt.get());
			return EVEN_EVENFORM;
		}
		logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
		model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
		return Const.INDEX;
	}

	/*
	 * Enregistrement de l'événement
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return EVEN_EVENFORM;
		}
		// On enlève les blancs aux extémités du commentaire.
		evenement.setCommentaire(evenement.getCommentaire().trim());

		String action = (evenement.getId() == null) ? "créé" : "modifié";
		evenementRepository.save(evenement);
		logger.info("{} {}", evenement, action);
		return REDIRECT_EVEN + evenement.getId();
	}

}
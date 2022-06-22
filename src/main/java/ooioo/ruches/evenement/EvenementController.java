package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ooioo.ruches.Const;
import ooioo.ruches.Notification;
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

	/**
	 * Liste événements par période de temps (par défaut 1 mois) la période est
	 * mémorisée dans des cookies les listes spécifiques : sucre, varoa... sont dans
	 * les autres controller événements
	 * 
	 * @param periode   1 tous, moins d'un : 2 an, 3 mois, 4 semaine, 5 jour,
	 *                  default période entre debut et fin
	 * @param debut     début de période (si periode != 1, 2, 3, 4 ou 5)
	 * @param fin       fin de période
	 * @param datestext le texte des dates de début et fin de période à afficher
	 * @param pCookie   période
	 * @param dxCookie  le texte des dates
	 * @param d1Cookie  date début
	 * @param d2Cookie  date fin
	 * @return
	 */
	@GetMapping("/liste")
	public String liste(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
			@RequestParam(required = false) String datestext,
			@CookieValue(value = "p", defaultValue = "3") Integer pCookie,
			@CookieValue(value = "dx", defaultValue = "") String dxCookie,
			@CookieValue(value = "d1", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d1Cookie,
			@CookieValue(value = "d2", defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime d2Cookie) {
		if (periode == null) {
			periode = pCookie;
			if (pCookie == 6) {
				debut = d1Cookie;
				fin = d2Cookie;
				datestext = dxCookie;
			}
		}
		switch (periode) {
		case 1: // toute période
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findAll());
			break;
		case 2: // moins d'un an
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(LocalDateTime.now().minusYears(1)));
			break;
		case 3: // moins d'un mois
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(LocalDateTime.now().minusMonths(1)));
			break;
		case 4: // moins d'une semaine
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(LocalDateTime.now().minusWeeks(1)));
			break;
		case 5: // moins d'un jour
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(LocalDateTime.now().minusDays(1)));
			break;
		default:
			// ajouter tests debut et fin non null
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(debut, fin));
			model.addAttribute("datestext", datestext);
		}
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
		List<LocalDateTime> fin = new ArrayList<>();
		List<Integer> jAvants = new ArrayList<>();
		List<Evenement> evens = new ArrayList<>();
		for (Evenement evenement : evenements) {
			try {
				Matcher m = Notification.MOINSNB1NB2.matcher(evenement.getValeur());
				if (!m.matches()) {
					continue;
				}
				int joursAvant = Integer.parseInt(m.group(2));
				int joursDuree = "".equals(m.group(3)) ? 0 : Integer.parseInt(m.group(3));
				// Si la date de notification est atteinte
				if (tous || (dateNow.isAfter(evenement.getDate().minusDays(joursAvant)) && (("-".equals(m.group(1)))
						|| (dateNow.isBefore(evenement.getDate().plusDays(joursDuree)))))) {
					evens.add(evenement);
					fin.add(joursDuree == 0 ? null : evenement.getDate().plusDays(joursDuree));
					jAvants.add(joursAvant);
				}
			} catch (NumberFormatException nfe) {
				// valeur n'est pas un entier
				logger.error("Erreur Integer.parseInt");
			}
		}
		model.addAttribute(Const.EVENEMENTS, evens);
		model.addAttribute("fin", fin);
		model.addAttribute("jAvants", jAvants);
		return "evenement/evenementNotifListe";
	}

	/**
	 * Liste événements notifications
	 */
	@GetMapping("/ganttNotif")
	public String ganttNotif(Model model) {
		List<Evenement> evenements = evenementRepository.findNotification();
		List<LocalDateTime> fin = new ArrayList<>();
		List<Integer> jAvants = new ArrayList<>();
		List<Evenement> evens = new ArrayList<>();

		DateTimeFormatter fmtYYYYMMDDHHMM = DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM);
		DateTimeFormatter fmtYYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode arrayNode = mapper.createArrayNode();

		for (Evenement evenement : evenements) {
			try {
				Matcher m = Notification.MOINSNB1NB2.matcher(evenement.getValeur());
				if (!m.matches()) {
					continue;
				}
				int joursAvant = Integer.parseInt(m.group(2));
				int joursDuree = "".equals(m.group(3)) ? 0 : Integer.parseInt(m.group(3));

				evens.add(evenement);
				fin.add(joursDuree == 0 ? null : evenement.getDate().plusDays(joursDuree));
				jAvants.add(joursAvant);

				// https://frappe.io/gantt
				ObjectNode task = mapper.createObjectNode();
				task.put("id", evenement.getId());
				task.put("name", evenement.getDate().format(fmtYYYYMMDDHHMM));
				task.put("start", evenement.getDate().format(fmtYYYYMMDD));
				task.put("end", evenement.getDate().plusDays(joursDuree).format(fmtYYYYMMDD));
				// task.put("progress", 0);
				// task.put("dependencies", "");
				arrayNode.add(task);

			} catch (NumberFormatException nfe) {
				// valeur n'est pas un entier
				logger.error("Erreur Integer.parseInt");
			}
		}
		String tasks;
		try {
			tasks = mapper.writeValueAsString(arrayNode);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			tasks = "";
		}
		model.addAttribute("tasks", tasks);

		return "evenement/evenementNotifGantt";
	}

	/*
	 * Détail d'un événement type est le type d'événement pour permettre le retour
	 * vers une liste d'événements typée : "pesée", "ajout ruche"... itemId est l'id
	 * ruche, essaim... pour permettre le retour vers l'affichage des événements de
	 * l'objet par exemple type=ruche et itemId id de la ruche
	 */
	@GetMapping("/{evenementId}")
	public String evenement(Model model, @PathVariable long evenementId,
			@RequestParam(defaultValue = "") @Nullable String type,
			@RequestParam(defaultValue = "0") @Nullable Long itemId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENT, evenementOpt.get());
			model.addAttribute("type", type);
			model.addAttribute("itemId", itemId);
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
		return "evenement/evenementDetail";
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
			logger.info("Evénement {} supprimé, id {}", evenement.getDate(), evenement.getId());
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
		return REDIRECT_EVEN_LISTE;
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
	public String modifie(Model model, HttpServletRequest request, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			model.addAttribute(Const.RUCHES, rucheRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.RUCHERS, rucherRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.HAUSSES, hausseRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute(Const.ESSAIMS, essaimRepository.findAllProjectedIdNomByOrderByNom());
			model.addAttribute("complet", "false");
			model.addAttribute(Const.EVENEMENT, evenementOpt.get());
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
		return EVEN_EVENFORM;
	}

	/*
	 * Enregistrement de l'événement
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return EVEN_EVENFORM;
		}
		evenementRepository.save(evenement);
		logger.info("Evénement {} enregistré, id {}", evenement.getDate(), evenement.getId());
		return REDIRECT_EVEN + evenement.getId();
	}

}
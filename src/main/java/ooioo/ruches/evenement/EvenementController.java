package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	/*
	 * Liste événements
	 */
	@GetMapping("/liste")
	public String liste(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1, 
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext) {
		if (periode == null) {
			periode = 2;
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
			// ajouter tests date1 et date2 non null
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findPeriode(date1, date2));
			model.addAttribute("datestext", datestext);
		}	
		model.addAttribute("periode", periode);
		return Const.EVEN_EVENLISTE;
	}

	/*
	 * Détail d'un événement
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
	@GetMapping("/{evenementId}")
	public String evenement(Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENT, evenementOpt.get());
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
		return "evenement/evenementDetail";
	}
*/
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
	 * avec tous les champs. Une case à cocher permet de choisir ruche, rucher, hausse
	 * et essaim librement.
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
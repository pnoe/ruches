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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.EssaimService;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.ruche.RucheService;

@Controller
@RequestMapping("/evenement/ruche")
public class EvenementRucheController {

	private static final String RUCHEID = "rucheId";

	final Logger logger = LoggerFactory.getLogger(EvenementRucheController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private HausseRepository hausseRepository;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	private RucheService rucheService;
	@Autowired
	private EssaimService essaimService;
	/*
	 * Liste événements poids ruche
	 */
	@GetMapping("/listePoidsRuche")
	public String listePoidsRuche(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1, 
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext) {
		if (periode == null) {
			periode = 3;
		}
		Iterable<Evenement> evenements;
		switch (periode) {
		case 1: // toute période
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHEPESEE);
			break;
		case 2: // moins d'un an
			evenements =  
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHEPESEE, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		List<BigDecimal> diff = new ArrayList<>();
		for (Evenement evenement : evenements) {
			BigDecimal d = new BigDecimal(evenement.getValeur()).subtract(evenement.getRuche().getPoidsVide());
			diff.add(d);
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
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1, 
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext) {
		if (periode == null) {
			periode = 3;
		}
		Iterable<Evenement> evenements;
		switch (periode) {
		case 1: // toute période
			evenements = evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.RUCHECADRE);
			break;
		case 2: // moins d'un an
			evenements =  
					evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, LocalDateTime.now().minusYears(1));
			break;
		case 3: // moins d'un mois
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, LocalDateTime.now().minusMonths(1));
			break;
		case 4: // moins d'une semaine
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, LocalDateTime.now().minusWeeks(1));
			break;
		case 5: // moins d'un jour
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, LocalDateTime.now().minusDays(1));
			break;
		default:
			// ajouter tests date1 et date2 non null
			evenements = 
					evenementRepository.findTypePeriode(TypeEvenement.RUCHECADRE, date1, date2);
			model.addAttribute("datestext", datestext);
		}
		model.addAttribute(Const.EVENEMENTS, evenements);
		model.addAttribute("periode", periode);
		return "evenement/evenementCadreRucheListe";
	}
	
	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIRERUCHE
	 */
	@GetMapping("/commentaire/{rucheId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long rucheId, 
			@RequestParam(defaultValue = "false") boolean retourEssaim) {
		model.addAttribute(Const.RETOURESSAIM, retourEssaim);
		return prepareAppelFormulaire(session, model, rucheId, "ruche/rucheCommentaireForm");
	}

	/**
	 * Appel du formulaire pour un événement Cadres
	 */
	@GetMapping("/cadre/{rucheId}")
	public String creeCadres(HttpSession session, Model model, @PathVariable long rucheId, 
			@RequestParam(defaultValue = "false") boolean retourEssaim) {
		model.addAttribute(Const.RETOURESSAIM, retourEssaim);
		String template = prepareAppelFormulaire(session, model, rucheId, "ruche/rucheCadreForm");
		Ruche ruche = (Ruche)model.asMap().get("ruche");
		essaimService.modelAddEvenement(model, ruche.getEssaim(), TypeEvenement.RUCHECADRE);
		return template;
	}

	/**
	 * Appel du formulaire pour un événement Pesée
	 */
	@GetMapping("/pesee/{rucheId}")
	public String creePesee(HttpSession session, Model model, @PathVariable long rucheId, 
			@RequestParam(defaultValue = "false") boolean retourEssaim) {
		model.addAttribute(Const.RETOURESSAIM, retourEssaim);
		String template = prepareAppelFormulaire(session, model, rucheId, "ruche/ruchePeseeForm");
		rucheService.modelAddEvenement(model, (Ruche)model.asMap().get("ruche"), TypeEvenement.RUCHEPESEE);
		return template;
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
	 * Préparation de l'appel du formulaire pour un événement ruche
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
	 * Enregistre un événement ruche
	 */
	@PostMapping("/sauve/{rucheId}")
	public String sauve(Model model, @PathVariable long rucheId, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String valeur, @RequestParam String date, @RequestParam String commentaire,
			@RequestParam(defaultValue = "false") boolean retourEssaim) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, ruche.getEssaim(), ruche.getRucher(),
					null, valeur, commentaire);
			evenementRepository.save(evenement);
			logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
			if (retourEssaim) {
				return "redirect:/essaim/" + ruche.getEssaim().getId();
			} else {
				return "redirect:/ruche/" + rucheId;
			}
		}
		logger.error(Const.IDRUCHEXXINCONNU, rucheId);
		model.addAttribute(Const.MESSAGE, 
				messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/**
	 * Liste des événements d'une ruche
	 */
	@GetMapping("/{rucheId}")
	public String liste(Model model, @PathVariable long rucheId) {
		model.addAttribute(Const.EVENEMENTS, evenementRepository.findByRucheId(rucheId));
		model.addAttribute(RUCHEID, rucheId);
		Iterable<RecolteHausse> recolteHausses = recolteHausseRepository.findByRucheId(rucheId);
		model.addAttribute("recoltehausses", recolteHausses);
		ArrayList<LocalDateTime> datesRecolteHausses = new ArrayList<>();
		for (RecolteHausse recolteHausse : recolteHausses) {
			datesRecolteHausses.add(recolteHausse.getRecolte().getDate());
		}
		model.addAttribute("datesRecolteHausses", datesRecolteHausses);
		// pour lien retour dans la liste vers détail ruche
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			model.addAttribute("rucheNom", ruche.getNom());
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE, 
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return Const.EVEN_EVENLISTE;
	}

}
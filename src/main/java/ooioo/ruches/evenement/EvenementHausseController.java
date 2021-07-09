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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
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

	// private static final String HAUSSEID = "hausseId";
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
	 * Appel formulaire événement COMMENTAIREHAUSSE
	 */
	@GetMapping("/commentaire/{hausseId}")
	public String creeCommentaire(HttpSession session, Model model, @PathVariable long hausseId) {
		return prepareAppelFormulaire(session, model, hausseId, "hausse/hausseCommentaireForm");
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
			} else {
				logger.error("Nom hausse {} inconnu", hausseNoms);
			}
		}
		return "redirect:/hausse/liste";
	}

	/**
	 * Appel formulaire événement hausse remplissage
	 */
	@GetMapping("/remplissage/{hausseId}")
	public String creeRemplissage(HttpSession session, Model model, @PathVariable long hausseId) {
		String template = prepareAppelFormulaire(session, model, hausseId, "hausse/hausseRemplissageForm");
		hausseService.modelAddEvenement(model, (Hausse)model.asMap().get("hausse"), TypeEvenement.HAUSSEREMPLISSAGE);
		return template;
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
	 * Sauvegarde événement hausse
	 */
	@PostMapping("/sauve/{hausseId}")
	public String sauve(Model model, @PathVariable long hausseId, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String valeur, @RequestParam String date, @RequestParam String commentaire) {
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
			logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE, 
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/hausse/" + hausseId;
	}

	/**
	 * Liste des événements d'une hausse
	 */
	@GetMapping("/{hausseId}")
	public String liste(Model model, @PathVariable long hausseId) {
		model.addAttribute(Const.EVENEMENTS, evenementRepository.findByHausseId(hausseId));
		model.addAttribute("itemId", hausseId);
		model.addAttribute("type", "hausse");
		Optional<Hausse> hausseOpt = hausseRepository.findById(hausseId);
		if (hausseOpt.isPresent()) {
			model.addAttribute("hausseNom", hausseOpt.get().getNom());
		} else {
			logger.error(Const.IDHAUSSEXXINCONNU, hausseId);
			model.addAttribute(Const.MESSAGE, 
					messageSource.getMessage(Const.IDHAUSSEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return Const.EVEN_EVENLISTE;
	}

}
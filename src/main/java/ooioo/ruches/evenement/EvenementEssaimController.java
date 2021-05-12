package ooioo.ruches.evenement;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.essaim.EssaimService;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Controller
@RequestMapping("/evenement/essaim")
public class EvenementEssaimController {

	private static final String ESSAIMID = "essaimId";

	final Logger logger = LoggerFactory.getLogger(EvenementEssaimController.class);

	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;

	@Autowired
	MessageSource messageSource;

	@Autowired
	private RucherService rucherService;
	@Autowired
	private EssaimService essaimService;
	
	/*
	 * Liste événements essaim sucre
	 */
	@GetMapping("/listeSucre")
	public String listeSucre(Model model, @RequestParam(required = false) Integer periode,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1, 
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
			@RequestParam(required = false) String datestext) {
		if (periode == null) {
			periode = 3;
		}
		switch (periode) {
		case 1: // toute période
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMSUCRE));
			break;
		case 2: // moins d'un an
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMSUCRE, LocalDateTime.now().minusYears(1)));
			break;
		case 3: // moins d'un mois
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMSUCRE, LocalDateTime.now().minusMonths(1)));
			break;
		case 4: // moins d'une semaine
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMSUCRE, LocalDateTime.now().minusWeeks(1)));
			break;
		case 5: // moins d'un jour
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMSUCRE, LocalDateTime.now().minusDays(1)));
			break;
		default:
			// ajouter tests date1 et date2 non null
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMSUCRE, date1, date2));
			model.addAttribute("datestext", datestext);
		}	
		model.addAttribute("periode", periode);
		return "evenement/evenementSucreListe";
	}
	
	/*
	 * Liste événements essaim traitement
	 */
	@GetMapping("/listeTraitement")
	public String listeTraitement(Model model, @RequestParam(required = false) Integer periode,
	@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date1, 
	@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime date2,
	@RequestParam(required = false) String datestext) {
		if (periode == null) {
			periode = 3;
		}
		switch (periode) {
		case 1: // toute période
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMTRAITEMENT));
			break;
		case 2: // moins d'un an
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMTRAITEMENT, LocalDateTime.now().minusYears(1)));
			break;
		case 3: // moins d'un mois
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMTRAITEMENT, LocalDateTime.now().minusMonths(1)));
			break;
		case 4: // moins d'une semaine
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMTRAITEMENT, LocalDateTime.now().minusWeeks(1)));
			break;
		case 5: // moins d'un jour
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMTRAITEMENT, LocalDateTime.now().minusDays(1)));
			break;
		default:
			// ajouter tests date1 et date2 non null
			model.addAttribute(Const.EVENEMENTS, 
					evenementRepository.findTypePeriode(TypeEvenement.ESSAIMTRAITEMENT, date1, date2));
			model.addAttribute("datestext", datestext);
		}	
		model.addAttribute("periode", periode);
		return "evenement/evenementTraitementListe";
	}
	
	/**
	 * Appel du formulaire pour l'ajout de sucre pour un lot d'essaims
	 */
	@GetMapping("/sucreLot/{essaimsNoms}")
	public String sucreLot(HttpSession session, Model model, @PathVariable String essaimsNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute("essaimsNoms", essaimsNoms);
		return "essaim/essaimSucreLotForm";
	}

	/**
	 * Appel du formulaire pour le traitement contre le varoa d'un lot d'essaims
	 */
	@GetMapping("/traitementLot/{essaimsNoms}")
	public String traitementLot(HttpSession session, Model model, @PathVariable String essaimsNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute("essaimsNoms", essaimsNoms);
		return "essaim/essaimTraitementLotForm";
	}
	
	/**
	 * Appel du formulaire pour créer des commentaires pour un lot d'essaims
	 */
	@GetMapping("/commentaireLot/{essaimsNoms}")
	public String commentaireLot(HttpSession session, Model model, @PathVariable String essaimsNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute("essaimsNoms", essaimsNoms);
		return "essaim/essaimCommentaireLotForm";
	}

	/**
	 * Créations des événements pour un lot d'essaims
	 */
	@PostMapping("/sauve/lot/{essaimsNom}")
	public String sauveLot(@PathVariable String[] essaimsNom, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String date, @RequestParam String valeur, @RequestParam String commentaire) {
		for (String essaimNom : essaimsNom) {
			Essaim essaim = essaimRepository.findByNom(essaimNom);
			Ruche ruche = rucheRepository.findByEssaimId(essaim.getId());
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, essaim, rucher, null, valeur,
					commentaire); // valeur commentaire
			evenementRepository.save(evenement);
		}
		return "redirect:/essaim/liste";
	}

	/**
	 * Appel du formulaire pour la création d'un événement COMMENTAIREESSAIM
	 */
	@GetMapping("/commentaire/{essaimId}")
	public String commentaire(HttpSession session, Model model, @PathVariable long essaimId, 
			@RequestParam(defaultValue = "false") boolean retourRuche) {
		model.addAttribute(Const.RETOURRUCHE, retourRuche);
		return prepareAppelFormulaire(session, model, essaimId, "essaim/essaimCommentaireForm");
	}

	/**
	 * Appel du formulaire de création d'un événement essaim sucre
	 */
	@GetMapping("/sucre/{essaimId}")
	public String sucre(HttpSession session, Model model, @PathVariable long essaimId, 
			@RequestParam(defaultValue = "false") boolean retourRuche) {
		model.addAttribute(Const.RETOURRUCHE, retourRuche);
		String template = prepareAppelFormulaire(session, model, essaimId, "essaim/essaimSucreForm");
		essaimService.modelAddEvenement(model, (Essaim)model.asMap().get("essaim"), TypeEvenement.ESSAIMSUCRE);
		return template;
	}

	/**
	 * Appel du formulaire de création d'un événement essaim traitement
	 */
	@GetMapping("/traitement/{essaimId}")
	public String traitement(HttpSession session, Model model, @PathVariable long essaimId, 
			@RequestParam(defaultValue = "false") boolean retourRuche) {
		model.addAttribute(Const.RETOURRUCHE, retourRuche);
		String template = prepareAppelFormulaire(session, model, essaimId, "essaim/essaimTraitementForm");
		essaimService.modelAddEvenement(model, (Essaim)model.asMap().get("essaim"), TypeEvenement.ESSAIMTRAITEMENT);
		return template;
	}

	/**
	 * Préparation de l'appel du formulaire pour un événement essaim
	 */
	public String prepareAppelFormulaire(HttpSession session, Model model, long essaimId, String template) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.ESSAIM, essaim);
			return template;
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		model.addAttribute(Const.MESSAGE, 
				messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/**
	 * Sauvegarde d'un événement essaim
	 */
	@PostMapping("/sauve/{essaimId}")
	public String sauve(Model model, @PathVariable long essaimId, @RequestParam TypeEvenement typeEvenement,
			@RequestParam String valeur, @RequestParam String date, @RequestParam String commentaire) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Evenement evenement = new Evenement(dateEve, typeEvenement, ruche, essaim, rucher, null, valeur,
					commentaire);
			evenementRepository.save(evenement);
			logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
			return Const.REDIRECT_ESSAIM_ESSAIMID;
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		model.addAttribute(Const.MESSAGE, 
				messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/**
	 * Appel du formulaire de dispersion d'un essaim
	 */
	@GetMapping("/dispersion/{essaimId}")
	public String dispersion(HttpSession session, Model model, @PathVariable long essaimId) {		
		// ajouter liste des essaims qui ne sont pas dans des ruches triés par date création décroissante
		//  pour remérage
		Ruche ruche = rucheRepository.findByEssaimId(essaimId);
		if (ruche != null) {
			Iterable<Object[]> essaimsRemerage = essaimRepository.findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();
			if (essaimsRemerage.iterator().hasNext()) {
				model.addAttribute("nomRuche", ruche.getNom());
				model.addAttribute("essaimsRemerage", essaimsRemerage);
			}
		}
		return prepareAppelFormulaire(session, model, essaimId, "essaim/essaimDispersionForm");
	}

	/**
	 * Enregistrement de la dispersion
	 * Crée un événement dispersion, enlève l'essaim de la ruche et inactive l'essaim
	 *   met cette ruche au dépôt si option choisie, crée événement RUCHEAJOUTRUCHER
	 * Option remérage : met l'essaim choisi dans la ruche et crée l'événement
	 *   AJOUTESSAIMRUCHE
	 */
	@PostMapping("/sauve/dispersion/{essaimId}")
	public String sauveDispersion(Model model, @PathVariable long essaimId, @RequestParam(defaultValue = "false") boolean depot,
			@RequestParam String date,
			@RequestParam String commentaire,
			@RequestParam(defaultValue = "") Long remerageId,
			@RequestParam(defaultValue = "false") boolean evencadre) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Essaim essaim = essaimOpt.get();
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			Rucher rucher = null;
			Rucher rucherDepot = rucherRepository.findByDepotIsTrue();
			if (ruche != null) {
				rucher = ruche.getRucher();
				// Tester si remérage demandé
				if (remerageId == null) {
					// Pas de remérage : on enlève l'esssaim de la ruche
					ruche.setEssaim(null);
				} else {
					// Remérage : on met le nouvel essaim dans la ruche
					Optional<Essaim> essaimOptRemerage = essaimRepository.findById(remerageId);
					if (essaimOptRemerage.isPresent()) {
						Essaim essaimRemerage = essaimOptRemerage.get();
						Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, ruche, essaimRemerage,
								ruche.getRucher(), null, null, commentaire); 
						evenementRepository.save(evenementAjout);
						ruche.setEssaim(essaimRemerage);
					} else {
						logger.error(Const.IDESSAIMXXINCONNU, remerageId);
						model.addAttribute(Const.MESSAGE, 
								messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
						return Const.INDEX;
					}
				}
				rucheRepository.save(ruche);
				if ((depot) && (!rucher.getId().equals(rucherDepot.getId()))) {
					// Met la ruche au dépôt et créer l'événement RUCHEAJOUTRUCHER
					// Si la ruche est déjà au dépôt on ne fait rien
					String[] ruchesNoms = new String[] { ruche.getNom() };
					rucherService.sauveAjouterRuches(rucherDepot, ruchesNoms, date, 
							"Dispersion essaim " + essaim.getNom() + ". " + commentaire);
				}
			}
			// LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
			Evenement evenement = new Evenement(dateEve, TypeEvenement.ESSAIMDISPERSION, ruche, essaim, rucher, null,
					null, commentaire); // valeur
			evenementRepository.save(evenement);
			if (evencadre) {
				// Evénement cadre : valeur 0 pour zéro cadre, essaim null, commentaire "Dispersion essaim xx"
				Evenement eveCadre = new Evenement(dateEve, TypeEvenement.RUCHECADRE, ruche, null,
					(depot)?rucherDepot:rucher, null, "0", "Dispersion essaim " + essaim.getNom());
				evenementRepository.save(eveCadre);
			}
			// On inactive l'essaim
			essaim.setActif(false);
			essaimRepository.save(essaim);
			logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
			// essaim inactivé on affiche la liste des essaims
			return "redirect:/essaim/liste";
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		model.addAttribute(Const.MESSAGE, 
				messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/**
	 * Liste des événements d'un essaim
	 */
	@GetMapping("/{essaimId}")
	public String liste(Model model, @PathVariable long essaimId) {
		model.addAttribute(Const.EVENEMENTS, evenementRepository.findByEssaimId(essaimId));
		model.addAttribute(ESSAIMID, essaimId);
		Iterable<RecolteHausse> recolteHausses = recolteHausseRepository.findByEssaimId(essaimId);
		model.addAttribute("recoltehausses", recolteHausses);
		ArrayList<LocalDateTime> datesRecolteHausses = new ArrayList<>();
		for (RecolteHausse recolteHausse : recolteHausses) {
			datesRecolteHausses.add(recolteHausse.getRecolte().getDate());
		}
		model.addAttribute("datesRecolteHausses", datesRecolteHausses);
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			model.addAttribute("essaimNom", essaimOpt.get().getNom());
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE, 
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return Const.EVEN_EVENLISTE;
	}

}
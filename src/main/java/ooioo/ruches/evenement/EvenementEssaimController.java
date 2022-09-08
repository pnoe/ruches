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
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.essaim.EssaimService;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;
import ooioo.ruches.rucher.RucherService;

@Controller
@RequestMapping("/evenement/essaim")
public class EvenementEssaimController {

	private static final String ESSAIMPASDANSRUCHE = "Essaimage : l'essaim n'est pas dans une ruche !";
	private static final String ESSAIMSNOMS = "essaimsNoms";

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
		switch (periode) {
		case 1: // toute période
			model.addAttribute(Const.EVENEMENTS,
					evenementRepository.findByTypeOrderByDateDesc(TypeEvenement.ESSAIMSUCRE));
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
	 * @param tous : événements traitement début et fin si true, début seulement sinon
	 */
	@GetMapping("/listeTraitement/{tous}")
	public String listeTraitement(Model model, @PathVariable boolean tous,
			@RequestParam(required = false) Integer periode,
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
		TypeEvenement typD = TypeEvenement.ESSAIMTRAITEMENT;
		TypeEvenement typF = TypeEvenement.ESSAIMTRAITEMENTFIN;
		switch (periode) {
		case 1: // toute période
			model.addAttribute(Const.EVENEMENTS, tous ? evenementRepository.findByTypeOrTypeOrderByDateDesc(typD, typF)
					: evenementRepository.findByTypeOrderByDateDesc(typD));
			break;
		case 2: // moins d'un an
			model.addAttribute(Const.EVENEMENTS,
					tous ? evenementRepository.findTypePeriode(typD, typF, LocalDateTime.now().minusYears(1))
							: evenementRepository.findTypePeriode(typD, LocalDateTime.now().minusYears(1)));
			break;
		case 3: // moins d'un mois
			model.addAttribute(Const.EVENEMENTS,
					tous ? evenementRepository.findTypePeriode(typD, typF, LocalDateTime.now().minusMonths(1))
							: evenementRepository.findTypePeriode(typD, LocalDateTime.now().minusMonths(1)));
			break;
		case 4: // moins d'une semaine
			model.addAttribute(Const.EVENEMENTS,
					tous ? evenementRepository.findTypePeriode(typD, typF, LocalDateTime.now().minusWeeks(1))
							: evenementRepository.findTypePeriode(typD, LocalDateTime.now().minusWeeks(1)));
			break;
		case 5: // moins d'un jour
			model.addAttribute(Const.EVENEMENTS,
					tous ? evenementRepository.findTypePeriode(typD, typF, LocalDateTime.now().minusDays(1))
							: evenementRepository.findTypePeriode(typD, LocalDateTime.now().minusDays(1)));
			break;
		default:
			// ajouter tests date1 et date2 non null
			model.addAttribute(Const.EVENEMENTS, tous ? evenementRepository.findTypePeriode(typD, typF, date1, date2)
					: evenementRepository.findTypePeriode(typD, date1, date2));
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
		model.addAttribute(ESSAIMSNOMS, essaimsNoms);
		return "essaim/essaimSucreLotForm";
	}

	/**
	 * Appel du formulaire pour le traitement contre le varoa d'un lot d'essaims
	 */
	@GetMapping("/traitementLot/{essaimsNoms}")
	public String traitementLot(HttpSession session, Model model, @PathVariable String essaimsNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute(ESSAIMSNOMS, essaimsNoms);
		return "essaim/essaimTraitementLotForm";
	}

	/**
	 * Appel du formulaire pour créer des commentaires pour un lot d'essaims
	 */
	@GetMapping("/commentaireLot/{essaimsNoms}")
	public String commentaireLot(HttpSession session, Model model, @PathVariable String essaimsNoms) {
		model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
		model.addAttribute(ESSAIMSNOMS, essaimsNoms);
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
	 * Appel du formulaire de création d'un événement essaim COMMENTAIREESSAIM. On
	 * passe un nouvel événement avec : - la date courante (ou décalée) - le type
	 * essaimsucre - les objets ruche, essaim, rucher
	 */
	@GetMapping("/commentaire/cree/{essaimId}")
	public String commentaire(HttpSession session, Model model, @PathVariable long essaimId) {
		// TODO factoriser avec even sucre si seul le formulaire de retour change
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.COMMENTAIREESSAIM, ruche,
					essaimOpt.get(), rucher, null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			return "essaim/essaimCommentaireForm";
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement essaim commentaire
	 */
	@GetMapping("/commentaire/modifie/{evenementId}")
	public String commentaireModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "essaim/essaimCommentaireForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de création d'un événement essaim sucre. On passe un
	 * nouvel événement avec : - la date courante (ou décalée) - le type essaimsucre
	 * - les objets ruche, essaim, rucher
	 */
	@GetMapping("/sucre/cree/{essaimId}")
	public String sucre(HttpSession session, Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.ESSAIMSUCRE, ruche, essaim,
					rucher, null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			// pour rappel du dernier événement sucre dans le fomulaire de saisie :
			essaimService.modelAddEvenement(model, essaim, TypeEvenement.ESSAIMSUCRE);
			return "essaim/essaimSucreForm";
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement essaim sucre
	 */
	@GetMapping("/sucre/modifie/{evenementId}")
	public String sucreModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		// pour rappel du dernier événement sucre dans le fomulaire de saisie :
		essaimService.modelAddEvenement(model, (Essaim) model.asMap().get(Const.ESSAIM), TypeEvenement.ESSAIMSUCRE);
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			return "essaim/essaimSucreForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de création d'un événement ESSAIMTRAITEMENT. On passe un
	 * nouvel événement avec : - la date courante (ou décalée) - le type essaimsucre
	 * - les objets ruche, essaim, rucher
	 */
	@GetMapping("/traitement/cree/{essaimId}")
	public String traitement(HttpSession session, Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			Rucher rucher = null;
			if (ruche != null) {
				rucher = ruche.getRucher();
			}
			var evenement = new Evenement(Utils.dateTimeDecal(session), TypeEvenement.ESSAIMTRAITEMENT, ruche, essaim,
					rucher, null, null, null);
			model.addAttribute(Const.EVENEMENT, evenement);
			// pour rappel du dernier événement sucre dans le fomulaire de saisie :
			essaimService.modelAddEvenement(model, essaim, TypeEvenement.ESSAIMTRAITEMENT);
			return "essaim/essaimTraitementForm";
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de modification d'un événement ESSAIMTRAITEMENT
	 */
	@GetMapping("/traitement/modifie/{evenementId}")
	public String traitementModifie(HttpSession session, Model model, @PathVariable long evenementId) {
		Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
		if (evenementOpt.isPresent()) {
			Evenement evenement = evenementOpt.get();
			model.addAttribute(Const.EVENEMENT, evenement);
			// pour rappel du dernier événement sucre dans le fomulaire de saisie :
			essaimService.modelAddEvenement(model, evenement.getEssaim(), TypeEvenement.ESSAIMTRAITEMENT);
			return "essaim/essaimTraitementForm";
		} else {
			logger.error(Const.IDEVENEMENTXXINCONNU, evenementId);
			model.addAttribute(Const.MESSAGE, Const.IDEVENEMENTINCONNU);
			return Const.INDEX;
		}
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
	 * Sauvegarde d'un événement commentaire essaim. Récupère tous les champs de
	 * l'événement du formulaire
	 */
	@PostMapping("/commentaire/sauve")
	public String commentaireSauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "essaim/essaimCommentaireForm";
		}
		evenement.setValeur(Utils.notifIntFmt(evenement.getValeur()));
		evenementRepository.save(evenement);
		logger.info("Evénement {} enregistré, id {}", evenement.getDate(), evenement.getId());
		logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
		return "redirect:/essaim/" + evenement.getEssaim().getId();
	}

	/**
	 * Sauvegarde d'un événement essaim. Récupère tous les champs de l'événement du
	 * formulaire
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Evenement evenement, BindingResult bindingResult) {
		// le template pour return doit être passé en paramètre si on veut
		// utiliser sauve pour tous les even essaim (sucre, varoa...)
		/*
		 * if (bindingResult.hasErrors()) { return "essaim/essaimSucreForm"; }
		 */
		evenementRepository.save(evenement);
		logger.info("Evénement {} enregistré, id {}", evenement.getDate(), evenement.getId());
		logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
		// return Const.REDIRECT_ESSAIM_ESSAIMID;
		return "redirect:/essaim/" + evenement.getEssaim().getId();
	}

	/**
	 * Appel du formulaire de dispersion d'un essaim
	 */
	@GetMapping("/dispersion/{essaimId}")
	public String dispersion(HttpSession session, Model model, @PathVariable long essaimId) {
		// ajouter liste des essaims qui ne sont pas dans des ruches triés par date
		// création décroissante
		// pour remérage
		Ruche ruche = rucheRepository.findByEssaimId(essaimId);
		if (ruche != null) {
			Iterable<Object[]> essaimsRemerage = essaimRepository
					.findProjectedIdNomByRucheIsNullOrderByDateAcquisitionDesc();
			if (essaimsRemerage.iterator().hasNext()) {
				model.addAttribute("nomRuche", ruche.getNom());
				model.addAttribute("essaimsRemerage", essaimsRemerage);
			}

			model.addAttribute(Const.RUCHE, ruche);

			// Si le retour au dépôt est demandé dans le formulaire, il faudra
			// que la date choisie soit postérieure à celle du dernier ajout
			// de la ruche dans son rucher
			Evenement evenFirst = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
					ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
			model.addAttribute("dateTime", evenFirst.getDate());
			LocalDateTime dateTimeFirst = evenFirst.getDate().plusMinutes(1);
			model.addAttribute("dateFirst", dateTimeFirst.format(dateFormat));
			model.addAttribute("timeFirst", dateTimeFirst.format(timeFormat));
			return prepareAppelFormulaire(session, model, essaimId, "essaim/essaimDispersionForm");

		} else {
			logger.error(ESSAIMPASDANSRUCHE);
			model.addAttribute(Const.MESSAGE, ESSAIMPASDANSRUCHE);
			return Const.INDEX;
		}

	}

	/**
	 * Enregistrement de la dispersion Crée un événement dispersion, enlève l'essaim
	 * de la ruche et inactive l'essaim met cette ruche au dépôt si option choisie,
	 * crée événement RUCHEAJOUTRUCHER et crée un événement 0 cadre si demandé
	 * Option remérage : met l'essaim choisi dans la ruche et crée l'événement
	 * AJOUTESSAIMRUCHE
	 */
	@PostMapping("/sauve/dispersion/{essaimId}")
	public String sauveDispersion(Model model, @PathVariable long essaimId,
			@RequestParam(defaultValue = "false") boolean depot, @RequestParam String date,
			@RequestParam String commentaire, @RequestParam(defaultValue = "") Long remerageId,
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
						Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, ruche,
								essaimRemerage, ruche.getRucher(), null, null, commentaire);
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
				if ((depot) && (remerageId == null) && (!rucher.getId().equals(rucherDepot.getId()))) {
					// Met la ruche au dépôt et crée l'événement RUCHEAJOUTRUCHER
					// Si la ruche est déjà au dépôt ou un remérage a été fait, on ne fait rien
					String[] ruchesNoms = new String[] { ruche.getNom() };
					rucherService.sauveAjouterRuches(rucherDepot, ruchesNoms, date,
							"Dispersion essaim " + essaim.getNom() + ". " + commentaire);
				}

				Evenement evenement = new Evenement(dateEve, TypeEvenement.ESSAIMDISPERSION, ruche, essaim, rucher,
						null, null, commentaire); // valeur
				evenementRepository.save(evenement);
				if ((evencadre) && (remerageId == null)) {
					// Evénement cadre : valeur 0 pour zéro cadre, essaim null, commentaire
					// "Dispersion essaim xx"
					Evenement eveCadre = new Evenement(dateEve, TypeEvenement.RUCHECADRE, ruche, null,
							(depot) ? rucherDepot : rucher, null, "0", "Dispersion essaim " + essaim.getNom());
					evenementRepository.save(eveCadre);
				}
				// On inactive l'essaim
				essaim.setActif(false);
				essaimRepository.save(essaim);
				logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
				// essaim inactivé on affiche la liste des essaims
				return "redirect:/essaim/liste";
			} else {
				// TODO il faudrait rester sur le détail essaim et afficher
				// le message d'erreur dans cette page
				logger.error(ESSAIMPASDANSRUCHE);
				model.addAttribute(Const.MESSAGE, ESSAIMPASDANSRUCHE);
				return Const.INDEX;
			}
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
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			model.addAttribute(Const.EVENEMENTS, evenementRepository.findByEssaimId(essaimId));
			model.addAttribute("type", Const.ESSAIM);
			model.addAttribute("essaimNom", essaimOpt.get().getNom());
			return Const.EVEN_EVENLISTE;
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

}
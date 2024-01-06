package ooioo.ruches.essaim;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.IdNom;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteHausse;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheRepository;
import ooioo.ruches.rucher.Rucher;

@Controller
@RequestMapping("/essaim")
public class EssaimController {

	private static final String ESSAIM_ESSAIMFORM = "essaim/essaimForm";
	private static final String ESSAIMSRRR = "EssaimsRRr";

	private final Logger logger = LoggerFactory.getLogger(EssaimController.class);

	private final EssaimRepository essaimRepository;
	private final RucheRepository rucheRepository;
	private final EvenementRepository evenementRepository;
	private final RecolteRepository recolteRepository;
	private final RecolteHausseRepository recolteHausseRepository;
	private final MessageSource messageSource;
	private final EssaimService essaimService;

	public EssaimController(EssaimRepository essaimRepository, RucheRepository rucheRepository,
			EvenementRepository evenementRepository, RecolteRepository recolteRepository,
			RecolteHausseRepository recolteHausseRepository, MessageSource messageSource, EssaimService essaimService) {
		this.essaimRepository = essaimRepository;
		this.rucheRepository = rucheRepository;
		this.evenementRepository = evenementRepository;
		this.recolteRepository = recolteRepository;
		this.recolteHausseRepository = recolteHausseRepository;
		this.messageSource = messageSource;
		this.essaimService = essaimService;
	}

	/**
	 * Graphe affichant les poids d'un essaim, les récoltes, les événments sucre,
	 * les traitements, les changements de rucher.
	 */
	@GetMapping("/poids/{essaimId}")
	public String poids(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			// Les pesées de l'essaim.
			List<Evenement> evesPesee = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.RUCHEPESEE);
			List<Long> dates = new ArrayList<>(evesPesee.size());
			List<Float> poids = new ArrayList<>(evesPesee.size());
			for (Evenement e : evesPesee) {
				dates.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
				poids.add(Float.parseFloat(e.getValeur()));
			}
			model.addAttribute("dates", dates);
			model.addAttribute("poids", poids);
			model.addAttribute("essaim", essaim);
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			model.addAttribute("ruche", ruche);
			// Les événements sucre.
			List<Evenement> evesSucre = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.ESSAIMSUCRE);
			List<Long> datesSucre = new ArrayList<>(evesSucre.size());
			List<Float> poidsSucre = new ArrayList<>(evesSucre.size());
			for (Evenement e : evesSucre) {
				datesSucre.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
				poidsSucre.add(Float.parseFloat(e.getValeur()));
			}
			model.addAttribute("datesSucre", datesSucre);
			model.addAttribute("poidsSucre", poidsSucre);
			// Les récoltes.
			// Calcul du poids de miel par récoltes pour cet essaim
			List<Long> datesRec = new ArrayList<>();
			List<Float> poidsRec = new ArrayList<>();
			List<String> ruchersRec = new ArrayList<>();
			for (Recolte recolte : recolteRepository.findAllByOrderByDateAsc()) {
				Integer pRec = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
				if (pRec != null) {
					RecolteHausse rHFirst = recolteHausseRepository.findFirstByRecolteAndEssaim(recolte, essaim);
					String rucherNom = ((rHFirst == null) || (rHFirst.getRucher() == null)) ? ""
							: rHFirst.getRucher().getNom();
					poidsRec.add(pRec / 1000f);
					datesRec.add(recolte.getDate().toEpochSecond(ZoneOffset.UTC));
					ruchersRec.add(rucherNom);
				}
			}
			model.addAttribute("poidsRec", poidsRec);
			model.addAttribute("datesRec", datesRec);
			model.addAttribute("ruchersRec", ruchersRec);
			// Les traitements.
			List<Evenement> evesTrait = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.ESSAIMTRAITEMENT);
			List<Long> datesTrait = new ArrayList<>(evesTrait.size());
			for (Evenement e : evesTrait) {
				datesTrait.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
			}
			model.addAttribute("datesTrait", datesTrait);
			// Les changements de rucher.
			List<Evenement> evesRucher = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.RUCHEAJOUTRUCHER);
			List<Long> datesRucher = new ArrayList<>(evesRucher.size());
			List<String> nomsRucher = new ArrayList<>(evesRucher.size());
			for (Evenement e : evesRucher) {
				if (e.getRucher() != null) {
					datesRucher.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
					nomsRucher.add(e.getRucher().getNom());
				}
			}
			model.addAttribute("datesRucher", datesRucher);
			model.addAttribute("nomsRucher", nomsRucher);

			// Les ajouts/retraits de cadres.
			List<Evenement> evesCadre = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.RUCHECADRE);
			List<Long> datesCadre = new ArrayList<>(evesCadre.size());
			List<String> nbsCadre = new ArrayList<>(evesCadre.size());
			for (Evenement e : evesCadre) {
				if (Utils.isIntInfX(e.getValeur(), 20)) {
					// Si la valeur de l'événement est un nombre (de cadres) compris entre 0 et 20.
					datesCadre.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
					nbsCadre.add(e.getValeur());
				}
			}
			model.addAttribute("datesCadre", datesCadre);
			model.addAttribute("nbsCadre", nbsCadre);

			return "essaim/essaimsPoids";
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/*
	 * Historique de la mise en ruchers d'un essaim. Les événements affichés dans
	 * l'historique : - les mise en rucher de ruches ou l'essaim apparait - la
	 * dispersion de l'essaim qui termine l'historique - la ou les mises en ruches
	 * de l'essaim qui peuvent impliquer des déplacements.
	 */
	@GetMapping("/historique/{essaimId}")
	public String historique(Model model, @PathVariable long essaimId) {
		if (essaimService.historique(model, essaimId)) {
			return "essaim/essaimHisto";
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		model.addAttribute(Const.MESSAGE,
				messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
		return Const.INDEX;
	}

	/*
	 * Statistiques (chartjs barchart) âges des reines. Appel icône dans page liste
	 * essaim.
	 */
	@GetMapping("/statistiquesage")
	public String statistiquesage(Model model) {
		essaimService.statistiquesage(model);
		return "essaim/essaimsStatAges";
	}

	/**
	 * Statistiques tableau poids de miel par essaim. Appel icône dans page liste
	 * essaim.
	 *
	 * @param rucherId       optionnel pour ne prendre en compte que les hausses de
	 *                       récolte dans ce rucher.
	 * @param masquerInactif true pour masquer les essaims inactifs.
	 */
	@GetMapping("/statistiques")
	public String statistiques(Model model, @RequestParam(required = false) Long rucherId,
			@RequestParam(defaultValue = "false") boolean masquerInactif) {
		essaimService.statistiques(model, rucherId, masquerInactif, null);
		return "essaim/essaimStatistiques";
	}

	/**
	 * Statistiques tableau poids de miel pour un essaim. Appel icône dans page
	 * détail essaim.
	 *
	 * @param essaimId l'essaim dont on veut les statitiques.
	 */
	@GetMapping("/statistiques/{essaimId}")
	public String statistiquesEssaim(Model model, @PathVariable Long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			essaimService.statistiques(model, null, false, essaimOpt.get());
			return "essaim/essaimStat";
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire d'essaimage.
	 */
	@GetMapping("/essaime/{essaimId}")
	public String essaimer(HttpSession session, Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			if (ruche == null) {
				logger.error("{} n'est pas dans un ruche", essaimOpt.get());
				model.addAttribute(Const.MESSAGE, "L'essaim n'est pas dans une ruche");
				return Const.INDEX;
			}
			// Liste des noms d'essaims déjà utilisés.
			// records -> strings pour liste plus compacte.
			List<Nom> nomsRecords = essaimRepository.findAllProjectedBy();
			List<String> noms = new ArrayList<>(nomsRecords.size());
			for (Nom essaimNom : nomsRecords) {
				noms.add(essaimNom.nom());
			}
			model.addAttribute(Const.ESSAIMNOMS, noms);
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.ESSAIM, essaimOpt.get());
			model.addAttribute(Const.RUCHE, ruche);
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "essaim/essaimEssaimerForm";
	}

	/**
	 * Enregistrement de l'essaimage.
	 * 
	 * @param essaimId    l'id de l'essaim qui essaime
	 * @param date        la date saisie dans le formulaire d'essaimage
	 * @param nom         le nom du nouvel essaim restant dans la ruche saisi dans
	 *                    le formulaire d'essaimage
	 * @param commentaire le commentaire saisi dans le formulaire d'essaimage
	 */
	@PostMapping("/essaime/sauve/{essaimId}")
	public String essaimeSauve(Model model, @PathVariable long essaimId, @RequestParam String date,
			@RequestParam String nom, @RequestParam String commentaire) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isEmpty()) {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		// On vérifie aussi côté serveur que le nom est libre.
		if (essaimRepository.findAllProjectedBy().contains(new Nom(nom))) {
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage("LeNomXXExiste", new Object[] { nom }, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		Essaim nouvelEssaim = essaimService.essaimSauve(essaimId, date, nom, commentaire, essaimOpt);
		return "redirect:/essaim/" + nouvelEssaim.getId();
	}

	/**
	 * Clonage multiple d'un essaim (appel XMLHttpRequest de la page détail d'un
	 * essaim) avec mises en ruches éventuelles.
	 *
	 * @param session   pour gestion du décalage éventuel des dates
	 * @param essaimId  l'id de l'essaim à cloner
	 * @param nomclones les noms des clones séparés par des virgules
	 * @param nomruches les noms des ruches pour mettre les clones séparés par des
	 *                  virgules, même nombre d'items que nomclones avec des "" si
	 *                  pas de ruche
	 * @return String liste des essaims créés ou erreur
	 */
	@PostMapping("/clone/{essaimId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String clone(HttpSession session, Model model, @PathVariable long essaimId,
			@RequestParam String nomclones, @RequestParam String nomruches) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			return essaimService.clone(session, essaimOpt, nomclones, nomruches);
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		return messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale());
	}

	/**
	 * Graphe de descendance
	 *
	 * @param essaimId l'id de l'essaim dont on veut tracer le graphe de descendance
	 */
	@GetMapping("/descendance/{essaimId}")
	public String graphedescendance(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			try {
				// Recherche de l'essaim racine du graphe de descendance
				// dans lequel est l'essaim passé en paramètre
				Essaim essaimRoot = essaim;
				while (essaimRoot.getSouche() != null) {
					essaimRoot = essaimRoot.getSouche();
				}
				// Liste des essaims du graphe dans des objets EssaimTree
				List<EssaimTree> essaimTree = essaimService.listeEssaimsFils(essaimRoot);
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				String essaimTreeJson = objectMapper.writeValueAsString(essaimTree);
				model.addAttribute("essaimTree", essaimTreeJson);
			} catch (JsonProcessingException e) {
				logger.error("Erreur parser json : {}", e.getMessage());
				model.addAttribute(Const.MESSAGE, "Erreur parser json");
				return Const.INDEX;
			}
		}
		return "essaim/essaimGraphe";
	}

	/**
	 * Liste des essaims.
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		List<EssaimRucheRucher> eRR = (voirInactif != null && (boolean) voirInactif)
				? essaimRepository.findEssaimRucheRucherOrderByNom()
				: essaimRepository.findEssaimActifRucheRucherOrderByNom();
		List<Evenement> listeEvenCadre = new ArrayList<>();
		for (EssaimRucheRucher e : eRR) {
			listeEvenCadre.add(
					evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(e.essaim(), TypeEvenement.RUCHECADRE));
		}
		model.addAttribute("listeEvenCadre", listeEvenCadre);
		model.addAttribute(ESSAIMSRRR, eRR);
		return "essaim/essaimsListe";
	}

	/**
	 * Appel du formulaire pour l'ajout d'un essaim.
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		// Pour le choix de la souche de l'essaim à créer
		Iterable<IdNom> idNoms = essaimRepository.findAllProjectedIdNomByOrderByNom();
		model.addAttribute(Const.ESSAIMS, idNoms);
		// Liste des noms des essaims pour nom essaim unique
		// en modification il faudra retirer le nom de l'essaim à modifier
		// de cette liste
		List<String> noms = new ArrayList<>();
		for (IdNom idNom : idNoms) {
			noms.add(idNom.nom());
		}
		model.addAttribute(Const.ESSAIMNOMS, noms);
		Essaim essaim = new Essaim();
		essaim.setDateAcquisition(Utils.dateDecal(session));
		model.addAttribute(Const.ESSAIM, essaim);
		return ESSAIM_ESSAIMFORM;
	}

	/**
	 * Appel du formulaire pour modifier un essaim.
	 */
	@GetMapping("/modifie/{essaimId}")
	public String modifie(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			// Pour le choix de la souche de l'essaim à modifier
			Iterable<IdNom> essaimIdNom = essaimRepository.findAllProjectedIdNomByOrderByNom();
			// Liste des noms des essaims pour nom essaim unique
			// on retire le nom de l'essaim à modifier de cette liste
			List<String> noms = new ArrayList<>();
			for (IdNom idNom : essaimIdNom) {
				noms.add(idNom.nom());
			}
			noms.remove(essaim.getNom());
			model.addAttribute(Const.ESSAIMNOMS, noms);
			// Il faut enlever les essaims qui génèrent une boucle de descendance
			// ainsi que l'essaim à modifier pour qu'ils ne soient pas dans la
			// liste des essaims pour le choix de la souche
			List<IdNom> essaimIdNomClean = new ArrayList<>();
			for (IdNom essaimIdNomItem : essaimIdNom) {
				boolean soucheok = true;
				if (essaimIdNomItem.id() != essaimId) {
					Optional<Essaim> soucheOpt = essaimRepository.findById(essaimIdNomItem.id());
					if (soucheOpt.isPresent()) {
						Essaim essaimSouche = soucheOpt.get();
						while (essaimSouche.getSouche() != null) {
							if (essaimSouche.getSouche().getId().equals(essaim.getId())) {
								soucheok = false;
								break;
							}
							essaimSouche = essaimSouche.getSouche();
						}
						if (soucheok) {
							essaimIdNomClean.add(essaimIdNomItem);
						}
					}
				}
			}
			model.addAttribute(Const.ESSAIMS, essaimIdNomClean);
			model.addAttribute(Const.ESSAIM, essaim);
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return ESSAIM_ESSAIMFORM;
	}

	/**
	 * Supprimer un essaim.
	 */
	@GetMapping("/supprime/{essaimId}")
	public String supprime(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			if (recolteHausseRepository.existsByEssaim(essaim)) {
				model.addAttribute(Const.MESSAGE,
						"Cet essaim ne peut être supprimé, il est référencé dans une récolte");
				return Const.INDEX;
			}
			if (evenementRepository.existsByEssaim(essaim)) {
				model.addAttribute(Const.MESSAGE,
						"Cet essaim ne peut être supprimé, il est référencé dans des événements");
				return Const.INDEX;
			}
			Iterable<Evenement> evenements = evenementRepository.findByEssaimId(essaimId);
			// on supprime les événements associées à cette essaim
			for (Evenement evenement : evenements) {
				evenementRepository.delete(evenement);
			}
			// On enlève cet essaim de sa ruche.
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			if (ruche != null) {
				ruche.setEssaim(null);
			}
			essaimRepository.delete(essaim);
			logger.info("{} supprimé", essaim);
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/essaim/liste";
	}

	/**
	 * Enregistrement de l'essaim créé ou modifié.
	 */
	@PostMapping("/sauve")
	public String sauve(Model model, @ModelAttribute Essaim essaim, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ESSAIM_ESSAIMFORM;
		}
		// On enlève les blancs aux extémités du commentaire.
		essaim.setCommentaire(essaim.getCommentaire().trim());
		// On enlève les blancs aux extémités du nom.
		essaim.setNom(essaim.getNom().trim());
		if ("".equals(essaim.getNom())) {
			logger.error("{} nom incorrect.", essaim);
			model.addAttribute(Const.MESSAGE, "Nom d'essaim incorrect.");
			return Const.INDEX;
		}
		// Vérification de l'unicité du nom
		Essaim eNom = essaimRepository.findByNom(essaim.getNom());
		if (eNom != null && !eNom.getId().equals(essaim.getId())) {
			logger.error("{} nom existant.", essaim);
			model.addAttribute(Const.MESSAGE, "Nom d'essaim existant.");
			return Const.INDEX;
		}
		Essaim essaimParent = essaim;
		while (essaimParent.getSouche() != null) {
			if (essaimParent.getSouche().getId().equals(essaim.getId())) {
				logger.info("Essaim {} id {}, erreur cyclique dans l'ascendance", essaim.getNom(), essaim.getId());
				// récupérer la souche avant modif mais pas les autres champs
				Optional<Essaim> essaimOpt = essaimRepository.findById(essaim.getId());
				if (essaimOpt.isPresent()) {
					essaim.setSouche(essaimOpt.get().getSouche());
				}
				return ESSAIM_ESSAIMFORM;
			}
			essaimParent = essaimParent.getSouche();
		}
		String action = (essaim.getId() == null) ? "créé" : "modifié";
		essaimRepository.save(essaim);
		logger.info("{} {}", essaim, action);
		return "redirect:/essaim/" + essaim.getId();
	}

	/**
	 * Afficher un essaim.
	 */
	@GetMapping("/{essaimId}")
	public String affiche(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			model.addAttribute("rucheEssaim", ruche);
			List<String> noms = new ArrayList<>();
			for (Nom essaimNom : essaimRepository.findAllProjectedBy()) {
				noms.add(essaimNom.nom());
			}
			model.addAttribute("essaimnoms", noms);
			List<String> ruchesvidesnoms = new ArrayList<>();
			for (Nom rucheNom : rucheRepository.findAllByEssaimNull()) {
				ruchesvidesnoms.add(rucheNom.nom());
			}
			model.addAttribute("ruchesvidesnoms", ruchesvidesnoms);
			model.addAttribute("eveRuche", evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim,
					TypeEvenement.AJOUTESSAIMRUCHE));
			// On recherche le premier éven RUCHEAJOUTRUCHER référençant l'essaim
			// si on fait la recherche pour la ruche et que la ruche a été mise dans le
			// rucher
			// sans l'essaim initialement, le retour du détail vers l'essaim ne sera pas
			// possible
			// laisser tel quel, c'est mieux pour les cas normaux (ruche + essaim mis en
			// rucher)
			model.addAttribute("eveRucher", evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim,
					TypeEvenement.RUCHEAJOUTRUCHER));
			// Si des hausses de récolte référencent cet essaim, on ne pourra le supprimer
			model.addAttribute("recolteHausses", recolteHausseRepository.existsByEssaim(essaim));
			// Si des événements référencent cet essaim, on ne peut le supprimer
			model.addAttribute(Const.EVENEMENTS, evenementRepository.existsByEssaim(essaim));
			model.addAttribute("eveTraite", evenementRepository.findFirstTraitemenetByEssaim(essaim));
			model.addAttribute("eveSucre",
					evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim, TypeEvenement.ESSAIMSUCRE));
			model.addAttribute("eveComm", evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim,
					TypeEvenement.COMMENTAIREESSAIM));
			model.addAttribute("evePesee",
					evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim, TypeEvenement.RUCHEPESEE));
			model.addAttribute("eveCadre",
					evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim, TypeEvenement.RUCHECADRE));
			// Calcul du poids de miel par récoltes pour cet essaim
			Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
			List<Integer> poidsListe = new ArrayList<>();
			List<Recolte> recoltesListe = new ArrayList<>();
			List<Rucher> rucherRecolteListe = new ArrayList<>();
			Integer poidsTotal = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
				if (poids != null) {
					RecolteHausse rHFirst = recolteHausseRepository.findFirstByRecolteAndEssaim(recolte, essaim);
					Rucher rucher = (rHFirst == null) ? null : rHFirst.getRucher();
					poidsListe.add(poids);
					rucherRecolteListe.add(rucher);
					recoltesListe.add(recolte);
					poidsTotal += poids;
				}
			}
			model.addAttribute("recoltesListe", recoltesListe);
			model.addAttribute("poidsListe", poidsListe);
			model.addAttribute("rucherRecolteListe", rucherRecolteListe);
			model.addAttribute("poidsTotal", poidsTotal);
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "essaim/essaimDetail";
	}

	/**
	 * Afficher page association essaim ruche pour choix de la ruche.
	 */
	@GetMapping("/associe/{essaimId}")
	public String associe(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);

			model.addAttribute("rucheEssaim", ruche);
			model.addAttribute("ruches", ruche == null ? rucheRepository.findByActiveTrueOrderByNom()
					: rucheRepository.findActiveIdDiffOrderByNom(ruche.getId()));

		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "essaim/essaimAssociation";
	}

	/**
	 * Appel du formulaire pour saisie date/commentaire association essaim ruche.
	 *
	 * @rucheId l'id de la ruche dans laquelle on veut mettre l'essaim
	 * @essaimId l'id de l'essaim à mettre dans la ruche
	 */
	@GetMapping("/ruche/associe/{rucheId}/{essaimId}")
	public String associeRuche(HttpSession session, Model model, @PathVariable long rucheId,
			@PathVariable long essaimId) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (rucheOpt.isPresent()) {
			Ruche ruche = rucheOpt.get();
			if (essaimOpt.isPresent()) {
				model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
				model.addAttribute(Const.ESSAIM, essaimOpt.get());
				model.addAttribute(Const.RUCHE, ruche);
				// Si l'essaim est dans une ruche envoyer le nom de la ruche
				// pour proposer l'échange du postionnment des deux ruches
				Ruche rucheSource = rucheRepository.findByEssaimId(essaimOpt.get().getId());
				model.addAttribute("rucheSource", rucheSource);
				if (rucheSource != null) {
					// Envoyer la date max événements ajout ruchers pour imposer une date de
					// l'événement essaim mis dans rucher postérieure à cette date.
					Evenement evenRuche = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
							ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
					Evenement evenRucheSource = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(rucheSource,
							ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
					LocalDateTime dateTime = evenRuche.getDate().isBefore(evenRucheSource.getDate())
							? evenRucheSource.getDate()
							: evenRuche.getDate();
					DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
					model.addAttribute("dateTime", dateTime);
					LocalDateTime dateTimeFirst = dateTime.plusMinutes(1);
					model.addAttribute("dateFirst", dateTimeFirst.format(dateFormat));
					model.addAttribute("timeFirst", dateTimeFirst.format(timeFormat));
				}
			} else {
				logger.error(Const.IDESSAIMXXINCONNU, essaimId);
				model.addAttribute(Const.MESSAGE,
						messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDRUCHEXXINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "essaim/essaimAssociationForm";
	}

	/**
	 * Change un essaim de ruche. Il faut mettre l'essaim essaimId dans le ruche
	 * rucheId. Si la ruche contient un essaim, le disperser.
	 * 
	 * @param date,          la date saisie dans le formulaire
	 * @param commentaire,   le commentaire saisi dans le formulaire
	 * @param swapPositions, true si échange de position des ruches demandé
	 */
	@PostMapping("/ruche/associe/sauve/{rucheId}/{essaimId}")
	public String associeRucheSauve(Model model, @PathVariable long rucheId, @PathVariable long essaimId,
			@RequestParam String date, @RequestParam String commentaire,
			@RequestParam(defaultValue = "false") boolean swapPositions) {
		Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
		if (rucheOpt.isEmpty()) {
			logger.error(Const.IDRUCHEINCONNU, rucheId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHEINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isEmpty()) {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		essaimService.associeRucheSauve(essaimOpt.get(), rucheOpt.get(), date, commentaire, swapPositions);
		return Const.REDIRECT_ESSAIM_ESSAIMID;
	}
}
package ooioo.ruches.essaim;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/essaim")
public class EssaimController {

	private static final String ESSAIM_ESSAIMFORM = "essaim/essaimForm";

	private final Logger logger = LoggerFactory.getLogger(EssaimController.class);

	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;

	@Autowired
	MessageSource messageSource;

	@Autowired
	private EssaimService essaimService;

	/*
	 * Historique de la mise en ruchers d'un essaim. Les événements affichés dans
	 * l'historique : - les mise en rucher de ruches ou l'essaim apparait - la
	 * dispersion de l'essaim qui termine l'historique - la ou les mises en ruches
	 * de l'essaim qui peuvent impliquer des déplacements
	 */
	@GetMapping("/historique/{essaimId}")
	public String historique(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			// la liste de tous les événements RUCHEAJOUTRUCHER concernant cet essaim
			// triés par ordre de date ascendante
			List<Evenement> evensEssaimAjout = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaimId,
					TypeEvenement.RUCHEAJOUTRUCHER);
			// Si l'essaim est dispersé cela termine le séjour dans le dernier rucher
			Evenement dispersion = evenementRepository.findFirstByEssaimAndType(essaim, TypeEvenement.ESSAIMDISPERSION);
			if (dispersion != null) {
				evensEssaimAjout.add(dispersion);
			}
			// Ajouter les mises en ruche
			List<Evenement> miseEnRuche = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(essaim.getId(),
					TypeEvenement.AJOUTESSAIMRUCHE);
			evensEssaimAjout.addAll(miseEnRuche);
			// Trier par date
			evensEssaimAjout.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));
			model.addAttribute("evensEssaimAjout", evensEssaimAjout);
			List<Long> durees = new ArrayList<>();
			if (!evensEssaimAjout.isEmpty()) {
				int i = 0;
				while (i < evensEssaimAjout.size() - 1) {
					// calcul de la durée de séjour dans le rucher
					durees.add(
							Duration.between(evensEssaimAjout.get(i).getDate(), evensEssaimAjout.get(i + 1).getDate())
									.toDays());
					i++;
				}
				durees.add(Duration.between(evensEssaimAjout.get(i).getDate(), LocalDateTime.now()).toDays());
				model.addAttribute("durees", durees);
			}
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "essaim/essaimHisto";
	}

	/*
	 * Statistiques âges des reines
	 */
	@RequestMapping("/statistiquesage")
	public String statistiquesage(Model model) {
		Iterable<Essaim> essaims = essaimRepository.findByActif(true);
		int pas = 6;
		int maxAgeMois = 95; // reine ignorée si plus ancienne (96 mois, 8 ans)
		// ages : classes d'âge de largeur "pas" en mois
		// voir
		// https://stackoverflow.com/questions/7139382/java-rounding-up-to-an-int-using-math-ceil/21830188
		int[] ages = new int[(maxAgeMois + pas - 1) / pas];
		int indexMaxAges = 0;
		long ageMaxJours = 0;
		long ageMinJours = 0;
		boolean premier = true;
		long ageTotalJours = 0;
		int ageMoyenJours;
		int nb = 1;
		LocalDate dateNow = LocalDate.now();
		double m = 0;
		double s = 0;
		for (Essaim essaim : essaims) {
			if (essaim.getReineDateNaissance() != null) {
				if (essaim.getReineDateNaissance().isAfter(dateNow)) {
					// Si la reine n'est pas encore née on ne la prends pas en compte !
					continue;
				}
				long ageMois = ChronoUnit.MONTHS.between(essaim.getReineDateNaissance(), dateNow);
				if (ageMois > maxAgeMois) {
					// Si la reine à plus de maxAgeMois on ne la prends pas en compte
					//  afficher un message en haut de la page de stat
					logger.info("Essaim {}, âge supérieur à {} mois", essaim.getNom(), maxAgeMois);
					continue;
				}
				if (rucheRepository.findByEssaimId(essaim.getId()) == null) {
					// Si la reine n'est pas dans une ruche on ne la prends pas en compte
					continue;
				}
				int indexAge = (int) ageMois / pas;
				ages[indexAge]++;
				indexMaxAges = Math.max(indexMaxAges, indexAge);
				long ageJours = ChronoUnit.DAYS.between(essaim.getReineDateNaissance(), dateNow);
				ageMaxJours = Math.max(ageMaxJours, ageJours);
				if (premier) {
					ageMinJours = ageJours;
					premier = false;
				} else {
					ageMinJours = Math.min(ageMinJours, ageJours);
				}
				ageTotalJours += ageJours;
				// Variance Welford's algorithm
				double tmpM = m;
				double ageJ = ageJours;
				m += (ageJ - tmpM) / nb;
				s += (ageJ - tmpM) * (ageJ - m);
				nb++;
			}
		}
		List<Integer> agesHisto = new ArrayList<>();
		for (int i = 0; i <= indexMaxAges; i++) {
			agesHisto.add(ages[i]);
		}
		ageMoyenJours = (int) Math.round((double) ageTotalJours / (nb - 1));
		// Variance sur population entière (nb est incrémenté avant la sortie de la
		// boucle)
		long ageVarianceJours = Math.round(Math.sqrt(s / (nb - 1)));
		model.addAttribute("ageMoyenJours", ageMoyenJours);
		model.addAttribute("agesHisto", agesHisto);
		model.addAttribute("ageVarianceJours", ageVarianceJours);
		model.addAttribute("ageMaxJours", ageMaxJours);
		model.addAttribute("ageMinJours", ageMinJours);
		model.addAttribute("pas", pas);
		return "essaim/essaimsStatAges";
	}

	/**
	 * Statistiques tableau poids de miel par essaim
	 *  Appel à partir de la liste des essaims
	 *
	 * @param rucherId       optionnel pour ne prendre en compte que les hausses de
	 *                       récolte dans ce rucher
	 * @param masquerInactif pour masquer les essaims inactifs
	 */
	@RequestMapping("/statistiques")
	public String statistiques(Model model, @RequestParam(required = false) Long rucherId,
			@RequestParam(defaultValue = "false") boolean masquerInactif) {
		// pour équivalence appel Get ou Post avec rucherId = 0
		if ((rucherId != null) && rucherId.equals(0L)) {
			rucherId = null;
		}
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		Iterable<Essaim> essaims = masquerInactif ? essaimRepository.findByActif(true) : essaimRepository.findAll();
		List<Map<String, String>> essaimsPoids = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		Integer pTotal; // poids de miel total produit par l'essaim
		Integer pMax; // poids de miel max lors d'une récolte
		Integer pMin; // poids de miel min lors d'une récolte
		boolean rucherOK;
		for (Essaim essaim : essaims) {
			pTotal = 0;
			pMax = 0;
			pMin = 1000000;
			rucherOK = false;
			for (Recolte recolte : recoltes) {
				// si rucherId non null, tester ou était l'essaim pour cette récolte
				// en regardant le rucher dans une des hausseRécolte de cette récolte
				// si différent de rucherId "continue"
				if (rucherId != null) {
					RecolteHausse recoltehausse = recolteHausseRepository.findFirstByRecolteAndEssaim(recolte, essaim);
					if ((recoltehausse == null) || !recoltehausse.getRucher().getId().equals(rucherId)) {
						continue;
					}
				}
				rucherOK = true;
				Integer poids = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
				if (poids != null) {
					pTotal += poids;
					pMax = Math.max(pMax, poids);
					pMin = Math.min(pMin, poids);
				}
			}
			if (pMin == 1000000) {
				pMin = 0;
			}
			// si rucherId non null
			// et rucherOK false ignorer cet essaim, il n'a pas produit dans le rucher
			// rucherId
			if ((rucherId == null) || rucherOK) {
				Map<String, String> essaimPoids = new HashMap<>();
				essaimPoids.put("nom", essaim.getNom());
				essaimPoids.put("id", essaim.getId().toString());
				essaimPoids.put("dateAcquisition", essaim.getDateAcquisition().toString());
				Evenement dispersion = evenementRepository.findFirstByEssaimAndType(essaim,
						TypeEvenement.ESSAIMDISPERSION);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				essaimPoids.put("dateDispersion", (dispersion == null) ? "" : dispersion.getDate().format(formatter));
				// calcul moyenne production miel par jour d'existence de l'essaim
				if (rucherId == null) {
					LocalDateTime dateFin = (dispersion == null) ? LocalDateTime.now() : dispersion.getDate();
					long duree = ChronoUnit.DAYS.between(essaim.getDateAcquisition().atStartOfDay(), dateFin);
					if (duree <= 0) {
						essaimPoids.put("pMoyen", "Erreur durée");
					} else {
						float pMoyen = pTotal * 0.365242f / duree;
						essaimPoids.put("pMoyen", decimalFormat.format(pMoyen));
					}
					essaimPoids.put("duree", Long.toString(duree));
				}
				essaimPoids.put("pTotal", decimalFormat.format(pTotal / 1000.0));
				essaimPoids.put("pMax", decimalFormat.format(pMax / 1000.0));
				essaimPoids.put("pMin", decimalFormat.format(pMin / 1000.0));
				essaimsPoids.add(essaimPoids);
			}
		}
		model.addAttribute("essaimsPoids", essaimsPoids);
		Collection<IdNom> rucherIdNom2 = rucherRepository.findAllProjectedIdNomByOrderByNom();
		List<IdNom> rucherIdNom = new ArrayList<>();
		rucherIdNom.add(new IdNom(0L, "Tous"));
		rucherIdNom.addAll(rucherIdNom2);
		model.addAttribute("rucherIdNom", rucherIdNom);
		model.addAttribute("rucherId", rucherId);
		model.addAttribute("masquerInactif", masquerInactif);
		return "essaim/essaimStatistiques";
	}

	/**
	 * Essaimer appel du formulaire d'essaimage
	 */
	@GetMapping("/essaime/{essaimId}")
	public String essaimer(HttpSession session, Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);
			// if (ruche = null) ?
			List<String> noms = new ArrayList<>();
			for (Nom essaimNom : essaimRepository.findAllProjectedBy()) {
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

	/*
	 * Enregistrement de l'essaimage
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
		// L'essaim à disperser
		Essaim essaim = essaimOpt.get();
		// La ruche dans laquelle on va mettre le nouvel essaim à créer
		Ruche ruche = rucheRepository.findByEssaimId(essaimId);
		LocalDateTime dateEveAjout = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		// On vérifie aussi côté serveur que le nom est libre
		List<String> noms = new ArrayList<>();
		for (Nom essaimNom : essaimRepository.findAllProjectedBy()) {
			noms.add(essaimNom.nom());
		}
		if (noms.contains(nom)) {
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage("LeNomXXExiste", new Object[] { nom }, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		// On crée l'essaim : nom saisi dans le formulaire, date acquisition et
		// naissance reine
		// = date formulaire, souche = essaim dispersé
		Essaim nouvelEssaim = new Essaim(nom, true, // actif
				dateEveAjout.toLocalDate(), // acquisition
				commentaire, // Le champ commentaire du formulaire ? essaim ou événement dispersion ?
				dateEveAjout.toLocalDate(), // reineDateNaissance
				false, // reineMarquee
				essaim, // souche,
				essaim.getAgressivite(), // agressivite
				essaim.getProprete()); // proprete
		essaimRepository.save(nouvelEssaim);
		// On met cet essaim dans la ruche
		ruche.setEssaim(nouvelEssaim);
		Evenement evenementAjout = new Evenement(dateEveAjout, TypeEvenement.AJOUTESSAIMRUCHE, ruche, nouvelEssaim,
				ruche.getRucher(), null, null, commentaire);
		evenementRepository.save(evenementAjout);
		logger.info(Const.EVENEMENTXXENREGISTRE, evenementAjout.getId());
		rucheRepository.save(ruche);
		// On inactive l'essaim dispersé
		essaim.setActif(false);
		essaimRepository.save(essaim);
		// On crée l'événement dispersion
		LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		Evenement evenement = new Evenement(dateEve, TypeEvenement.ESSAIMDISPERSION, ruche, essaim, ruche.getRucher(),
				null, null, commentaire);
		evenementRepository.save(evenement);
		logger.info(Const.EVENEMENTXXENREGISTRE, evenement.getId());
		return "redirect:/essaim/" + nouvelEssaim.getId();
	}

	/**
	 * Clonage multiple d'un essaim (appel XMLHttpRequest de la page détail d'un
	 * essaim) avec mises en ruches éventuelles
	 * 
	 * @param session   pour gestion du décalage éventuel des dates
	 * @param model
	 * @param essaimId  l'id de l'essaim à cloner
	 * @param nomclones les noms des clones séparés par des virgules
	 * @param nomruches les noms des ruches pour mettre les clones séparés par des
	 *                  virgules
	 * @return String liste des essaims créés ou erreur
	 */
	@PostMapping("/clone/{essaimId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String clone(HttpSession session, Model model, @PathVariable long essaimId,
			@RequestParam String nomclones, @RequestParam String nomruches) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			List<String> noms = new ArrayList<>();
			for (Nom essaimNom : essaimRepository.findAllProjectedBy()) {
				noms.add(essaimNom.nom());
			}
			String[] nomarray = nomclones.split(",");
			String[] nomruchesarray = nomruches.split(",");
			List<String> nomsCrees = new ArrayList<>();
			LocalDateTime dateEve = Utils.dateTimeDecal(session);
			for (int i = 0; i < nomarray.length; i++) {
				if (noms.contains(nomarray[i])) {
					logger.error("Clone d'un essaim : {} nom existant", nomarray[i]);
				} else {
					Essaim clone = new Essaim(essaim, nomarray[i]);
					essaimRepository.save(clone);
					nomsCrees.add(nomarray[i]);
					// pour éviter clone "a,a" : 2 fois le même nom dans la liste
					noms.add(nomarray[i]);
					if (i < nomruchesarray.length && !"".contentEquals(nomruchesarray[i])) {
						Ruche ruche = rucheRepository.findByNom(nomruchesarray[i]);
						if (ruche.getEssaim() == null) {
							ruche.setEssaim(clone);
							rucheRepository.save(ruche);
							Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, ruche,
									clone, ruche.getRucher(), null, null, "Clone essaim " + essaim.getNom());
							evenementRepository.save(evenementAjout);
							logger.info(Const.EVENEMENTXXENREGISTRE, evenementAjout.getId());
						} else {
							logger.error("Clone d'un essaim : {} la ruche {} n'est pas vide", nomarray[i],
									nomruchesarray[i]);
						}
					}
				}
			}
			String nomsJoin = String.join(",", nomsCrees);
			logger.info("Essaims {} créé(s)", nomsJoin);
			return messageSource.getMessage("cloneessaimcrees", new Object[] { nomsJoin },
					LocaleContextHolder.getLocale());
		}
		logger.error(Const.IDESSAIMXXINCONNU, essaimId);
		return "Erreur : id essaim inconnu";
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
	 * Liste des essaims
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		if (voirInactif != null && (boolean) voirInactif) {
			model.addAttribute(Const.ESSAIMS, essaimRepository.findAllByOrderByNom());
			// Recherche des ruches et ruchers associés aux essaims
			model.addAttribute(Const.RUCHES, essaimRepository.findRucheIdNomOrderByNom());
		} else {
			model.addAttribute(Const.ESSAIMS, essaimRepository.findByActifOrderByNom(true));
			model.addAttribute(Const.RUCHES, essaimRepository.findRucheIdNomByActifOrderByNom(true));
		}
		return "essaim/essaimsListe";
	}

	/**
	 * Appel du formulaire pour l'ajout d'un essaim
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
	 * Appel du formulaire pour modifier un essaim
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
	 * Supprimer un essaim
	 */
	@GetMapping("/supprime/{essaimId}")
	public String supprime(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Iterable<Evenement> evenements = evenementRepository.findByEssaimId(essaimId);
			List<RecolteHausse> recolteEssaims = recolteHausseRepository.findByEssaimId(essaimId);
			if (recolteEssaims.isEmpty()) {
				// on supprime les événements associées à cette essaim
				for (Evenement evenement : evenements) {
					evenementRepository.delete(evenement);
				}
				// on enlève cet essaim de sa ruche
				Ruche ruche = rucheRepository.findByEssaimId(essaimId);
				if (ruche != null) {
					ruche.setEssaim(null);
				}
				Essaim essaim = essaimOpt.get();
				essaimRepository.delete(essaim);
				logger.info("Essaim {} supprimé, id {}", essaim.getNom(), essaim.getId());
			} else {
				model.addAttribute(Const.MESSAGE, "Cette essaim ne peut être supprimé");
				return Const.INDEX;
			}
		} else {
			logger.error(Const.IDESSAIMXXINCONNU, essaimId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDESSAIMINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/essaim/liste";
	}

	/**
	 * Enregistrement de l'essaim
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Essaim essaim, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ESSAIM_ESSAIMFORM;
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
		essaimRepository.save(essaim);
		logger.info("Essaim {} enregistré, id {}", essaim.getNom(), essaim.getId());
		return "redirect:/essaim/" + essaim.getId();
	}

	/**
	 * Afficher un essaim
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
			//   si on fait la recherche pour la ruche et que la ruche a été mise dans le rucher
			//   sans l'essaim initialement, le retour du détail vers l'essaim ne sera pas possible
			//   laisser tel quel, c'est mieux pour les cas normaux (ruche + essaim mis en rucher)
			model.addAttribute("eveRucher", evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(essaim,
					TypeEvenement.RUCHEAJOUTRUCHER));
			// Si des hausses de récolte référencent cet essaim, on ne pourra la supprimer
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByEssaimId(essaimId);
			model.addAttribute("recolteHausses", recolteHausses.iterator().hasNext());
			// Si des événements référencent cette essaim, il faudra les supprimer si on
			// supprime l'essaim
			Iterable<Evenement> evenements = evenementRepository.findByEssaimId(essaimId);
			model.addAttribute(Const.EVENEMENTS, evenements.iterator().hasNext());
			model.addAttribute("eveTraite", evenementRepository.findFirstTraitemenetByEssaim(essaim.getId(),
					TypeEvenement.ESSAIMTRAITEMENT.ordinal(), TypeEvenement.ESSAIMTRAITEMENTFIN.ordinal()));
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
	 * Afficher page association essaim ruche
	 */
	@GetMapping("/associe/{essaimId}")
	public String associe(Model model, @PathVariable long essaimId) {
		Optional<Essaim> essaimOpt = essaimRepository.findById(essaimId);
		if (essaimOpt.isPresent()) {
			Essaim essaim = essaimOpt.get();
			model.addAttribute(Const.ESSAIM, essaim);
			Ruche ruche = rucheRepository.findByEssaimId(essaimId);

			model.addAttribute("rucheEssaim", ruche);
			model.addAttribute("ruches", ruche == null ? rucheRepository.findByActiveOrderByNom(true)
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
	 * Appel du formulaire pour saisie date/commentaire association essaim ruche
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
					// envoyer la date max événements ajout ruchers
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
	 * Change un essaim de ruche Il faut mettre l'essaim essaimId dans le ruche
	 * rucheId si la ruche contient un essaim, le disperser
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
		Essaim essaim = essaimOpt.get();
		// La ruche dans laquelle on va mettre l'essaim
		Ruche ruche = rucheOpt.get();
		LocalDateTime dateEve = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(Const.YYYYMMDDHHMM));
		// Si la ruche contient un essaim, le disperser
		Essaim essaimDisperse = ruche.getEssaim();
		if (essaimDisperse != null) {
			// On inactive l'essaim dispersé
			essaimDisperse.setActif(false);
			essaimRepository.save(essaimDisperse);
			// On crée l'événement dispersion
			//  quel commentaire ?
			Evenement eveDisperse = new Evenement(dateEve, TypeEvenement.ESSAIMDISPERSION, ruche, essaimDisperse,
					ruche.getRucher(), null, null, commentaire);
			evenementRepository.save(eveDisperse);
			logger.info(Const.EVENEMENTXXENREGISTRE, eveDisperse.getId());
		}
		Rucher rucher = ruche.getRucher();
		// La ruche dans laquelle est l'essaim
		Ruche rucheActuelle = rucheRepository.findByEssaimId(essaimId);
		if (rucheActuelle != null) {
			if (swapPositions) {
				Float lat = ruche.getLatitude();
				Float lon = ruche.getLongitude();
				ruche.setRucher(rucheActuelle.getRucher());
				ruche.setLatitude(rucheActuelle.getLatitude());
				ruche.setLongitude(rucheActuelle.getLongitude());
				rucheRepository.save(ruche);
				rucheActuelle.setRucher(rucher);
				rucheActuelle.setLatitude(lat);
				rucheActuelle.setLongitude(lon);
				// création de deux événements rucherajouterucher si les ruchers sont différents
				// on peut avoir demandé d'échanger les positions des ruches alors qu'elles sont
				// dans
				// les mêmes ruchers !
				if (!rucheActuelle.getRucher().getId().equals(ruche.getRucher().getId())) {
					Evenement eveRuche = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER, ruche,
							ruche.getEssaim(), ruche.getRucher(), null, null, commentaire);
					evenementRepository.save(eveRuche);
					logger.info(Const.EVENEMENTXXENREGISTRE, eveRuche.getId());
					Evenement eveRucheActuelle = new Evenement(dateEve.minusSeconds(1), TypeEvenement.RUCHEAJOUTRUCHER,
							rucheActuelle, rucheActuelle.getEssaim(), rucheActuelle.getRucher(), null, null,
							commentaire);
					evenementRepository.save(eveRucheActuelle);
					logger.info(Const.EVENEMENTXXENREGISTRE, eveRucheActuelle.getId());
				}
			}
			rucheActuelle.setEssaim(null);
			rucheRepository.save(rucheActuelle);
		}
		ruche.setEssaim(essaimOpt.get());
		rucheRepository.save(ruche);
		// on met dans l'événement le rucher ruche.getRucher car la position des ruches
		// a pu être échangée
		Evenement evenementAjout = new Evenement(dateEve, TypeEvenement.AJOUTESSAIMRUCHE, ruche, essaim,
				ruche.getRucher(), null, null, commentaire); // valeur commentaire
		evenementRepository.save(evenementAjout);
		return Const.REDIRECT_ESSAIM_ESSAIMID;
	}
}
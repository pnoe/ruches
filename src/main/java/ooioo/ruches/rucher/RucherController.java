package ooioo.ruches.rucher;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.LatLon;
import ooioo.ruches.Nom;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;
import ooioo.ruches.hausse.HausseRepository;
import ooioo.ruches.personne.PersonneRepository;
import ooioo.ruches.recolte.Recolte;
import ooioo.ruches.recolte.RecolteHausseRepository;
import ooioo.ruches.recolte.RecolteRepository;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.ruche.RucheParcours;
import ooioo.ruches.ruche.RucheRepository;

@Controller
@RequestMapping("/rucher")
public class RucherController {

	private static final String RUCHER_RUCHERFORM = "rucher/rucherForm";
	private static final String RUCHER_RUCHERLISTE = "rucher/ruchersListe";
	private static final String HISTO = "histo";

	private final Logger logger = LoggerFactory.getLogger(RucherController.class);

	@Autowired
	private RucherRepository rucherRepository;
	@Autowired
	private EssaimRepository essaimRepo;
	@Autowired
	private RucheRepository rucheRepository;
	@Autowired
	private HausseRepository hausseRepository;
	@Autowired
	private PersonneRepository personneRepository;
	@Autowired
	private EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private RucherService rucherService;
	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private DistRucherRepository drRepo;

	@Autowired
	private MessageSource messageSource;

	@Value("${rucher.butinage.rayons}")
	private int[] rayonsButinage;
	@Value("${ign.carteIGN.license}")
	private boolean ignCarteLiscense;
	@Value("${ruche.dist.max}")
	private float distMaxRuche;

	/**
	 * Calcul des distances entre les ruchers par appel de l'api ign de calcul
	 * d'itinéraire.
	 */
	@GetMapping(path = "/dist")
	public String dist(Model model, @RequestParam(required = false) boolean reset) {
		rucherService.dist(reset);
		model.addAttribute(Const.MESSAGE, "Calcul des distances terminé.");
		return Const.INDEX;
	}

	/**
	 * Graphe affichant les poids des ruches d'un rucher.
	 */
	@GetMapping("/poids/{rucherId}")
	public String poids(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// Traite les essaims du rucher
			Iterable<Essaim> essaims = essaimRepo.findByRucherId(rucherId);
			List<Essaim> essaimsPeses = new ArrayList<>();
			List<List<Long>> dates = new ArrayList<>();
			List<List<Float>> poids = new ArrayList<>();
			List<Ruche> ruches = new ArrayList<>();
			for (Essaim ess : essaims) {
				List<Evenement> evesPesee = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(ess.getId(),
						TypeEvenement.RUCHEPESEE);
				if (evesPesee.isEmpty()) {
					continue;
				}
				List<Long> d = new ArrayList<>();
				List<Float> p = new ArrayList<>();
				for (Evenement e : evesPesee) {
					d.add(e.getDate().toEpochSecond(ZoneOffset.UTC));
					p.add(Float.parseFloat(e.getValeur()));
				}
				essaimsPeses.add(ess);
				dates.add(d);
				poids.add(p);
				Ruche ruche = rucheRepository.findByEssaimId(ess.getId());
				ruches.add(ruche);
			}
			model.addAttribute("dates", dates);
			model.addAttribute("poids", poids);
			model.addAttribute("essaims", essaimsPeses);
			model.addAttribute("ruches", ruches);
			model.addAttribute("rucher", rucher);
			return "rucher/rucherPoids";
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
	}

	/**
	 * Appel du formulaire de saisie tabulaire du poids des ruches d'un rucher.
	 */
	@GetMapping("/poidsruches/{rucherId}")
	public String poidsruches(HttpSession session, Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			model.addAttribute(Const.RUCHES, ruches);

			List<Evenement> evesPesee = new ArrayList<>();
			for (Ruche r : ruches) {
				evesPesee.add((r.getEssaim() == null) ? null
						: evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(r.getEssaim(),
								TypeEvenement.RUCHEPESEE));
			}
			model.addAttribute("evesPesee", evesPesee);
			model.addAttribute("date", Utils.dateTimeDecal(session));
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherPoidsForm";
	}

	/**
	 * Sauve la saisie tabulaire des poids des ruches d'un rucher. Récupère la liste
	 * id ruche, poids ruche, commentaire pour créer des événements Poids Ruche.
	 */
	@PostMapping("/poidsruches/sauve/{rucherId}")
	public String poidsruchesSauve(Model model, @ModelAttribute IdValeurList eve, BindingResult bindingResult,
			@PathVariable long rucherId) {
		if (bindingResult.hasErrors()) {
			logger.error("Erreur formulaire poids ruches, rucher {}", rucherId);
			model.addAttribute(Const.MESSAGE, "Erreur formulaire poids ruches");
			return Const.INDEX;
		}
		List<String> ruches = new ArrayList<>();
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			// On enlève les blancs aux extémités du commentaire.
			eve.setCommentaire(eve.getCommentaire().trim());
			for (IdValeur idVal : eve.getIdValLst()) {
				if (!Utils.isNum(idVal.getValeur()) || (idVal.getId() == null) || (!rucher.getId().equals(rucherId))
						|| (eve.getDate() == null)) {
					// si le champ valeur n'est pas un numérique, ou l'id de la rucher est null,
					// ou la ruche n'appartient pas au rucher, ou la date est null on ignore la
					// ligne
					continue;
				}
				Optional<Ruche> rucheOpt = rucheRepository.findById(idVal.getId());
				if (rucheOpt.isPresent()) {
					Ruche ruche = rucheOpt.get();
					Evenement evenement = new Evenement(eve.getDate(), TypeEvenement.RUCHEPESEE, ruche,
							ruche.getEssaim(), ruche.getRucher(), null, idVal.getValeur(), eve.getCommentaire());
					evenementRepository.save(evenement);
					logger.info("{} créé", evenement);
					ruches.add(ruche.getId().toString());
				}
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE, Const.IDRUCHERINCONNU);
			return Const.INDEX;
		}
		return "redirect:/rucher/" + rucherId + "?ruchespesee=" + String.join("-", ruches);
	}

	/**
	 * Affiche les distances et temps de déplacement en voiture du rucher rucherId
	 * aux autres ruchers actifs. Affiche aussi les distances à vol d'oiseau.
	 */
	@GetMapping("/dists/{rucherId}")
	public String dists(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			List<Float> dist = new ArrayList<>();
			List<LocalTime> temps = new ArrayList<>();
			List<Double> distOiseau = new ArrayList<>();
			List<Rucher> rrs = new ArrayList<>();
			List<Rucher> ruchers = rucherRepository.findByActifTrue();
			for (Rucher rr : ruchers) {
				if (rr.getId().equals(rucherId)) {
					continue;
				}
				rrs.add(rr);
				// lecture en base de la distance et du temps pour aller
				// à ce rucher.
				DistRucher dr = (rr.getId().intValue() > rucher.getId().intValue())
						? drRepo.findByRucherStartAndRucherEnd(rucher, rr)
						: drRepo.findByRucherStartAndRucherEnd(rr, rucher);
				if (dr == null) {
					dist.add(0f);
					temps.add(null);
				} else {
					dist.add(dr.getDist() / 1000f);
					temps.add(LocalTime.MIN.plus(Duration.ofMinutes(dr.getTemps())));
				}
				double diametreTerre = ((rr.getAltitude() == null) ? 0 : rr.getAltitude())
						+ 2 * Utils.rTerreLat(rucher.getLatitude());
				distOiseau.add(Utils.distance(diametreTerre, rr.getLatitude(), rucher.getLatitude(), rr.getLongitude(),
						rucher.getLongitude()) / 1000f);
			}
			model.addAttribute("dist", dist);
			model.addAttribute("temps", temps);
			model.addAttribute("distOiseau", distOiseau);
			model.addAttribute("rucher", rucher);
			model.addAttribute("rrs", rrs);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherDists";
	}

	/**
	 * Transhumances d'un rucher.
	 *
	 * Les événements sont utilisés par date décroissante : du plus récent au plus
	 * ancien. Liste les événements correspondant à l'ajout ou au retrait de ruches
	 * dans ce rucher. Option "Grouper" pour grouper les événements de même date
	 * (année, mois et jour) et de même type (Ajout/Retrait).
	 *
	 * @param model : on ajoute au model "histo" ou "histoGroup" la liste des
	 *              événements, "group" true si regroupement fait, "rucher" l'objet
	 *              rucher (titre, lien)
	 * @param group : si true les événements de même jour sont regroupés
	 */
	@GetMapping("/historique/{rucherId}/{group}")
	public String historique(Model model, @PathVariable long rucherId, @PathVariable boolean group) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// evenementRepository.findAjoutRucheOK
			// la liste de tous les événements RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null.
			List<Transhumance> histoAll = new ArrayList<>(); // si non groupées
			List<Transhumance> histoGroup = new ArrayList<>(); // si groupées
			rucherService.transhum(rucher, evenementRepository.findAjoutRucheOK(), group, histoAll, histoGroup);
			model.addAttribute(HISTO, group ? histoGroup : histoAll);
			model.addAttribute("group", group);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherHisto";
	}

	/**
	 * Transhumances de tous les ruchers.
	 */
	@GetMapping("/historiques/{group}")
	public String historiques(Model model, @PathVariable boolean group) {
		List<Rucher> ruchers = rucherRepository.findByActifTrue();
		List<Transhumance> histoAll = new ArrayList<>(); // si non groupés
		List<Transhumance> histoGroup = new ArrayList<>(); // si groupés
		for (Rucher rucher : ruchers) {
			if (rucher.getDepot()) {
				continue;
			}
			List<Transhumance> histo = new ArrayList<>();
			// evenementRepository.findAjoutRucheOK() la liste de tous les événements
			// RUCHEAJOUTRUCHER triés par ordre de date
			// descendante avec les champs ruche et rucher non null.
			// On ne peut exclure le dépôt qui sert pour trouver les retraits d'un
			// rucher vers le dépôt.
			rucherService.transhum(rucher, evenementRepository.findAjoutRucheOK(), group, histo, histoGroup);
			if (!group) {
				histoAll.addAll(histo);
			}
		}
		// On trie la liste à afficher par date et on l'ajoute au model.
		if (group) {
			Collections.sort(histoGroup, (b, a) -> a.date().compareTo(b.date()));
			model.addAttribute(HISTO, histoGroup);
		} else {
			Collections.sort(histoAll, (b, a) -> a.date().compareTo(b.date()));
			model.addAttribute(HISTO, histoAll);
		}
		model.addAttribute("group", group);
		return "rucher/ruchersHisto";
	}

	/**
	 * Statistiques tableau poids de miel par rucher.
	 */
	@GetMapping("/statistiques")
	public String statistiques(Model model) {
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		Iterable<Rucher> ruchers = rucherRepository.findAll();
		List<Map<String, String>> ruchersPoids = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		Integer pTotal; // poids de miel total produit par le rucher
		Integer pMax; // poids de miel max lors d'une récolte
		Integer pMin; // poids de miel min lors d'une récolte
		int nbRecoltes;
		for (Rucher rucher : ruchers) {
			pTotal = 0;
			pMax = 0;
			pMin = Integer.MAX_VALUE;
			nbRecoltes = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByRucherByRecolte(rucher.getId(), recolte.getId());
				if (poids != null) {
					nbRecoltes++;
					pTotal += poids;
					pMax = Math.max(pMax, poids);
					pMin = Math.min(pMin, poids);
				}
			}
			if (pMin == Integer.MAX_VALUE) {
				pMin = 0;
			}
			Map<String, String> rucherPoids = new HashMap<>();
			rucherPoids.put("nom", rucher.getNom());
			rucherPoids.put("id", rucher.getId().toString());
			rucherPoids.put("pTotal", decimalFormat.format(pTotal / 1000.0));
			rucherPoids.put("pMax", decimalFormat.format(pMax / 1000.0));
			rucherPoids.put("pMin", decimalFormat.format(pMin / 1000.0));
			if (nbRecoltes <= 0) {
				rucherPoids.put("pMoyen", "");
			} else {
				float pMoyen = pTotal / (float) nbRecoltes;
				rucherPoids.put("pMoyen", decimalFormat.format(pMoyen / 1000));
			}
			rucherPoids.put("nbRecoltes", Integer.toString(nbRecoltes));
			ruchersPoids.add(rucherPoids);
		}
		model.addAttribute("ruchersPoids", ruchersPoids);
		return "rucher/rucherStatistiques";
	}

	/**
	 * Liste des ruchers.
	 */
	@GetMapping("/liste")
	public String liste(HttpSession session, Model model) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		List<Rucher> ruchers;
		if (voirInactif != null && (boolean) voirInactif) {
			ruchers = rucherRepository.findAllByOrderByNom();
		} else {
			ruchers = rucherRepository.findByActifOrderByNom(true);
		}
		int nbr = ruchers.size();
		Collection<Long> nbRuches = new ArrayList<>(nbr);
		Collection<Integer> nbHausses = new ArrayList<>(nbr);
		for (Rucher rucher : ruchers) {
			nbRuches.add(rucheRepository.countByRucher(rucher));
			// si rucher.depot == true alors ajouter les hausses ou hausse.ruche == null
			Integer nb = hausseRepository.countHausseInRucher(rucher.getId());
			if (rucher.getDepot()) {
				// on ajoute les hausses qui ne sont pas sur des ruches
				// à la somme pour le dépôt
				nb += hausseRepository.countByActiveAndRucheIsNull(true);
			}
			nbHausses.add(nb);
		}
		model.addAttribute(Const.RUCHERS, ruchers);
		model.addAttribute("nbRuches", nbRuches);
		model.addAttribute(Const.NBHAUSSES, nbHausses);
		return RUCHER_RUCHERLISTE;
	}

	/**
	 * Appel du formulaire de création d'un rucher. Les coordonnées du nouveau
	 * rucher sont dans un cercle de rayon "dispersion" du dépôt. Le contact est
	 * celui du dépôt.
	 */
	@GetMapping("/cree")
	public String cree(Model model) {
		List<Nom> nomsRecords = rucherRepository.findAllProjectedBy();
		List<String> noms = new ArrayList<>(nomsRecords.size());
		for (Nom rucherNom : nomsRecords) {
			noms.add(rucherNom.nom());
		}
		model.addAttribute(Const.RUCHERNOMS, noms);
		Rucher rucher = new Rucher();
		// Récupération des coordonnées du dépôt
		Rucher rucherDepot = rucherRepository.findByDepotTrue();
		LatLon latLon = rucherService.dispersion(rucherDepot.getLatitude(), rucherDepot.getLongitude());
		rucher.setLatitude(latLon.lat());
		rucher.setLongitude(latLon.lon());
		rucher.setContact(rucherDepot.getContact());
		model.addAttribute(Const.RUCHER, rucher);
		model.addAttribute(Const.PERSONNES, personneRepository.findAll());
		return RUCHER_RUCHERFORM;
	}

	/**
	 * Appel du formulaire de modification d'un rucher.
	 */
	@GetMapping("/modifie/{rucherId}")
	public String modifie(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHERNOMS, rucherRepository.findAllProjectedBy().stream().map(Nom::nom)
					.filter(nom -> !nom.equals(rucher.getNom())).toList());
			model.addAttribute(Const.RUCHER, rucher);
			model.addAttribute(Const.PERSONNES, personneRepository.findAll());
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return RUCHER_RUCHERFORM;
	}

	/**
	 * Enregistrement du rucher créé ou modifié.
	 */
	@PostMapping("/sauve")
	public String sauve(Model model, @ModelAttribute Rucher rucher, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RUCHER_RUCHERFORM;
		}
		// On enlève les blancs aux extémités du commentaire.
		rucher.setCommentaire(rucher.getCommentaire().trim());
		// On enlève les blancs aux extémités du nom.
		rucher.setNom(rucher.getNom().trim());
		if ("".equals(rucher.getNom())) {
			logger.error("{} nom incorrect.", rucher);
			model.addAttribute(Const.MESSAGE, "Nom de rucher incorrect.");
			return Const.INDEX;
		}

		// Vérification de l'unicité du nom
		Rucher rNom = rucherRepository.findByNom(rucher.getNom());
		if (rNom != null && !rNom.getId().equals(rucher.getId())) {
			logger.error("{} nom existant.", rucher);
			model.addAttribute(Const.MESSAGE, "Nom de rucher existant.");
			return Const.INDEX;
		}
		String action = (rucher.getId() == null) ? "créé" : "modifié";
		rucherRepository.save(rucher);
		logger.info("{} {}", rucher, action);
		return "redirect:/rucher/" + rucher.getId();
	}

	/**
	 * Afficher un rucher et ses ruches.
	 */
	@GetMapping("/{rucherId}")
	public String affiche(Model model, @PathVariable long rucherId,
			@RequestParam(required = false) String ruchespesee) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// Liste des ruches de ce rucher.
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			model.addAttribute(Const.RUCHES, ruches);
			// si rucher.depot == true alors ajouter les hausses ou hausse.ruche == null
			Integer nbHausses = hausseRepository.countHausseInRucher(rucherId);
			if (rucher.getDepot()) {
				// On ajoute les hausses qui ne sont pas sur des ruches
				// à la somme pour le dépôt.
				nbHausses += hausseRepository.countByActiveAndRucheIsNull(true);
			} else {
				// Lecture en base de la distance et du temps pour aller du dépôt
				// à ce rucher.
				Rucher depot = rucherRepository.findByDepotTrue();
				DistRucher dr = (depot.getId().intValue() > rucher.getId().intValue())
						? drRepo.findByRucherStartAndRucherEnd(rucher, depot)
						: drRepo.findByRucherStartAndRucherEnd(depot, rucher);
				if (dr != null) {
					// distance en km
					model.addAttribute("dist", dr.getDist() / 1000.);
					// temps en h et min
					model.addAttribute("temps", LocalTime.MIN.plus(Duration.ofMinutes(dr.getTemps())));
				}
			}
			model.addAttribute("nbHausses", nbHausses);
			List<Integer> listNbHausses = new ArrayList<>();
			// Nonmbre de cadres dans le dernier événement cadre
			List<Evenement> listeEvenCadre = new ArrayList<>();
			for (Ruche ruche : ruches) {
				// Compte du nombre de hausses de chaque ruche
				listNbHausses.add(hausseRepository.countByRucheId(ruche.getId()));
				listeEvenCadre.add(ruche.getEssaim() == null ? null
						: evenementRepository.findFirstByEssaimAndTypeOrderByDateDesc(ruche.getEssaim(),
								TypeEvenement.RUCHECADRE));
			}
			model.addAttribute("listeEvenCadre", listeEvenCadre);
			model.addAttribute("listNbHausses", listNbHausses);
			// Si des hausses de récolte référencent ce rucher, on ne pourra le supprimer
			model.addAttribute("recolteHausses", recolteHausseRepository.existsByRucher(rucher));
			// Si des événements référencent ce rucher, on ne peut le supprimer
			model.addAttribute(Const.EVENEMENTS, evenementRepository.existsByRucher(rucher));
			// Calcul du poids de miel par récoltes pour ce rucher.
			List<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
			// Seules les récoltes du rucher sont ajoutées aux ArrayList.
			List<Integer> poidsListe = new ArrayList<>();
			List<Recolte> recoltesListe = new ArrayList<>();
			List<Integer> recoltesNbRuches = new ArrayList<>();
			Integer poidsTotal = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByRucherByRecolte(rucher.getId(), recolte.getId());
				if (poids != null) {
					recoltesListe.add(recolte);
					poidsListe.add(poids);
					// nombre de ruche du rucher rucherId dans cette récolte
					recoltesNbRuches
							.add(recolteHausseRepository.countRucheByRecolteByRucher(recolte.getId(), rucherId));
					poidsTotal += poids;
				}
			}
			model.addAttribute("recoltesListe", recoltesListe);
			model.addAttribute("recoltesNbRuches", recoltesNbRuches);
			model.addAttribute("poidsListe", poidsListe);
			model.addAttribute("poidsTotal", poidsTotal);
			if (ruchespesee != null) {
				if ("".equals(ruchespesee)) {
					model.addAttribute("ruchespesee", "");
				} else {
					List<String> rs = new ArrayList<>();
					for (String r : ruchespesee.split("-")) {
						Optional<Ruche> rucheopt = rucheRepository.findById(Long.valueOf(r));
						if (rucheopt.isPresent()) {
							rs.add(rucheopt.get().getNom());
						}
					}
					model.addAttribute("ruchespesee", String.join(", ", rs));
				}
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherDetail";
	}

	/**
	 * Rapprocher les ruches trop éloignées de leur rucher. Utilise ruche.dist.max
	 * comme distance max ruche - entrée du rucher. Rapproche les ruches à une
	 * distance inférieure à rucher.ruche.dispersion de l'entrée.
	 */
	@GetMapping({ "/rapproche/Ign/{rucherId}", "/rapproche/Osm/{rucherId}" })
	public String rapproche(Model model, @PathVariable long rucherId, HttpServletRequest request) {
		String servletPath = request.getServletPath().split("/")[3];
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			Float longRucher = rucher.getLongitude();
			Float latRucher = rucher.getLatitude();
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			double diametreTerre = ((rucher.getAltitude() == null) ? 0 : rucher.getAltitude())
					+ 2 * Utils.rTerreLat(rucher.getLatitude());
			for (Ruche ruche : ruches) {
				if (Utils.distance(diametreTerre, latRucher, ruche.getLatitude(), longRucher,
						ruche.getLongitude()) > distMaxRuche) {
					// On calcule un point près de l'entrée du rucher
					LatLon latLon = rucherService.dispersion(rucher.getLatitude(), rucher.getLongitude());
					// On met la ruche à ce point
					ruche.setLatitude(latLon.lat());
					ruche.setLongitude(latLon.lon());
					rucheRepository.save(ruche);
					logger.info("{} repositionnée", ruche);
				}
			}
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/" + servletPath + "/{rucherId}";
	}

	/**
	 * Appel du template pour l'ajout de ruches dans un rucher
	 */
	@GetMapping("/ruches/{rucherId}")
	public String afficheRuches(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
			// liste des ruches de ce rucher
			Iterable<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			// liste des ruches sur un autre rucher
			Iterable<Ruche> ruchesNot = rucheRepository.findByRucherIdNotOrderByNom(rucherId);
			model.addAttribute(Const.RUCHES, ruches);
			model.addAttribute("ruchesNot", ruchesNot);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherAjout";
	}

	/**
	 * Suppression d'un rucher. On ne peut pas supprimer le dépôt.
	 */
	@Transactional
	// Transactionnal nécessaire pour deleteDists
	@GetMapping("/supprime/{rucherId}")
	public String supprime(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			if (rucher.getDepot()) {
				model.addAttribute(Const.MESSAGE, "Le rucher dépôt ne peut être supprimé");
				return Const.INDEX;
			}
			// On interdit la suppression d'un rucher référencé dans une récolte
			if (recolteHausseRepository.existsByRucher(rucher)) {
				model.addAttribute(Const.MESSAGE, "Ce rucher ne peut être supprimé, il est référencé dans une récolte");
				return Const.INDEX;
			}
			// On interdit la suppression d'un rucher référencé dans des événements
			// if (evenementRepository.countByRucher(rucher) != 0) {
			if (evenementRepository.existsByRucher(rucher)) {
				model.addAttribute(Const.MESSAGE,
						"Ce rucher ne peut être supprimé, il est référencé dans des événements");
				return Const.INDEX;
			}
			// On interdit la suppression du rucher s'il contient des ruches.
			// Il faut mettre au préalable ces ruches dans un autre rucher.
			if (rucheRepository.countByRucher(rucher) != 0) {
				model.addAttribute(Const.MESSAGE, "Ce rucher ne peut être supprimé, il contient des ruches");
				return Const.INDEX;
			}
			// On supprime les distances vers ce rucher.
			drRepo.deleteDists(rucher);
			rucherRepository.delete(rucher);
			logger.info("{} supprimé", rucher);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/liste";
	}

	/**
	 * Affiche la carte avec les ruches du rucher rucherId. Gg google maps, Ign ou
	 * Osm OpenStreetMap.
	 */
	@GetMapping({ "/Gg/{rucherId}", "/Ign/{rucherId}", "/Osm/{rucherId}" })
	public String rucheMap(Model model, @PathVariable long rucherId, HttpServletRequest request) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			List<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			List<RucheParcours> cheminRet = new ArrayList<>(ruches.size() + 2);
			double retParcours = rucherService.cheminRuchesRucher(cheminRet, rucher, ruches, false);
			model.addAttribute("distParcours", retParcours);
			model.addAttribute("rucheParcours", cheminRet);
			List<String> nomHausses = new ArrayList<>();
			// Pour calcul du barycentre des ruches, centre des cercles de butinage
			Float longitude;
			Float latitude = 0f;
			int nbRuches = 0;
			double xlon = 0d;
			double ylon = 0d;
			for (Ruche ruche : ruches) {
				nomHausses.add(hausseRepository.findByRucheId(ruche.getId()).stream().map(Nom::nom)
						.reduce("", (a, b) -> a + " " + b).trim());
				nbRuches++;
				Float longrad = (float) (ruche.getLongitude() * Math.PI / 180.0d);
				xlon += Math.cos(longrad);
				ylon += Math.sin(longrad);
				latitude += ruche.getLatitude();
			}
			if (nbRuches != 0) {
				longitude = (float) (Math.atan2(ylon, xlon) * 180d / Math.PI);
				latitude /= nbRuches;
			} else {
				longitude = rucher.getLongitude();
				latitude = rucher.getLatitude();
			}
			model.addAttribute("longitudeCentre", longitude);
			model.addAttribute("latitudeCentre", latitude);
			model.addAttribute("rayonsButinage", rayonsButinage);
			model.addAttribute(Const.HAUSSENOMS, nomHausses);
			model.addAttribute(Const.RUCHER, rucher);
			model.addAttribute(Const.RUCHES, ruches);
			model.addAttribute("ignCarteLiscense", ignCarteLiscense);
			model.addAttribute("distMaxRuche", distMaxRuche);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherDetail" + request.getServletPath().split("/")[2];
	}

	/**
	 * Affiche la carte de tous les ruchers. Gg google maps, Ign ou Osm
	 * OpenStreetMap.
	 */
	@GetMapping({ "/Gg", "/Ign", "/Osm" })
	public String rucherMap(HttpSession session, Model model, HttpServletRequest request) {
		Object voirInactif = session.getAttribute(Const.VOIRINACTIF);
		Iterable<Rucher> ruchers;
		if (voirInactif != null && (boolean) voirInactif) {
			ruchers = rucherRepository.findAllByOrderByNom();
		} else {
			ruchers = rucherRepository.findByActifOrderByNom(true);
		}
		List<String> nomRuches = new ArrayList<>();
		for (Rucher rucher : ruchers) {
			nomRuches.add(rucheRepository.findByRucherId(rucher.getId()).stream().map(Nom::nom)
					.reduce("", (a, b) -> a + " " + b).trim());
		}
		model.addAttribute(Const.RUCHENOMS, nomRuches);
		model.addAttribute(Const.RUCHERS, ruchers);
		model.addAttribute("ignCarteLiscense", ignCarteLiscense);
		return RUCHER_RUCHERLISTE + request.getServletPath().split("/")[2];
	}

	/**
	 * Appel du formulaire pour ajouter une liste de ruches dans un rucher. Ajoute à
	 * model : dateTime, la date du dernier ajout des ruches de la liste. dateFirst
	 * et timeFirst, la date et les heures minutes de la date ci-dessus plus 1
	 * minute. ruchesNoms la liste des noms des ruches séparés par une virgule. rIds
	 * la liste des ids des ruches séparés par une virgule.
	 */
	@GetMapping("/ruches/ajouter/{rucherId}/{ruchesIds}")
	public String ajouterRuches(HttpSession session, Model model, @PathVariable long rucherId,
			@PathVariable Long[] ruchesIds) {
		// le formulaire affiche le nom du rucher et la liste des noms de ruche
		// il permet le choix de la date et du commentaire
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			model.addAttribute(Const.DATE, Utils.dateTimeDecal(session));
			model.addAttribute(Const.RUCHER, rucherOpt.get());
			// On cherche la date du dernier événement RUCHEAJOUTRUCHER
			// pour imposer cette date comme min dans le datetimecalendar et
			// dans la règle de validation jquery.
			LocalDateTime dateTimeMin = LocalDateTime.MIN;
			StringBuilder ruchesNoms = new StringBuilder();
			StringBuilder rIds = new StringBuilder();
			for (Long rucheId : ruchesIds) {
				Optional<Ruche> rucheOpt = rucheRepository.findById(rucheId);
				if (rucheOpt.isPresent()) {
					Ruche ruche = rucheOpt.get();
					ruchesNoms.append(ruche.getNom() + ",");
					rIds.append(ruche.getId() + ",");
					Evenement evenFirst = evenementRepository.findFirstByRucheAndTypeOrderByDateDesc(ruche,
							ooioo.ruches.evenement.TypeEvenement.RUCHEAJOUTRUCHER);
					if (dateTimeMin.isBefore(evenFirst.getDate())) {
						dateTimeMin = evenFirst.getDate();
					}
				} else {
					// on continue le traitement des autres ruches
					logger.error(Const.IDRUCHEXXINCONNU, rucheId);
				}
			}
			model.addAttribute("dateTime", dateTimeMin);
			model.addAttribute("dateFirst", dateTimeMin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			model.addAttribute("timeFirst", dateTimeMin.format(DateTimeFormatter.ofPattern("HH:mm")));
			ruchesNoms.deleteCharAt(ruchesNoms.length() - 1);
			rIds.deleteCharAt(rIds.length() - 1);
			model.addAttribute("ruchesNoms", ruchesNoms);
			model.addAttribute("rIds", rIds);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherRuchesAjoutForm";
	}

	/**
	 * Ajoute une liste de ruches dans un rucher. Création de l'événement
	 * RUCHEAJOUTRUCHER par ruche
	 */
	@PostMapping("/ruches/ajouter/sauve/{rucherId}/{ruchesIds}")
	public String sauveAjouterRuches(Model model, @PathVariable long rucherId, @PathVariable Long[] ruchesIds,
			@RequestParam String date, @RequestParam String commentaire) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucherService.sauveAjouterRuches(rucher, ruchesIds, date, commentaire);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "redirect:/rucher/ruches/" + rucherId;
	}

	/**
	 * Déplace un rucher (appel xmlhttp).
	 */
	@PostMapping("/deplace/{rucherId}/{lat}/{lng}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String deplace(@PathVariable Long rucherId, @PathVariable Float lat, @PathVariable Float lng) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucher.setLatitude(lat);
			rucher.setLongitude(lng);
			rucherRepository.save(rucher);
			logger.info("{} déplacé", rucher);
			return "OK";
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		return "Id rucher inconnu";

	}

	/**
	 * Météo d'un rucher
	 *
	 * https://github.com/Prominence/openweathermap-java-api/blob/master/docs/SNAPSHOT.md
	 * https://openweathermap.org/api/one-call-api https://openweathermap.org/api
	 * http://api.openweathermap.org/data/2.5/onecall?lat=43.4900093&lon=5.49108076&APPID=xxxx
	 *
	 */
	@GetMapping("/meteo/{rucherId}")
	public String meteo(Model model, @PathVariable long rucherId) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			model.addAttribute(Const.RUCHER, rucher);
		} else {
			logger.error(Const.IDRUCHERXXINCONNU, rucherId);
			model.addAttribute(Const.MESSAGE,
					messageSource.getMessage(Const.IDRUCHERINCONNU, null, LocaleContextHolder.getLocale()));
			return Const.INDEX;
		}
		return "rucher/rucherMeteo";
	}

	/**
	 * Calcul du parcours des ruches d'un rucher (appel XMLHttpRequest). redraw = 0
	 * recalcul si l'utilisateur déplace une ruche sur la carte redraw = 1 recalcul
	 * si l'utilisateur le demande pour améliore de parcours
	 *
	 * @param redraw si true recherche du parcours optimisée avec limite 10
	 *               secondes.
	 * @return Map<String, Object> : "distParcours" : distance du parcours,
	 *         "rucheParcours" : idruche, ordre, long, lat. Si erreur, "erreur" :
	 *         "Id rucher inconnu"
	 */
	@GetMapping("/parcours/{rucherId}/{redraw}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody Map<String, Object> parcours(@PathVariable Long rucherId, @PathVariable boolean redraw) {
		Map<String, Object> map = new HashMap<>();
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			List<Ruche> ruches = rucheRepository.findByRucherIdOrderByNom(rucherId);
			List<RucheParcours> cheminRet = new ArrayList<>(ruches.size() + 2);
			double retParcours = rucherService.cheminRuchesRucher(cheminRet, rucher, ruches, redraw);
			map.put("distParcours", retParcours);
			map.put("rucheParcours", cheminRet);
			return map;
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		map.put("erreur", "Id rucher inconnu");
		return map;
	}

	/**
	 * Sauvegarde d'un dessin sur carte ign ou osm (appel XMLHttpRequest)
	 */
	@PostMapping("/sauveDessin/{rucherId}")
	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody String sauveDessin(@PathVariable Long rucherId, @RequestBody String dessin) {
		Optional<Rucher> rucherOpt = rucherRepository.findById(rucherId);
		if (rucherOpt.isPresent()) {
			Rucher rucher = rucherOpt.get();
			rucher.setDessin(dessin);
			rucherRepository.save(rucher);
			return "OK";
		}
		logger.error(Const.IDRUCHERXXINCONNU, rucherId);
		return "error";
	}

}

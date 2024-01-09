package ooioo.ruches.recolte;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import ooioo.ruches.Const;
import ooioo.ruches.IdNom;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.rucher.Rucher;
import ooioo.ruches.rucher.RucherRepository;

@Controller
@RequestMapping("/recolte")
public class RecolteController {

	private static final String RECOLTERECOLTEFORM = "recolte/recolteForm";

	private final Logger logger = LoggerFactory.getLogger(RecolteController.class);

	private final RecolteRepository recolteRepository;
	private final RecolteHausseRepository recolteHausseRepository;
	private final EssaimRepository essaimRepository;
	private final RecolteHausseService recolteHausseService;
	private final RucherRepository rucherRepository;
	private final RecolteService recolteService;

	public RecolteController(RecolteRepository recolteRepository, RecolteHausseRepository recolteHausseRepository,
			EssaimRepository essaimRepository, RecolteHausseService recolteHausseService,
			RucherRepository rucherRepository, RecolteService recolteService) {
		this.recolteRepository = recolteRepository;
		this.recolteHausseRepository = recolteHausseRepository;
		this.essaimRepository = essaimRepository;
		this.recolteHausseService = recolteHausseService;
		this.rucherRepository = rucherRepository;
		this.recolteService = recolteService;
	}

	/*
	 * Statistiques de production de miel par année. Miel pesé dans les hausses,
	 * miel mis en pot et miel dans les hausses rapporté au nombre d'essaims moyen
	 * actifs dans l'année. Diagramme à barres verticales (histogramme)/
	 */
	@GetMapping("/statprod")
	public String statprod(Model model) {
		recolteService.statprod(model);
		return "recolte/recoltesStatProd";
	}

	/**
	 * Statistiques d'une récolte.
	 */
	@GetMapping("/stat/{recolteId}")
	public String stat(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			// Pour chaque rucher de la récolte calculer poids total rucher, nb essaims,
			// moyenne, écart type
			List<Long> idruchers = recolteHausseRepository.findRuchersRecolteEssaim(recolte);
			List<Rucher> ruchers = new ArrayList<>(idruchers.size());
			// Par rucher, calcul de la moyenne, de l'écart type, des poids récoltés et du
			// nombre d'essaims pour la récolte.
			List<Double> avgRec = new ArrayList<>(idruchers.size());
			List<Double> stdRec = new ArrayList<>(idruchers.size());
			List<Double> poidsRec = new ArrayList<>(idruchers.size());
			List<Long> countEssaimsRec = new ArrayList<>(idruchers.size());
			List<Double> partRec = new ArrayList<>(idruchers.size());
			List<Double> minRec = new ArrayList<>(idruchers.size());
			List<Double> maxRec = new ArrayList<>(idruchers.size());
			Double pTotalRec = 0d;
			for (Long id : idruchers) {
				Optional<Rucher> rucherOpt = rucherRepository.findById(id);
				ruchers.add(rucherOpt.get());
				List<Object[]> avgStdPoCo = recolteHausseRepository.findAvgStdSumNbRecolte(recolte.getId(), id);
				avgRec.add((Double) avgStdPoCo.get(0)[0] / 1000d); // moyenne des poids des essaims.
				stdRec.add((Double) avgStdPoCo.get(0)[1] / 1000d); // écart type des poids des essaims.
				Double pRec = ((Long) avgStdPoCo.get(0)[2]) / 1000d;
				poidsRec.add(pRec); // somme des poids de miel des essaims.
				countEssaimsRec.add((Long) avgStdPoCo.get(0)[3]);
				minRec.add((Long) avgStdPoCo.get(0)[4] / 1000d); // min poids des essaims.
				maxRec.add((Long) avgStdPoCo.get(0)[5] / 1000d); // max poids des essaims.
				pTotalRec += pRec;
			}
			for (Long id : idruchers) {
				int i = idruchers.indexOf(id);
				partRec.add(100d * ((pTotalRec == 0d) ? 0d : poidsRec.get(i) / pTotalRec));

			}
			model.addAttribute(Const.RUCHERS, ruchers);
			model.addAttribute("avgRec", avgRec);
			model.addAttribute("stdRec", stdRec);
			model.addAttribute("poidsRec", poidsRec);
			model.addAttribute("countEssaimsRec", countEssaimsRec);
			model.addAttribute("partRec", partRec);
			model.addAttribute("minRec", minRec);
			model.addAttribute("maxRec", maxRec);
			// Par essaim de la récolte, calcul du poids, de l'écart par rapport à la
			// moyenne du rucher, de l'écart standardisé, de la note et du nombre de
			// hausses.
			List<Long> idessaims = new ArrayList<>(recolteHausseRepository.findEssaimsRecolteEssaim(recolte));
			List<Essaim> essaims = new ArrayList<>(idessaims.size());
			List<Rucher> ruchersX = new ArrayList<>(idessaims.size());
			List<Double> poidsEss = new ArrayList<>(idessaims.size());
			List<Long> countRec = new ArrayList<>(idessaims.size());
			List<Double> ecartEss = new ArrayList<>(idessaims.size());
			List<Double> noteEss = new ArrayList<>(idessaims.size());
			List<Double> partRecEss = new ArrayList<>(idessaims.size());
			List<Double> partRucherEss = new ArrayList<>(idessaims.size());
			for (Long id : idessaims) {
				Optional<Essaim> essaimOpt = essaimRepository.findById(id);
				Essaim essaim = essaimOpt.get();
				essaims.add(essaim);
				// Recherche du rucher des hausses de récolte de cet essaim.
				Optional<Rucher> rucherOpt = rucherRepository
						.findById(recolteHausseRepository.findRuRRRecolte(recolte, essaim));
				Rucher rucher = rucherOpt.get();
				ruchersX.add(rucher);
				// Somme poids et compte rec pour la récolte et pour l'essaim.
				List<Object[]> poidsCount = recolteHausseRepository.findSumNbRecolte(recolte.getId(), essaim.getId());
				Double poids = ((Long) poidsCount.get(0)[0]) / 1000d;
				poidsEss.add(poids);
				countRec.add((Long) poidsCount.get(0)[1]);
				// i index de rucher.getId() dans idruchers, pour calculer l'écart par
				// rapport à la moyenne et la note.
				int i = idruchers.indexOf(rucher.getId());
				Double avg = avgRec.get(i);
				Double std = stdRec.get(i);
				Double ec = poids - avg;
				ecartEss.add(ec);
				noteEss.add(std == 0d ? 0d : Math.round(1000d * ec / std));
				partRecEss.add(100d * poids / pTotalRec);
				partRucherEss.add(100d * ((poidsRec.get(i) == 0d) ? 0d : poids / poidsRec.get(i)));
			}
			model.addAttribute("poidsEss", poidsEss);
			model.addAttribute("countRec", countRec);
			model.addAttribute("ruchersX", ruchersX);
			model.addAttribute(Const.ESSAIMS, essaims);
			model.addAttribute("ecartEss", ecartEss);
			model.addAttribute("noteEss", noteEss);
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute("partRucherEss", partRucherEss);
			model.addAttribute("partRecEss", partRecEss);
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recolteStat";
	}

	/**
	 * Liste des récoltes.
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		List<Recolte> recoltes = recolteRepository.findAllByOrderByDateDesc();
		model.addAttribute("recoltes", recoltes);
		// Liste par récolte des id et noms des ruchers.
		List<Set<IdNom>> ruchers = new ArrayList<>(recoltes.size());
		for (Recolte recolte : recoltes) {
			ruchers.add(recolteHausseService.idNomRuchers(recolteHausseRepository.findByRecolte(recolte)));
		}
		model.addAttribute(Const.RUCHERS, ruchers);
		return "recolte/recoltesListe";
	}

	/**
	 * Appel du formulaire pour création d'une récolte.
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		Recolte recolte = new Recolte();
		recolte.setDate(Utils.dateTimeDecal(session));
		model.addAttribute(Const.RECOLTE, recolte);
		return RECOLTERECOLTEFORM;
	}

	/**
	 * Appel du formulaire pour modifier une récolte.
	 */
	@GetMapping("/modifie/{recolteId}")
	public String modifie(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			model.addAttribute(Const.RECOLTE, recolteOpt.get());
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return RECOLTERECOLTEFORM;
	}

	/**
	 * Enregistrement de la récolte.
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute Recolte recolte, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RECOLTERECOLTEFORM;
		}
		// On enlève les blancs aux extémités du commentaire.
		recolte.setCommentaire(recolte.getCommentaire().trim());
		String action = (recolte.getId() == null) ? "créée" : "modifiée";
		recolteRepository.save(recolte);
		logger.info("{} {}", recolte, action);
		return "redirect:/recolte/" + recolte.getId();
	}

	/**
	 * Supprimer une récolte et ses hausses de récolte.
	 */
	@GetMapping("/supprime/{recolteId}")
	public String supprime(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolte(recolte);
			for (RecolteHausse recolteHausse : recolteHausses) {
				recolteHausseRepository.delete(recolteHausse);
			}
			recolteRepository.delete(recolte);
			logger.info("{} supprimée", recolte);
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "redirect:/recolte/liste";
	}

	/**
	 * Statistiques tableau poids de miel par essaim et par récolte.
	 *
	 * @param tous si false n'affiche pas les essaims n'ayant jamais produit de
	 *             miel.
	 */
	@GetMapping("/stat/essaim/{tous}")
	public String statistiquesEssaim(Model model, @PathVariable boolean tous) {
		Iterable<Essaim> essaims = essaimRepository.findAll();
		List<List<String>> essaimsRecoltes = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00",
				new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		List<Essaim> essaimsAffiches = new ArrayList<>();  
		for (Essaim essaim : essaims) {
			List<String> poidsListe = new ArrayList<>();
			int poidsTotal = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
				if (poids != null) {
					poidsListe.add(decimalFormat.format(poids / 1000.0));
					poidsTotal += poids;
				} else {
					poidsListe.add("");
				}
			}
			if (tous || (poidsTotal != 0)) {
				// Si tous est false, on n'affiche que les essaims qui ont produit du miel
				poidsListe.add(decimalFormat.format(poidsTotal / 1000.0));
				essaimsRecoltes.add(poidsListe);
				essaimsAffiches.add(essaim);
			}
		}
		model.addAttribute("essaimsAffiches", essaimsAffiches);
		model.addAttribute("recoltes", recoltes);
		model.addAttribute("essaimsRecoltes", essaimsRecoltes);
		return "recolte/recoltesStatEssaim";
	}

	/**
	 * Statistiques graphique poids de miel par essaim pour une récolte. Camembert
	 * chartjs qui affiche la part de chaque essaim dans la récolte en poids et en
	 * %.
	 */
	@GetMapping(value = { "/statistiques/{recolteId}", "/statistiques/{recolteId}/{rucherId}" })
	public String statistiques(Model model, @PathVariable long recolteId,
			@PathVariable(required = false) Long rucherId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			List<Object[]> poidsNomEssaim = (rucherId == null)
					? recolteHausseRepository.findPoidsMielNomEssaimByRecolte(recolteId)
					: recolteHausseRepository.findPoidsMielNomEssaimByRecRucher(recolteId, rucherId);
			// Les listes des noms des essaims et leurs poids de miel pour cette récolte,
			// pour les essaims qui ont eu une production non nulle.
			ArrayList<String> noms = new ArrayList<>();
			ArrayList<Long> poidsListe = new ArrayList<>();
			Long poidsTotal = 0l;
			Long poids;
			for (Object[] obj : poidsNomEssaim) {
				poids = ((Long) obj[0]);
				if (poids > 0) {
					poidsListe.add(poids);
					noms.add((String) obj[1]);
					poidsTotal += poids;
				}
			}
			model.addAttribute("poids", poidsListe);
			model.addAttribute("noms", noms);
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute("poidsTotal", poidsTotal);
			// Liste des ruchers
			List<IdNom> ruchers = recolteHausseRepository.findIdNomsEssaimsRecolte(recolte);
			model.addAttribute("ruchers", ruchers);
			model.addAttribute("rucherId", rucherId);
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recoltesStat";
	}

}
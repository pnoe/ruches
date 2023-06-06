package ooioo.ruches.recolte;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@RequestMapping("/recolte")
public class RecolteController {

	private static final String RECOLTERECOLTEFORM = "recolte/recolteForm";

	private final Logger logger = LoggerFactory.getLogger(RecolteController.class);

	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private RecolteHausseRepository recolteHausseRepository;
	@Autowired
	private EssaimRepository essaimRepository;
	@Autowired
	private RecolteHausseService recolteHausseService;

	@Autowired
	private RecolteService recolteService;

	/*
	 * Statistiques de production de miel par année.
	 * Miel pesé dans les hausses, miel mis en pot et miel dans les hausses
	 *  rapporté au nombre d'essaims moyen actifs dans l'année.
	 * Diagramme à barres verticales (histogramme)/
	 */
	@GetMapping("/statprod")
	public String statprod(Model model) {
		recolteService.statprod(model);
		return "recolte/recoltesStatProd";
	}

	/**
	 * Liste des récoltes
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateDesc();
		model.addAttribute("recoltes", recoltes);
		List<List<IdNom> > ruchers = new ArrayList<>();
		for (Recolte recolte : recoltes) {
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolte(recolte);
			ruchers.add(recolteHausseService.idNomRuchers(recolteHausses));
		}
		model.addAttribute(Const.RUCHERS, ruchers);
		return "recolte/recoltesListe";
	}

	/**
	 * Appel du formulaire pour création d'une récolte
	 */
	@GetMapping("/cree")
	public String cree(HttpSession session, Model model) {
		Recolte recolte = new Recolte();
		recolte.setDate(Utils.dateTimeDecal(session));
		model.addAttribute(Const.RECOLTE, recolte);
		return RECOLTERECOLTEFORM;
	}

	/**
	 * Appel du formulaire pour modifier une récolte
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
	 * Enregistrement de la récolte
	 */
	@PostMapping("/sauve")
	public String sauve(@ModelAttribute  Recolte recolte, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return RECOLTERECOLTEFORM;
		}
		String action = (recolte.getId() == null) ? "créée" : "modifiée";
		recolteRepository.save(recolte);
		logger.info("{} {}", recolte, action);
		return "redirect:/recolte/" + recolte.getId();
	}

	/**
	 * Supprimer une récolte et ses hausses de récolte
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
		return "redirect:/recolte/liste";	}

	/**
	 * Statistiques tableau poids de miel par essaim et par récolte
	 */
	@GetMapping("/statistiques/essaim")
	public String statistiquesEssaim(Model model) {
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateAsc();
		Iterable<Essaim> essaims = essaimRepository.findAll();
		List<List<String>> essaimsRecoltes = new ArrayList<>();
		DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(LocaleContextHolder.getLocale()));
		for (Essaim essaim : essaims) {
			List<String> poidsListe = new ArrayList<>();
			poidsListe.add(essaim.getNom());
			int poidsTotal = 0;
			for (Recolte recolte : recoltes) {
				Integer poids = recolteHausseRepository.findPoidsMielByEssaimByRecolte(essaim.getId(), recolte.getId());
				if (poids != null) {
					poidsListe.add(decimalFormat.format(poids/1000.0));
					poidsTotal += poids;
				} else {
					poidsListe.add("");
				}
			}
			poidsListe.add(decimalFormat.format(poidsTotal/1000.0));
			essaimsRecoltes.add(poidsListe);
		}
		model.addAttribute("recoltes", recoltes);
		model.addAttribute("essaimsRecoltes", essaimsRecoltes);
		return "recolte/recoltesStatEssaim";
	}

	/**
	 * Statistiques graphique poids de miel par essaim pour une récolte
	 */
	@GetMapping("/statistiques/essaim/{recolteId}")
	public String statistiques(Model model, @PathVariable long recolteId) {
		Optional<Recolte> recolteOpt = recolteRepository.findById(recolteId);
		if (recolteOpt.isPresent()) {
			Recolte recolte = recolteOpt.get();
			ArrayList<Integer> poidsListe = new ArrayList<>();
			ArrayList<String> nomListe = new ArrayList<>();
			Integer poidsTotal = 0;
			Iterable <Object[]> poidsNomEssaim = recolteHausseRepository.findPoidsMielNomEssaimByRecolte(recolteId);
			int poids;
			for (Object[] i:poidsNomEssaim) {
				poids = ((BigInteger)i[0]).intValue();
				if (poids > 0) {
					poidsListe.add(poids);
					nomListe.add((String)i[1]);
					poidsTotal += poids;
				}
			}
			model.addAttribute("poids", poidsListe);
			model.addAttribute("noms", nomListe);
			model.addAttribute(Const.RECOLTE, recolte);
			model.addAttribute("poidsTotal", poidsTotal);
		} else {
			logger.error(Const.IDRECOLTEXXINCONNU, recolteId);
			model.addAttribute(Const.MESSAGE, Const.IDRECOLTEINCONNU);
			return Const.INDEX;
		}
		return "recolte/recoltesStat";
	}

}
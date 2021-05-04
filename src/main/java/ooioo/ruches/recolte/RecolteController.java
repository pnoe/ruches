package ooioo.ruches.recolte;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ooioo.ruches.Const;
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

	@GetMapping("/statprod")
	public String statprod(Model model) {
		int debutAnnee = recolteRepository.findFirstByOrderByDateAsc().getDate().getYear();
		int finAnnee = recolteRepository.findFirstByOrderByDateDesc().getDate().getYear();
		int dureeAns = finAnnee - debutAnnee + 1;
		int[] poidsMielHausses = new int[dureeAns];
		int[] poidsMielPots = new int[dureeAns];
		
		for (int i = 0; i < dureeAns; i++) {
			poidsMielHausses[i] = recolteRepository.findPoidsHaussesByYear(Double.valueOf(debutAnnee + i))/1000;
			poidsMielPots[i] = recolteRepository.findPoidsMielByYear(Double.valueOf(debutAnnee + i)).intValue()/1000;
		}
		model.addAttribute("poidsMielHausses", poidsMielHausses);
		model.addAttribute("poidsMielPots", poidsMielPots);
		model.addAttribute("debutAnnee", debutAnnee);
		return "recolte/recoltesStatProd";
	}
	
	/**
	 * Liste des récoltes
	 */
	@GetMapping("/liste")
	public String liste(Model model) {
		Iterable<Recolte> recoltes = recolteRepository.findAllByOrderByDateDesc();
		model.addAttribute("recoltes", recoltes);
		List<String> rucherListe = new ArrayList<>();
		for (Recolte recolte : recoltes) {
			List<String> rucher = new ArrayList<>();
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolte(recolte);
			for (RecolteHausse recolteHausse : recolteHausses) {
				if ((recolteHausse.getRucher() != null) &&
					(!rucher.contains(recolteHausse.getRucher().getNom()))
					) {
					rucher.add(recolteHausse.getRucher().getNom());
				}
			}
			rucherListe.add(String.join(",", rucher));	
		}
		model.addAttribute(Const.RUCHERS, rucherListe);
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
		recolteRepository.save(recolte);
		logger.info("Récolte {} enregistrée", recolte.getId());
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
			logger.info("Récolte {} supprimée, id {}", recolte.getDate(), recolte.getId());
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
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		for (Essaim essaim : essaims) {
			List<String> poidsListe = new ArrayList<>();
			poidsListe.add(essaim.getNom());
			Integer poidsTotal = 0;
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
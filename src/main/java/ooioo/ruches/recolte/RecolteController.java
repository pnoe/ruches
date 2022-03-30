package ooioo.ruches.recolte;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

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

import ooioo.ruches.Const;
import ooioo.ruches.Utils;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;

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
	private	EvenementRepository evenementRepository;
	@Autowired
	private RecolteHausseService recolteHausseService;

	/*
	 * Statistiques de production de miel par année
	 * Miel pesé dans les hausses, miel mis en pot et miel dans les hausses
	 *  rapporté au nombre d'essaims moyen actifs dans l'année
	 * Diagramme à barres
	 */
	@GetMapping("/statprod")
	public String statprod(Model model) {
		int debutAnnee = recolteRepository.findFirstByOrderByDateAsc().getDate().getYear();
		LocalDate maintenant = LocalDate.now();
		int finAnnee = maintenant.getYear();
		int dureeAns = finAnnee - debutAnnee + 1;
		int[] poidsMielHausses = new int[dureeAns];
		int[] poidsMielPots = new int[dureeAns];
		for (int i = 0; i < dureeAns; i++) {
			Optional<Integer> pMHOpt = recolteRepository.
					findPoidsHaussesByYear(Double.valueOf((double)debutAnnee + i));
			poidsMielHausses[i] = pMHOpt.isPresent() ? pMHOpt.get()/1000 : 0;
			Optional<Double> pMPOpt = recolteRepository.
					findPoidsMielByYear(Double.valueOf((double)debutAnnee + i));
			poidsMielPots[i] = pMPOpt.isPresent() ? pMPOpt.get().intValue()/1000 : 0;
		}
		float[] nbEssaims = new float[dureeAns]; // nombre d'essaims actifs par année de production
		Iterable<Essaim> essaims = essaimRepository.findAll();
		LocalDate dateFirstEssaim = essaimRepository.findFirstByOrderByDateAcquisition().getDateAcquisition();
		for (Essaim essaim : essaims) {
			LocalDate dateProduction = essaim.getDateAcquisition();
			Evenement dispersion = evenementRepository.
					findFirstByEssaimAndType(essaim, TypeEvenement.ESSAIMDISPERSION);
			LocalDate dateFin = (dispersion == null)?maintenant:dispersion.getDate().toLocalDate();
			// l'essaim est actif de dateProduction à dateFin
			boolean premier = true;
			for (int annee = dateProduction.getYear(); annee <= dateFin.getYear(); annee++) {
				// trouver le nombre de jours où l'essaim était actif dans l'année
				//   de dDebut à dFin
				// dDebut = date acquisition pour la première année, sinon 1er janvier
				//    de l'année
				LocalDate dDebut = premier ? dateProduction : LocalDate.of(annee, 1, 1);
				premier = false;
				if (annee < debutAnnee) {
					// si l'annee est inférieur à l'année de début de production
					//  on la passe. Pas de miel produit.
					continue;
				}
				LocalDate dFin = LocalDate.of(annee + 1, 1, 1);
				if (dateFin.isBefore(dFin)) {
					// si la fin de la date de fin de l'essaim (dispersion ou now()) est avant
					// l'année + 1 de la boucle, on retient cette fin de l'essaim
					dFin = dateFin;
				}
				float nbJoursAnnee;
				// Si la date courante est entre annee et annee + 1
				//   on ne garde comme durée de l'année que le nombre de jour jusqu'à now()
				//   et si la date d'acquisition du premier essaim est entre annee et annee + 1
				//      on débute le compte du nombre de jour à cette date
				if (LocalDate.of(annee + 1, 1, 1).isBefore(maintenant)) {
					if (LocalDate.of(annee, 1, 1).isBefore(dateFirstEssaim)) {
						nbJoursAnnee = ChronoUnit.DAYS.between(dateFirstEssaim, LocalDate.of(annee + 1, 1, 1));
					} else {
						nbJoursAnnee = ChronoUnit.DAYS.between(LocalDate.of(annee, 1, 1), LocalDate.of(annee + 1, 1, 1));
					}
				} else {
					if (LocalDate.of(annee, 1, 1).isBefore(dateFirstEssaim)) {
						nbJoursAnnee = ChronoUnit.DAYS.between(dateFirstEssaim, maintenant);
					} else {
						nbJoursAnnee = ChronoUnit.DAYS.between(LocalDate.of(annee, 1, 1), maintenant);
					}
				}
				nbEssaims[annee - debutAnnee] += ChronoUnit.DAYS.between(dDebut, dFin)/(nbJoursAnnee);
			}
		}
		// faire l'arrondi de nbEssaims dans un tableau d'int nbIEssaims
		//   et calculer le poids de miel moyen pas essaim par année
		int[] nbIEssaims = new int[dureeAns];
		for (int i = 0; i < dureeAns; i++) {
			nbIEssaims[i] = Math.round(poidsMielHausses[i]/nbEssaims[i]);
		}
		model.addAttribute("poidsMielHausses", poidsMielHausses);
		model.addAttribute("poidsMielPots", poidsMielPots);
		model.addAttribute("debutAnnee", debutAnnee);
		model.addAttribute("nbIEssaims", nbIEssaims);
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
			List<RecolteHausse> recolteHausses = recolteHausseRepository.findByRecolte(recolte);
			rucherListe.add(recolteHausseService.nomsRuchers(recolteHausses));
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
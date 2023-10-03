package ooioo.ruches.recolte;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;

@Service
public class RecolteService {

	@Autowired
	private RecolteRepository recolteRepository;
	@Autowired
	private EssaimRepository essaimRepository;

	/*
	 * Statistiques de production de miel par année. Miel pesé dans les hausses de
	 * récolte, miel mis en pot et miel dans les hausses rapporté au nombre
	 * d'essaims moyen actifs dans l'année. Diagramme à barres verticales
	 * (histogramme).
	 */
	void statprod(Model model) {
		Recolte recFirst = recolteRepository.findFirstByOrderByDateAsc();
		if (recFirst == null) {
			// Pas encore de récolte.
			return;
		}
		int debutAnnee = recFirst.getDate().getYear();
		LocalDate maintenant = LocalDate.now();
		int finAnnee = maintenant.getYear();
		int dureeAns = finAnnee - debutAnnee + 1;
		// Poids de miel pesé dans les hausses de récolte par année
		int[] poidsMielHausses = new int[dureeAns];
		// Poids de miel mis en pot par année
		int[] poidsMielPots = new int[dureeAns];
		for (int i = 0; i < dureeAns; i++) {
			Optional<Integer> pMHOpt = recolteRepository
					.findPoidsHaussesByYear(Double.valueOf((double) debutAnnee + i));
			poidsMielHausses[i] = pMHOpt.isPresent() ? pMHOpt.get() / 1000 : 0;
			Optional<Double> pMPOpt = recolteRepository.findPoidsMielByYear(debutAnnee + i);
			poidsMielPots[i] = pMPOpt.isPresent() ? pMPOpt.get().intValue() / 1000 : 0;
		}
		float[] nbEssaims = new float[dureeAns]; // nombre d'essaims actifs par année de production
		Iterable<Essaim> essaims = essaimRepository.findAll();
		LocalDate dateFirstEssaim = essaimRepository.findFirstByOrderByDateAcquisition().getDateAcquisition();
		for (Essaim essaim : essaims) {
			LocalDate dateProduction = essaim.getDateAcquisition();
			// dispersion mis dans entité essaim
			LocalDate dateFin = (essaim.getActif()) ? maintenant : essaim.getDateDispersion().toLocalDate();
			// l'essaim est actif de dateProduction à dateFin
			boolean premier = true;
			for (int annee = dateProduction.getYear(); annee <= dateFin.getYear(); annee++) {
				// trouver le nombre de jours où l'essaim était actif dans l'année
				// de dDebut à dFin
				// dDebut = date acquisition pour la première année, sinon 1er janvier
				// de l'année
				LocalDate dDebut = premier ? dateProduction : LocalDate.of(annee, 1, 1);
				premier = false;
				if (annee < debutAnnee) {
					// si l'annee est inférieur à l'année de début de production
					// on la passe. Pas de miel produit.
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
				// on ne garde comme durée de l'année que le nombre de jour jusqu'à now()
				// et si la date d'acquisition du premier essaim est entre annee et annee + 1
				// on débute le compte du nombre de jour à cette date
				if (LocalDate.of(annee + 1, 1, 1).isBefore(maintenant)) {
					if (LocalDate.of(annee, 1, 1).isBefore(dateFirstEssaim)) {
						nbJoursAnnee = ChronoUnit.DAYS.between(dateFirstEssaim, LocalDate.of(annee + 1, 1, 1));
					} else {
						nbJoursAnnee = ChronoUnit.DAYS.between(LocalDate.of(annee, 1, 1),
								LocalDate.of(annee + 1, 1, 1));
					}
				} else {
					if (LocalDate.of(annee, 1, 1).isBefore(dateFirstEssaim)) {
						nbJoursAnnee = ChronoUnit.DAYS.between(dateFirstEssaim, maintenant);
					} else {
						nbJoursAnnee = ChronoUnit.DAYS.between(LocalDate.of(annee, 1, 1), maintenant);
					}
				}
				nbEssaims[annee - debutAnnee] += ChronoUnit.DAYS.between(dDebut, dFin) / (nbJoursAnnee);
			}
		}
		// faire l'arrondi de nbEssaims dans un tableau d'int nbIEssaims
		// et calculer le poids de miel moyen pas essaim par année
		int[] nbIEssaims = new int[dureeAns];
		for (int i = 0; i < dureeAns; i++) {
			nbIEssaims[i] = Math.round(poidsMielHausses[i] / nbEssaims[i]);
		}
		model.addAttribute("poidsMielHausses", poidsMielHausses);
		model.addAttribute("poidsMielPots", poidsMielPots);
		model.addAttribute("debutAnnee", debutAnnee);
		model.addAttribute("nbIEssaims", nbIEssaims);
	}
}

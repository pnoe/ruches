package ooioo.ruches;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.essaim.EssaimRepository;
import ooioo.ruches.evenement.Evenement;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;

/*
 * Pour le calcul des données des graphiques du nombre de ruches et d'essaims.
 */
@Service
public class GrapheEsRuService {

	private final Logger logger = LoggerFactory.getLogger(GrapheEsRuService.class);

	private final EvenementRepository evenementRepository;
	private final EssaimRepository essaimRepository;

	public GrapheEsRuService(EvenementRepository evenementRepository, EssaimRepository essaimRepository) {
		this.evenementRepository = evenementRepository;
		this.essaimRepository = essaimRepository;
	}

	/**
	 *
	 * @param ruchesAcqu    les ids et dates (LocalDateTime) d'acquisition des
	 *                      ruches.
	 * @param ruInacLastEve les ids et dates (LocalDateTime) d'inactivation des
	 *                      ruches. C'est une estimation de cette date d'après le
	 *                      dernier événement qui la référence.
	 * @param suffixe
	 */
	public void nbRuches(Model model, List<IdDateNoTime> ruchesAcqu, List<IdDate> ruInacLastEve, String suffixe) {
		List<Long[]> datesTotal = new ArrayList<>();
		// Pour chaque ruche inactive, on recherche la date du dernier événement la
		// concernant.
		// Id est forcé à null pour distinguer la liste acqusition qui ajoute la ruche
		// (+1) et la liste lastEve qui retire la ruche (-1).
		// Incrémenter d'un jour les dates de ruInacLastEve.
		for (IdDate e : ruInacLastEve) {
			ruchesAcqu.add(new IdDateNoTime(null, e.date().toLocalDate()));
		}
		if (!ruchesAcqu.isEmpty()) {
			// Trier rucheInacLastEve par date.
			Collections.sort(ruchesAcqu, Comparator.comparing(IdDateNoTime::date));
			LocalDate datecourTotal = ruchesAcqu.get(0).date();
			int nbHTotal = 0;
			for (IdDateNoTime rA : ruchesAcqu) {
				if (!datecourTotal.equals(rA.date())) {
					// Si l'événement est à un autre jour que les précédents.
					// Dates en millisecondes pour Chartjs.
					datesTotal.add(new Long[] { 1000 * datecourTotal.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC),
							(long) nbHTotal });
					datecourTotal = rA.date();
				}
				nbHTotal += (rA.id() == null) ? -1 : +1;
			}
			datesTotal.add(
					new Long[] { 1000 * datecourTotal.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), (long) nbHTotal });
		}
		model.addAttribute("dates" + suffixe, datesTotal);
	}

	/**
	 * Calcul de la liste dates et nombre d'essaims de production regroupés par
	 * jour.
	 */
	public void nbEssaimsProd(Model model) {
		// essaims liste de tous les essaims actifs et inactifs projetés Id, Nom
		List<IdNom> essaims = essaimRepository.findAllProjectedIdNomBy();
		// signeDate liste des dates (LocalDate) d'ajout si id = 1 ou de retrait si id =
		// -1 (emploi de IdDateNoTime pour éviter de recréer un record).
		List<IdDateNoTime> signeDate = new ArrayList<>();
		for (IdNom esIdNom : essaims) {
			Long esId = esIdNom.id();
			Optional<Essaim> esOpt = essaimRepository.findById(esId);
			if (esOpt.isPresent()) {
				Essaim es = esOpt.get();
				// eves liste des événements mise en ruche triés par date ascendante de l'essaim
				// esId. Projection difficile, les champs date et ruche.production sont
				// utilisés, plus log en cas d'erreur de l'événement.
				List<Evenement> eves = evenementRepository.findByEssaimIdAndTypeOrderByDateAsc(esId,
						TypeEvenement.AJOUTESSAIMRUCHE);
				// eProd état de l'essaim es : production ou non.
				boolean eProd = false;
				for (Evenement ev : eves) {
					// Si date événement ev (LocalDateTime) est avant date acquisition (LocalDate)
					boolean errDateAcq = ev.getDate().toLocalDate().isBefore(es.getDateAcquisition());
					// Si essaim inactif et date événement ev (LocalDateTime) est après la date
					// dispersion de l'essaim.
					boolean errDateDisp = !es.getActif() && (ev.getDate().isAfter(es.getDateDispersion()));
					// Si la ruche n'est pas renseignée
					boolean errEveRuche = ev.getRuche() == null;
					if (errDateAcq || errDateDisp || errEveRuche) {
						// Si erreur événement, on l'ignore.
						if (errDateAcq) {
							logger.error("{} est avant le date d'acquisition de l'essaim {}", ev, es);
						}
						if (errDateDisp) {
							logger.error("{} est après le date de dispersion de l'essaim {}", ev, es);
						}
						if (errEveRuche) {
							logger.error("{} ruche non renseignée", ev);
						}
						continue;
					}
					// Trouver si la ruche de l'événement est une ruche de production.
					boolean prod = ev.getRuche().getProduction();
					if (eProd && !prod) {
						// Si l'état courant est essaim dans une ruche de production et que l'on passe
						// dans une ruche qui n'est pas de production.
						eProd = false;
						// mémoriser date et -1 essaim de prod.
						signeDate.add(new IdDateNoTime(-1l, ev.getDate().toLocalDate()));
						continue;
					}
					if (!eProd && prod) {
						// Si l'état courant est essaim dans une ruche pas de production et que l'on
						// passe dans une ruche de production.
						eProd = true;
						// mémoriser date et +1 essaim de prod.
						signeDate.add(new IdDateNoTime(1l, ev.getDate().toLocalDate()));
						continue;
					}
				} // Fin boucle événements
				if (!es.getActif() && eProd) {
					// L'essaim est inactif et l'état courant à la fin du traitement des événements
					// est production. Mémoriser date dispersion et -1.
					signeDate.add(new IdDateNoTime(-1l, es.getDateDispersion().toLocalDate()));
				}
			} else {
				logger.error(Const.IDESSAIMXXINCONNU, esIdNom.id());
			}
		} // Fin boucle essaims.
			// Trie signeDate par dates.
		Collections.sort(signeDate, Comparator.comparing(IdDateNoTime::date));
		// Alimente datesNb avec la date Epoch en millisecondes et le nombre d'essaims
		// de production à cette date. Regroupement par jour.
		List<Long[]> datesNb = new ArrayList<>();
		LocalDate datecour = signeDate.get(0).date();
		long nb = 0;
		for (IdDateNoTime idd : signeDate) {
			// Regroupement par jour.
			if (!datecour.equals(idd.date())) {
				datesNb.add(new Long[] { 1000 * datecour.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), nb });
				datecour = idd.date();
			}
			nb += idd.id();
		}
		datesNb.add(new Long[] { 1000 * datecour.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), nb });
		model.addAttribute("datesProd", datesNb);
	}

	/**
	 * Calcul de la liste des dates et nombre d'essaims regroupés par jour.
	 *
	 * @param essaimsAcqu les ids et dates (LocalDate) d'acquisition des essaims.
	 * @param essaimsDisp les ids et dates (LocalDateTime) de dispersion des essaims
	 *                    (inactifs).
	 */
	public void nbEssaims(Model model, List<IdDateNoTime> essaimsAcqu, List<IdDate> essaimsDisp) {
		// La taille de ces listes n'est pas connue ( inférieure à essaimsAcqu.size() +
		// essaimsDisp.size()).
		List<Long[]> dates = new ArrayList<>();
		// Fusion des deux listes, on met null comme id pour les dispersions, ce qui
		// permettra de les différencier des acquisitions.
		for (IdDate e : essaimsDisp) {
			essaimsAcqu.add(new IdDateNoTime(null, e.date().toLocalDate()));
		}
		if (!essaimsAcqu.isEmpty()) {
			// Tri de la fusion par dates croissantes.
			Collections.sort(essaimsAcqu, Comparator.comparing(IdDateNoTime::date));
			// On compte le nombre d'essaims pour toutes ces dates : acquisition +1,
			// dispersion -1.
			LocalDate datecour = essaimsAcqu.get(0).date();
			long nb = 0;
			for (IdDateNoTime e : essaimsAcqu) {
				if (!datecour.equals(e.date())) {
					// Si l'événement est à une autre date que les précédents.
					dates.add(new Long[] { 1000 * datecour.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), nb });
					datecour = e.date();
				}
				nb += (e.id() == null) ? -1 : +1;
			}
			dates.add(new Long[] { 1000 * datecour.toEpochSecond(LocalTime.MIN, ZoneOffset.UTC), nb });
		}
		model.addAttribute("dates", dates);
	}

}

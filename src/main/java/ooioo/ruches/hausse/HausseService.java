package ooioo.ruches.hausse;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ooioo.ruches.IdDate;
import ooioo.ruches.IdDateNoTime;
import ooioo.ruches.TypeDate;
import ooioo.ruches.evenement.EvenementRepository;
import ooioo.ruches.evenement.TypeEvenement;

@Service
public class HausseService {

	private final EvenementRepository evenementRepository;
	private final HausseRepository hausseRepository;

	public HausseService(EvenementRepository evenementRepository, HausseRepository hausseRepository) {
		this.evenementRepository = evenementRepository;
		this.hausseRepository = hausseRepository;
	}

	public void grapheHaussePose(Model model) {
		// Liste des événements pose et retrait hausse triés par date croissante.
		List<TypeDate> eves = evenementRepository.findByTypeOrTypeOrderByDateAsc(TypeEvenement.HAUSSEPOSERUCHE,
				TypeEvenement.HAUSSERETRAITRUCHE);
		// Pour le nombre de hausses posées sur des ruches.
		// Les dates seront regroupées par jour.
		List<Long> dates = new ArrayList<>();
		List<Integer> nbPosees = new ArrayList<>();
		// Pour le nombre de hausses total.
		List<Long> datesTotal = new ArrayList<>();
		List<Integer> nbTotal = new ArrayList<>();
		if (!eves.isEmpty()) {
			LocalDateTime datecour = eves.get(0).date();
			int nbPose = 0;
			for (TypeDate e : eves) {
				if (datecour.getDayOfYear() != e.date().getDayOfYear() || datecour.getYear() != e.date().getYear()) {
					// Si l'événement est à un autre jour ou une autre année que les précédents, on
					// ajoute le nombre de hausses et la date précédente aux listes pour le graphique.
					nbPosees.add(nbPose);
					dates.add(datecour.toEpochSecond(ZoneOffset.UTC));
					// On mémorise la nouvelle date courante.
					datecour = e.date();
				}
				nbPose += e.type().equals(TypeEvenement.HAUSSEPOSERUCHE) ? 1 : -1;
			}
			nbPosees.add(nbPose);
			dates.add(datecour.toEpochSecond(ZoneOffset.UTC));
			// Courbe du nombre total de hausses.
			// Estimation de la date d'inactivation d'une hausse d'après la date du dernier
			// événement qui la référence.
			// Liste des dates d'acquisition des hausses (actives et inactives).
			List<IdDateNoTime> haussesAcqu = hausseRepository.findByOrderByDateAcquisition();
			// Pour chaque hausse inactive, on recherche la date du dernier événement la
			// concernant.
			// Id est forcé à null pour distinguer la liste acqusition qui ajoute la hausse
			// (+1) et la liste lastEve qui retire la hausse (-1).
			List<IdDate> haInacLastEve = evenementRepository.findHausseInacLastEve();
			// Incrémenter d'un jour les dates de hausseInacLastEve.
			List<IdDate> hausseInacLastEve = new ArrayList<>(haInacLastEve.size());
			for (IdDate idD : haInacLastEve) {
				hausseInacLastEve.add(new IdDate(null, idD.date().plusDays(1)));
			}
			// Fusion des deux listes.
			for (IdDateNoTime hA : haussesAcqu) {
				hausseInacLastEve.add(new IdDate(hA.id(), LocalDateTime.of(hA.date(), LocalTime.NOON)));
			}
			// Trier hausseInacLastEve par date.
			Collections.sort(hausseInacLastEve, Comparator.comparing(IdDate::date));
			if (!hausseInacLastEve.isEmpty()) {
				LocalDateTime datecourTotal = hausseInacLastEve.get(0).date();
				int nbHTotal = 0;
				for (IdDate hA : hausseInacLastEve) {
					if (datecourTotal.getDayOfYear() != hA.date().getDayOfYear()
							|| datecourTotal.getYear() != hA.date().getYear()) {
						// Si l'événement est à un autre jour que les pécédents.
						nbTotal.add(nbHTotal);
						datesTotal.add(datecourTotal.toEpochSecond(ZoneOffset.UTC));
						datecourTotal = hA.date();
					}
					nbHTotal += (hA.id() == null) ? -1 : +1;
				}
				nbTotal.add(nbHTotal);
				datesTotal.add(datecourTotal.toEpochSecond(ZoneOffset.UTC));
				if (eves.get(eves.size() - 1).date()
						.isAfter(hausseInacLastEve.get(hausseInacLastEve.size() - 1).date())) {
					// On complète la courbe jusqu'à la fin de la courbe des hausses posées.
					datesTotal.add(eves.get(eves.size() - 1).date().toEpochSecond(ZoneOffset.UTC));
					nbTotal.add(nbHTotal);
				}
			}
		}
		model.addAttribute("dates", dates);
		model.addAttribute("nbPosees", nbPosees);
		model.addAttribute("datesTotal", datesTotal);
		model.addAttribute("nbTotal", nbTotal);
	}

}
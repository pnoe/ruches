package ooioo.ruches.recolte;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class RecolteHausseService {

	/**
	 * Retourne une String des noms des ruchers séparés par une virgule
	 * auquels appartiennent les hausses de la récolte
	 */
	public String nomsRuchers(List<RecolteHausse> recolteHausses) {
		List<String> nomRuchers = new ArrayList<>();
		for (RecolteHausse recolteHausse : recolteHausses) {
			if ((recolteHausse.getRucher() != null) && (!nomRuchers.contains(recolteHausse.getRucher().getNom()))) {
				nomRuchers.add(recolteHausse.getRucher().getNom());
			}
		}
		return String.join(", ", nomRuchers);
	}

	/**
	 * Retourne une liste des noms des ruches de la récolte
	 */
	public List<String> nomsRuches(List<RecolteHausse> recolteHausses) {
		List<String> nomRuches = new ArrayList<>();
		for (RecolteHausse recolteHausse : recolteHausses) {
			if ((recolteHausse.getRuche() != null) && (!nomRuches.contains(recolteHausse.getRuche().getNom()))) {
				nomRuches.add(recolteHausse.getRuche().getNom());
			}
		}
		return nomRuches;
	}

}

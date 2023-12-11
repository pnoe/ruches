package ooioo.ruches.recolte;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ooioo.ruches.IdNom;

@Service
public class RecolteHausseService {

	/**
	 * Set des IdNom : ids et noms des ruchers d'une récolte.
	 *
	 * @param recolteHausses la liste des hausses d'une récolte
	 * @return les records IdNom des ruchers de cette récolte
	 */
	Set<IdNom> idNomRuchers(List<RecolteHausse> recolteHausses) {
		Set<IdNom> idNoms = new HashSet<>();
		for (RecolteHausse rH : recolteHausses) {
			if (rH.getRucher() != null) {
				IdNom idn = new IdNom(rH.getRucher().getId(), rH.getRucher().getNom());
				idNoms.add(idn);
			}
		}
		return idNoms;
	}

	/**
	 * Retourne le nombre de ruches de la liste recolteHausses passée en argument.
	 */
	int recHausNbRuches(List<RecolteHausse> recolteHausses) {
		return recolteHausses.stream().filter(recolteHausse -> recolteHausse.getRuche() != null)
				.map(recolteHausse -> recolteHausse.getRuche().getId()).collect(Collectors.toSet()).size();
	}

}

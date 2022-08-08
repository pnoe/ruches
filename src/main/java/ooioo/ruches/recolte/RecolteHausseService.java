package ooioo.ruches.recolte;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ooioo.ruches.IdNom;

@Service
public class RecolteHausseService {

	/**
	 * Liste des ids et noms des ruchers d'une récolte
	 * 
	 * @param recolteHausses la liste des hausses d'une récolte
	 * @return les records IdNom des ruchers de cette récolte
	 */
	public List<IdNom> idNomRuchers(List<RecolteHausse> recolteHausses) {
		List<IdNom> idNoms = new ArrayList<>();
		for (RecolteHausse rH : recolteHausses) {
			IdNom idn = new IdNom(rH.getRucher().getId(), rH.getRucher().getNom());
			if ((rH.getRucher() != null) && (!idNoms.contains(idn))) {
				idNoms.add(idn);
			}
		}
		return idNoms;
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

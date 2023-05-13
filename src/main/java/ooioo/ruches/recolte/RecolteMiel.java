package ooioo.ruches.recolte;

import java.util.ArrayList;
import java.util.List;

/**
 * Liste de hausses de récolte pour saisie tabulaire de leurs poids de miel. 
 */
public class RecolteMiel {

	private List<RecolteHausseMiel> recolteHaussesMiel;

	public RecolteMiel() {
		this.recolteHaussesMiel = new ArrayList<>();
	}

	public List<RecolteHausseMiel> getRecolteHaussesMiel() {
		return recolteHaussesMiel;
	}

	public void setRecolteHaussesMiel(List<RecolteHausseMiel> rhm) {
		this.recolteHaussesMiel = rhm;
	}

	/*
	public void addRecolteHausseMiel(RecolteHausseMiel rhm) {
		this.recolteHaussesMiel.add(rhm);
	}
	*/

}
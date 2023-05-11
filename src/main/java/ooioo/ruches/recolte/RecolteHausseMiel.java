package ooioo.ruches.recolte;

import java.math.BigDecimal;

/**
 * Pour saisie tabulaire de leurs poids de miel.
 */
public class RecolteHausseMiel {

	public RecolteHausseMiel() {
		// constructeur par défaut
	}

	/**
	 * Poids de la hausse avant récolte
	 */
	private Integer poidsAvant;

	/**
	 * Poids de la hausse après récolte
	 */
	private Integer poidsApres;

	/**
	 * L'id de la hausse de Récolte.
	 */
	private Long recolteHausseId;

	public BigDecimal getPoidsAvant() {
		return new BigDecimal(poidsAvant).movePointLeft(3);
	}

	public void setPoidsAvant(BigDecimal poidsAvant) {
		this.poidsAvant = poidsAvant.movePointRight(3).intValue();
	}

	public BigDecimal getPoidsApres() {
		return new BigDecimal(poidsApres).movePointLeft(3);
	}

	public void setPoidsApres(BigDecimal poidsApres) {
		this.poidsApres = poidsApres.movePointRight(3).intValue();
	}

	public Long getRecolteHausseId() {
		return recolteHausseId;
	}

	public void setRecolteHausseId(Long recolteHausseId) {
		this.recolteHausseId = recolteHausseId;
	}

}
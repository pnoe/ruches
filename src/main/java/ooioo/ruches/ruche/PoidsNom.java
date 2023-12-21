package ooioo.ruches.ruche;

import java.math.BigDecimal;

public class PoidsNom {
	
	PoidsNom(String nom, Integer poidsVide) {
		this.nom = nom;
		this.poidsVide = poidsVide;
	}

	private String nom;

	private Integer poidsVide;
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public BigDecimal getPoidsVide() {
		return new BigDecimal(poidsVide).movePointLeft(3);
	}

	public void setPoidsVide(BigDecimal poidsVide) {
		this.poidsVide = poidsVide.movePointRight(3).intValue();
	}

}
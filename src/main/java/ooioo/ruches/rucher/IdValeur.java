package ooioo.ruches.rucher;

public class IdValeur {
	private Long id;
	private String valeur;

	// @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	// private String date;

	// private String commentaire;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValeur() {
		return valeur;
	}

	public void setValeur(String valeur) {
		this.valeur = valeur;
	}

}
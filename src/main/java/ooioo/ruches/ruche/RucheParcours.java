package ooioo.ruches.ruche;

/**
 * Pour la fonction de recherche du parcours des ruches d'un rucher
 */
public class RucheParcours {
	
	public RucheParcours() {
	}

	public RucheParcours(Long id, int ordre, Float longitude, Float latitude) {
		this.id = id;
		this.ordre = ordre;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	/**
	 * Id de la ruche ou 0 pour les points entrée/sortie du rucher
	 */
	private Long id;

	/**
	 * Ordre de la ruche renvoyé par la recherche en base
	 */
	private int ordre;

	/**
	 * Longitude de la ruche
	 */
	private Float longitude;

	/*
	 * Latitude de la ruche
	 */
	private Float latitude;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getOrdre() {
		return ordre;
	}

	public void setOrdre(int ordre) {
		this.ordre = ordre;
	}

	public Float getLongitude() {
		return longitude;
	}

	public void setLongitude(Float longitude) {
		this.longitude = longitude;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

}
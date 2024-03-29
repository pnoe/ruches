package ooioo.ruches.rucher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import ooioo.ruches.personne.Personne;

@Entity
public class Rucher {

	@Override
	public String toString() {
		return "Rucher [id=" + id + ", depot=" + depot + ", nom=" + nom + ", contact=" +
				((contact == null)?"null":contact.getNom()) + ", adresse="
				+ adresse + ", commentaire=" + commentaire + ", longitude=" + longitude + ", latitude=" + latitude
				+ ", altitude=" + altitude + ", ressource=" + ressource + ", actif=" + actif + ", dessin=" + dessin
				+ "]";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Ce rucher est-il le dépôt ?
	 */
	private boolean depot = false;

	/**
	 * Nom du rucher
	 */
	@Column(nullable = false, unique = true)
	private String nom;

	/**
	 * Le propriétaire du rucher
	 */
	@ManyToOne
	private Personne contact;

	/**
	 * Adresse du rucher
	 */
	private String adresse;

	/**
	 * Commentaire
	 */
	private String commentaire;

	/**
	 * Longitude du rucher
	 */
	private Float longitude;

	/*
	 * Latitude du rucher
	 */
	private Float latitude;

	/**
	 * Altitude du rucher en mètres
	 */
	private Integer altitude = 100;

	/**
	 * Plantes mellifères caractéristiques du rucher
	 */
	private String ressource;

	/**
	 * Rucher actif ?
	 */
	private boolean actif = true;

	/**
	 * Dessin créé dans une carte ign ou osm au format kml
	 */
	private String dessin;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean getDepot() {
		return depot;
	}

	public void setDepot(boolean depot) {
		this.depot = depot;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Personne getContact() {
		return contact;
	}

	public void setContact(Personne contact) {
		this.contact = contact;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
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

	public Integer getAltitude() {
		return altitude;
	}

	public void setAltitude(Integer altitude) {
		this.altitude = altitude;
	}

	public String getRessource() {
		return ressource;
	}

	public void setRessource(String ressource) {
		this.ressource = ressource;
	}

	public boolean getActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public String getDessin() {
		return dessin;
	}

	public void setDessin(String dessin) {
		this.dessin = dessin;
	}

}
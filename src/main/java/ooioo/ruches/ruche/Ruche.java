package ooioo.ruches.ruche;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.springframework.format.annotation.DateTimeFormat;

import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.ruche.type.RucheType;
import ooioo.ruches.rucher.Rucher;

@Entity
public class Ruche {

	@Override
	public String toString() {
		return "Ruche [id=" + id + ", nom=" + nom + ", active=" + active + ", Acquisition=" + dateAcquisition
				+ ", poidsVide=" + poidsVide + ", type=" + ((type == null) ? "null" : type.getNom()) +
				", essaim="	+ ((essaim == null) ? "null" : essaim.getNom()) + 
				", rucher=" + ((rucher == null) ? "null" : rucher.getNom()) +
				", longitude=" + longitude + ", latitude=" + latitude + ", commentaire=" + commentaire + "]";
	}

	public Ruche() {
	}

	/**
	 * Clonage d'une ruche
	 */
	public Ruche(Ruche ruche, String nom, Float longitude) {
		// l'essaim est laissé à null
		// champs modifiés
		this.nom = nom;
		this.longitude = longitude;
		// champs identiques
		this.active = ruche.getActive();
		this.dateAcquisition = ruche.getDateAcquisition();
		this.setPoidsVide(ruche.getPoidsVide());
		this.type = ruche.getType();
		this.rucher = ruche.getRucher();
		this.latitude = ruche.getLatitude();
		this.commentaire = ruche.commentaire;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String nom;

	/**
	 * Ruche active ?
	 */
	private boolean active = true;

	/**
	 * Date d'acquisition de la ruche
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateAcquisition;

	/**
	 * Poids de la ruche sans cadres en grammes
	 */
	private Integer poidsVide = 15000;

	/**
	 * Type de ruche
	 */
	@ManyToOne
	private RucheType type;

	/**
	 * Essaim dans la ruche
	 */
	@OneToOne
	@JoinColumn(unique = true)
	private Essaim essaim;

	/*
	 * Rucher où est installée la ruche
	 */
	@ManyToOne
	private Rucher rucher;

	/**
	 * Longitude de la ruche
	 */
	private Float longitude;

	/*
	 * Latitude de la ruche
	 */
	private Float latitude;

	/**
	 * Commentaire
	 */
	private String commentaire;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDate getDateAcquisition() {
		return dateAcquisition;
	}

	public void setDateAcquisition(LocalDate dateAcquisition) {
		this.dateAcquisition = dateAcquisition;
	}

	public Integer getPoidsVideInt() {
		return poidsVide;
	}

	public void setPoidsVideInt(Integer poidsVide) {
		this.poidsVide = poidsVide;
	}

	public BigDecimal getPoidsVide() {
		return new BigDecimal(poidsVide).movePointLeft(3);
	}

	public void setPoidsVide(BigDecimal poidsVide) {
		this.poidsVide = poidsVide.movePointRight(3).intValue();
	}

	public RucheType getType() {
		return type;
	}

	public void setType(RucheType type) {
		this.type = type;
	}

	public Essaim getEssaim() {
		return essaim;
	}

	public void setEssaim(Essaim essaim) {
		this.essaim = essaim;
	}

	public Rucher getRucher() {
		return rucher;
	}

	public void setRucher(Rucher rucher) {
		this.rucher = rucher;
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

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}
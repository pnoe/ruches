package ooioo.ruches.hausse;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import ooioo.ruches.ruche.Ruche;

@Entity
public class Hausse {

	@Override
	public String toString() {
		return "Hausse [id=" + id + ", ruche=" + ((ruche == null)?"null":ruche.getNom()) +
				", ordreSurRuche=" + ordreSurRuche + ", active=" + active
				+ ", nom=" + nom + ", Acquisition=" + dateAcquisition + ", nbCadres=" + nbCadres + ", nbCadresMax="
				+ nbCadresMax + ", poidsVide=" + poidsVide + ", commentaire=" + commentaire + "]";
	}

	public Hausse() {
	}

	/**
	 * Clonage d'une hausse
	 */
	public Hausse(Hausse hausse, String nom) {
		// champs ignorés : ruche et ordreSurRuche
		// champs modifiés
		this.nom = nom;
		// champs identiques
		this.active = hausse.getActive();
		this.dateAcquisition = hausse.getDateAcquisition();
		this.nbCadres = hausse.getNbCadres();
		this.nbCadresMax = hausse.getNbCadresMax();
		this.setPoidsVide(hausse.getPoidsVide());
		this.commentaire = hausse.commentaire;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Version
	private Integer version;

	/**
	 * La ruche sur laquelle est posée la hausse
	 */
	@ManyToOne
	private Ruche ruche;

	/**
	 * Numéro d'ordre de la hausse sur la ruche
	 */
	private Integer ordreSurRuche;

	/**
	 * Hausse active ?
	 */
	private boolean active = true;

	@Column(nullable = false, unique = true)
	private String nom;

	/**
	 * Date d'acquisition de la hausse
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateAcquisition;

	/**
	 * Nombre de cadres dans la hausse
	 */
	private Integer nbCadres = 9;

	/**
	 * Nombre de cadres maxi dans la hausse
	 */
	private Integer nbCadresMax = 9;

	/**
	 * Poids de la hausse sans cadres en grammes
	 */
	private Integer poidsVide = 5000;

	/**
	 * Commentaires
	 */
	private String commentaire;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Ruche getRuche() {
		return ruche;
	}

	public void setRuche(Ruche ruche) {
		this.ruche = ruche;
	}

	public Integer getOrdreSurRuche() {
		return ordreSurRuche;
	}

	public void setOrdreSurRuche(Integer ordreSurRuche) {
		this.ordreSurRuche = ordreSurRuche;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public LocalDate getDateAcquisition() {
		return dateAcquisition;
	}

	public void setDateAcquisition(LocalDate dateAcquisition) {
		this.dateAcquisition = dateAcquisition;
	}

	public Integer getNbCadres() {
		return nbCadres;
	}

	public void setNbCadres(Integer nbCadres) {
		this.nbCadres = nbCadres;
	}

	public Integer getNbCadresMax() {
		return nbCadresMax;
	}

	public void setNbCadresMax(Integer nbCadresMax) {
		this.nbCadresMax = nbCadresMax;
	}

	public BigDecimal getPoidsVide() {
		return new BigDecimal(poidsVide).movePointLeft(3);
	}

	public void setPoidsVide(BigDecimal poidsVide) {
		this.poidsVide = poidsVide.movePointRight(3).intValue();
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
package ooioo.ruches.ruche.type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class RucheType {

	@Override
	public String toString() {
		return "RucheType [id=" + id + ", nom=" + nom + ", nbCadresMax=" + nbCadresMax + ", commentaire=" + commentaire
				+ "]";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Version
	private Integer version;

	@Column(nullable = false, unique = true)
	private String nom;

	/**
	 * Nombre de cadres maxi
	 */
	private Integer nbCadresMax = 10;

	/**
	 * Commentaire
	 */
	private String commentaire;

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

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

	public Integer getNbCadresMax() {
		return nbCadresMax;
	}

	public void setNbCadresMax(Integer nbCadresMax) {
		this.nbCadresMax = nbCadresMax;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
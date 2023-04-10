package ooioo.ruches.ruche.type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RucheType {

	@Override
	public String toString() {
		return "RucheType [id=" + id + ", nom=" + nom + ", nbCadresMax=" + nbCadresMax + ", commentaire=" + commentaire + "]";
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

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

}
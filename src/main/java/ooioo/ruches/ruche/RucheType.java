package ooioo.ruches.ruche;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RucheType {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String nom;
	
	/**
	 * Nombre de cadres maxi
	 */
	private Integer nbCadresMax = 10;

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
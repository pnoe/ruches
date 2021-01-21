package ooioo.ruches;

/**
 * Projection Id/Nom
 */
public class IdNom {
	private Long id;

	private String nom;

	public Long getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public IdNom(Long id, String nom) {
		this.id = id;
		this.nom = nom;
	}

}
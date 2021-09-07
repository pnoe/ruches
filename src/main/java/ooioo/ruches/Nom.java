package ooioo.ruches;

/**
 * Pour contrôle d'unicité des noms (ruche, essaim, hausse, personne, rucher)
 *  dans les formulaires de création/modification
 */
public class Nom {
	private String nom;

	public String getNom() {
		return nom;
	}

	public Nom(String nom) {
		this.nom = nom;
	}

}
package ooioo.ruches.personne;

/**
 * Pour contrôle d'unicité des emails dans les formulaires de
 * création/modification
 */
public class PersonneEmail {
	private String email;

	public PersonneEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
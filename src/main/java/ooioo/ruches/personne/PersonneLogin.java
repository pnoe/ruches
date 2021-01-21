package ooioo.ruches.personne;

/**
 * Pour contrôle d'unicité des login
 *  dans les formulaires de création/modification
 */
public class PersonneLogin {
	private String login;

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}
	
	public PersonneLogin(String login) {
		this.login = login;
	}
	
}
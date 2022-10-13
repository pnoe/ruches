package ooioo.ruches.personne;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity // This tells Hibernate to make a table out of this class
public class Personne {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, unique = true)
	private String login;

	private String password;

	/**
	 * Si roles = "admin" la personne peut modifier les autres personnes,
	 *   re-déployer l'application, charger des dumps database
	 */
	private String roles;

	/**
	 * Nom
	 */
	@Column(nullable = false)
	private String nom;

	/**
	 * Prénom
	 */
	@Column(nullable = false)
	private String prenom;

	/**
	 * Adresse
	 */
	@Column(nullable = false)
	private String adresse;

	/**
	 * Numéro de téléphone
	 */
	private String tel;

	/**
	 * Email
	 */
	@Column(unique = true)
	private String email;

	/**
	 * Persoone active ?
	 */
	private boolean active = true;

	/**
	 * Token pour mot de passe oublié
	 */
	private String token;

	/**
	 * Date d'expiration du token
	 */
	private LocalDateTime tokenexpiration;

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

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LocalDateTime getTokenexpiration() {
		return tokenexpiration;
	}

	public void setTokenExpiration(LocalDateTime tokenexpiration) {
		this.tokenexpiration = tokenexpiration;
	}

}
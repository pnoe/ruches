package ooioo.ruches.essaim;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
public class Essaim {
	
	public Essaim() {
	}

	/**
	 * Clonage d'un essaim
	 */
	public Essaim(Essaim essaim, String nom) {
		// champs modifiés
		this.nom = nom;
		// champs identiques
		this.actif = essaim.getActif();
		this.dateAcquisition = essaim.getDateAcquisition();
		this.commentaire = essaim.getCommentaire();
		this.reineDateNaissance = essaim.getReineDateNaissance();
		this.reineMarquee = essaim.getReineMarquee();
		this.souche = essaim.getSouche();
		this.agressivite = essaim.getAgressivite();
		this.proprete = essaim.getProprete();
	}
	
	public Essaim(String nom, boolean actif, LocalDate dateAcquisition,
			String commentaire, LocalDate reineDateNaissance, boolean reineMarquee,
			Essaim souche, Integer agressivite, Integer proprete) {
		this.nom = nom;
		this.actif = actif;
		this.dateAcquisition = dateAcquisition;
		this.commentaire = commentaire;
		this.reineDateNaissance = reineDateNaissance;
		this.reineMarquee = reineMarquee;
		this.souche = souche;
		this.agressivite = agressivite;
		this.proprete = proprete;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Le nom de l'essaim
	 */
	@Column(nullable = false, unique = true)
	private String nom;

	/**
	 * Date d'acquisition de l'essaim
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateAcquisition;

	/**
	 * Essaim actif
	 */
	private boolean actif = true;

	/**
	 * Commentaire
	 */
	private String commentaire;

	/**
	 * Date de naissance de la reine
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate reineDateNaissance;

	/**
	 * La reine est-elle marquée ?
	 */
	private boolean reineMarquee = false;
	
	/**
	 * Souche (parent) de l'essaim
	 */
	@ManyToOne
	private Essaim souche;
	
	/**
	 * Agressivité de l'essaim
	 *   de 1 à 5
	 */
	private Integer agressivite;
	
	/**
	 * Propreté de l'essaim
	 *   de 1 à 5
	 */
	private Integer proprete;
	

	/**
	 * Calcule la couleur de marquage de la reine d'après sa date de naissance
	 * 
	 * @return
	 */
	public ReineCouleurMarquage getReineCouleurMarquage() {
		return (this.reineDateNaissance == null) ? ReineCouleurMarquage.ORANGE
				: ReineCouleurMarquage.values()[(this.reineDateNaissance.getYear()) % 5];
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

	public LocalDate getDateAcquisition() {
		return dateAcquisition;
	}

	public void setDateAcquisition(LocalDate dateAcquisition) {
		this.dateAcquisition = dateAcquisition;
	}

	public boolean getActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

	public LocalDate getReineDateNaissance() {
		return reineDateNaissance;
	}

	public void setReineDateNaissance(LocalDate reineDateNaissance) {
		this.reineDateNaissance = reineDateNaissance;
	}

	public boolean getReineMarquee() {
		return reineMarquee;
	}

	public void setReineMarquee(boolean reineMarquee) {
		this.reineMarquee = reineMarquee;
	}

	public Essaim getSouche() {
		return souche;
	}

	public void setSouche(Essaim souche) {
		this.souche = souche;
	}

	public Integer getAgressivite() {
		return agressivite;
	}

	public void setAgressivite(Integer agressivite) {
		this.agressivite = agressivite;
	}

	public Integer getProprete() {
		return proprete;
	}

	public void setProprete(Integer proprete) {
		this.proprete = proprete;
	}

}
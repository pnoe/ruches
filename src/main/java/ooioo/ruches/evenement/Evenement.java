package ooioo.ruches.evenement;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.format.annotation.DateTimeFormat;

import ooioo.ruches.Const;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@Entity
public class Evenement {

	public Evenement() {
	}

	public Evenement(LocalDateTime date, TypeEvenement typeEvenement, Ruche ruche,
			Essaim essaim, Rucher rucher, Hausse hausse, String valeur,	String commentaire) {
		this.date = date;
		this.type = typeEvenement;
		this.ruche = ruche;
		this.rucher = rucher;
		this.essaim = essaim;
		this.hausse = hausse;
		this.valeur = valeur;
		this.commentaire = commentaire;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Date de l'événement
	 */
	@DateTimeFormat(pattern = Const.YYYYMMDDHHMM)
	private LocalDateTime date;

	/**
	 * Le type d'événement : ruche : déplacement dans un rucher hausse : pose sur
	 * une ruche, contrôle du remplissage essaim : traitement, sucre
	 */
	private TypeEvenement type;

	@ManyToOne
	private Ruche ruche;

	/**
	 * Essaim dans la ruche
	 */
	@ManyToOne
	private Essaim essaim;

	/*
	 * Rucher où est installée la ruche
	 */
	@ManyToOne
	private Rucher rucher;

	/*
	 * Hausse
	 */
	@ManyToOne
	private Hausse hausse;

	/**
	 * La valeur de l'événement :
	 *   poids de sucre ajouté, taux de remplissage d'une hausse,
	 *    nombre de languettes pour le traitement varoa,
	 *    ...
	 */
	@Column(length = 64)
	private String valeur;

	/**
	 * Commentaire
	 */
	private String commentaire = "";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public TypeEvenement getType() {
		return type;
	}

	public void setType(TypeEvenement typeEvenement) {
		this.type = typeEvenement;
	}

	public Ruche getRuche() {
		return ruche;
	}

	public void setRuche(Ruche ruche) {
		this.ruche = ruche;
	}

	public Essaim getEssaim() {
		return essaim;
	}

	public void setEssaim(Essaim essaim) {
		this.essaim = essaim;
	}

	public Rucher getRucher() {
		return rucher;
	}

	public void setRucher(Rucher rucher) {
		this.rucher = rucher;
	}

	public Hausse getHausse() {
		return hausse;
	}

	public void setHausse(Hausse hausse) {
		this.hausse = hausse;
	}

	public String getValeur() {
		return valeur;
	}

	public void setValeur(String valeur) {
		this.valeur = valeur;
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}
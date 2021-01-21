package ooioo.ruches.recolte;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

import ooioo.ruches.Const;

@Entity
public class Recolte {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * Date de la récolte
	 */
	@DateTimeFormat(pattern = Const.YYYYMMDDHHMM)
	private LocalDateTime date;

	/**
	 * Type de miel
	 */
	@Enumerated
	private TypeMiel typeMiel;

	/**
	 * Poids de miel mis en pot
	 */
	private Integer poidsMiel = 0;

	/**
	 * Commentaire
	 */
	private String commentaire;

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

	public TypeMiel getTypeMiel() {
		return typeMiel;
	}

	public void setTypeMiel(TypeMiel typeMiel) {
		this.typeMiel = typeMiel;
	}

	public BigDecimal getPoidsMiel() {
		return new BigDecimal(poidsMiel).movePointLeft(3);
	}

	public Integer getIntPoidsMiel() {
		return this.poidsMiel;
	}
	
	public void setPoidsMiel(BigDecimal poidsMiel) {
		this.poidsMiel = poidsMiel.movePointRight(3).intValue();
	}

	public String getCommentaire() {
		return commentaire;
	}

	public void setCommentaire(String commentaire) {
		this.commentaire = commentaire;
	}

}
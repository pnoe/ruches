package ooioo.ruches.recolte;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import ooioo.ruches.essaim.Essaim;
import ooioo.ruches.hausse.Hausse;
import ooioo.ruches.ruche.Ruche;
import ooioo.ruches.rucher.Rucher;

@Entity
public class RecolteHausse {

	public RecolteHausse() {
		// constructeur par défaut nécessaire pour recolteDetail
	}

	public RecolteHausse(Recolte recolte, Hausse hausse, BigDecimal poidsAvant, BigDecimal poidsApres) {
		this.recolte = recolte;
		this.hausse = hausse;
		this.setPoidsAvant(poidsAvant);
		this.setPoidsApres(poidsApres);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Recolte recolte;

	/**
	 * Poids de la hausse avant récolte, en g. Attention get et set en kg.
	 */
	private Integer poidsAvant = 0;

	/**
	 * Poids de la hausse après récolte, en g. Attention get et set en kg.
	 */
	private Integer poidsApres = 0;

	/**
	 * La hausse dont on a extrait le miel
	 */
	@ManyToOne
	private Hausse hausse;

	/**
	 * La ruche associée à la hausse au moment de la récolte
	 */
	@ManyToOne
	private Ruche ruche;

	/**
	 * L'essaim associée à la ruche au moment de la récolte
	 */
	@ManyToOne
	private Essaim essaim;

	/**
	 * Le rucher associée à la ruche au moment de la récolte ... en fait avant la
	 * récolte, au moment de la récolte c'est "Dépôt" !
	 */
	@ManyToOne
	private Rucher rucher;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Recolte getRecolte() {
		return recolte;
	}

	public void setRecolte(Recolte recolte) {
		this.recolte = recolte;
	}
	
	public Integer getPdsAvantGr()  {
		return poidsAvant;
	}

	public Integer getPdsApresGr()  {
		return poidsApres;
	}

	public BigDecimal getPoidsAvant() {
		return new BigDecimal(poidsAvant).movePointLeft(3);
	}

	public void setPoidsAvant(BigDecimal poidsAvant) {
		this.poidsAvant = poidsAvant.movePointRight(3).intValue();
	}

	public BigDecimal getPoidsApres() {
		return new BigDecimal(poidsApres).movePointLeft(3);
	}

	public void setPoidsApres(BigDecimal poidsApres) {
		this.poidsApres = poidsApres.movePointRight(3).intValue();
	}

	public Hausse getHausse() {
		return hausse;
	}

	public void setHausse(Hausse hausse) {
		this.hausse = hausse;
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

	@Override
	public String toString() {
		return "RecolteHausse [id=" + id + ", recolte=" + recolte.getDate() + ", poidsAvant=" + poidsAvant
				+ ", poidsApres=" + poidsApres + ", hausse=" + hausse.getNom() + ", ruche="
				+ ((ruche == null) ? "null" : ruche.getNom()) + ", essaim="
				+ ((essaim == null) ? "null" : essaim.getNom()) + ", rucher="
				+ ((rucher == null) ? "null" : rucher.getNom()) + "]";
	}

}
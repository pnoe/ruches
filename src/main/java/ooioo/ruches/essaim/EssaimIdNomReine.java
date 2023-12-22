package ooioo.ruches.essaim;

import java.time.LocalDate;

public class EssaimIdNomReine {

	public EssaimIdNomReine(Long id, String nom, LocalDate reineDateNaissance, boolean reineMarquee) {
		this.id = id;
		this.nom = nom;
		this.reineDateNaissance = reineDateNaissance;
		this.reineMarquee = reineMarquee;
	}

	private Long id;

	private String nom;

	private LocalDate reineDateNaissance;

	private boolean reineMarquee = false;

	public ReineCouleurMarquage getReineCouleurMarquage() {
		return (this.reineDateNaissance == null) ? ReineCouleurMarquage.ORANGE
				: ReineCouleurMarquage.values()[(this.reineDateNaissance.getYear()) % 5];
	}

	public Long getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public boolean getReineMarquee() {
		return reineMarquee;
	}

}
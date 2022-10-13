package ooioo.ruches.rucher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

// Distances et temps de parcours en voiture entre les ruchers
@Entity
public class DistRucher {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Rucher rucherStart;

	@ManyToOne
	private Rucher rucherEnd;

	// Distance en mètres
	@Column(nullable = false)
	private Integer dist = 0;

	// Temps en minutes
	@Column(nullable = false)
	private Integer temps = 0;

	public DistRucher() {
	}

	public DistRucher(Rucher rucherStart, Rucher rucherEnd, Integer dist, Integer temps) {
		this.rucherStart = rucherStart;
		this.rucherEnd = rucherEnd;
		this.dist = dist;
		this.temps = temps;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Rucher getRucherStart() {
		return rucherStart;
	}

	public void setRucherStart(Rucher rucherStart) {
		this.rucherStart = rucherStart;
	}

	public Rucher getRucherEnd() {
		return rucherEnd;
	}

	public void setRucherEnd(Rucher rucherEnd) {
		this.rucherEnd = rucherEnd;
	}

	public Integer getDist() {
		return dist;
	}

	public void setDist(Integer dist) {
		this.dist = dist;
	}

	public Integer getTemps() {
		return temps;
	}

	public void setTemps(Integer temps) {
		this.temps = temps;
	}

}
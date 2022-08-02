package ooioo.ruches.rucher;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DistRucher {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Rucher rucherStart;

	@ManyToOne
	private Rucher rucherEnd;

	@Column(nullable = false)
	private Integer dist = 0;

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
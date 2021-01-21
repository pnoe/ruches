package ooioo.ruches.essaim;

public class EssaimTree {
	
	public EssaimTree() {
	}

	public EssaimTree(String name, String parent, Long id, String couleurReine, 
			String nomRuche, String nomRucher, 
			Integer poidsMiel, Integer poidsMielDescendance) {
		this.name = name;
		this.parent = parent;
		this.id = id;
		this.couleurReine = couleurReine;
		this.nomRuche = nomRuche;
		this.nomRucher = nomRucher;
		this.poidsMiel = poidsMiel;
		this.poidsMielDescendance = poidsMielDescendance;
	}

	/**
	 * Nom de l'essaim
	 */
	private String name;
	/**
	 *  Nom de l'essaim parent (souche)
	 */
	private String parent;
	/* 
	 * Id de l'essaim
	 */
	private Long id;
	/*
	 * Couleur du marquage de la reine
	 */
	private String couleurReine;
	/**
	 * Nom de la ruche
	 */
	private String nomRuche;
	/**
	 * Nom du rucher
	 */
	private String nomRucher;

	private Integer poidsMiel;
	
	private Integer poidsMielDescendance;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCouleurReine() {
		return couleurReine;
	}

	public void setCouleurReine(String couleurReine) {
		this.couleurReine = couleurReine;
	}

	public String getNomRuche() {
		return nomRuche;
	}

	public void setNomRuche(String nomRuche) {
		this.nomRuche = nomRuche;
	}

	public String getNomRucher() {
		return nomRucher;
	}

	public void setNomRucher(String nomRucher) {
		this.nomRucher = nomRucher;
	}

	public Integer getPoidsMiel() {
		return poidsMiel;
	}

	public void setPoidsMiel(Integer poidsMiel) {
		this.poidsMiel = poidsMiel;
	}

	public Integer getPoidsMielDescendance() {
		return poidsMielDescendance;
	}

	public void setPoidsMielDescendance(Integer poidsMielDescendance) {
		this.poidsMielDescendance = poidsMielDescendance;
	}

}
package ooioo.ruches.essaim;

import java.util.ArrayList;
import java.util.List;

public class EssaimTreeNode {

	public EssaimTreeNode(Essaim essaim) {
		this.essaim = essaim;
		this.enfants = new ArrayList<>();
	}

	private Essaim essaim;

	private List<EssaimTreeNode> enfants;

	public Essaim getEssaim() {
		return essaim;
	}

	public void setEssaim(Essaim essaim) {
		this.essaim = essaim;
	}

	public List<EssaimTreeNode> getEnfants() {
		return enfants;
	}

	public void setEnfants(List<EssaimTreeNode> enfants) {
		this.enfants = enfants;
	}

}
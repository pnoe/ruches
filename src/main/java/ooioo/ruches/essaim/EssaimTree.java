package ooioo.ruches.essaim;

import java.time.LocalDate;

public record EssaimTree(
		String name,
		String parent,
		Long id,
		boolean actif,
		LocalDate dateAcquisition,
		String couleurReine,
		String nomRuche,
		String nomRucher,
		Integer poidsMiel,
		Integer poidsMielDescendance) {
}
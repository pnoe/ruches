package ooioo.ruches.rucher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record Transhumance(
		// Le rucher si transhumances de tous les ruchers
		Rucher rucher,
		// Le type de mouvement, true : Ajout dans le rucher, false : Retrait du rucher
		boolean type,
		// La date de l'événement ou du premier événement si groupe
		// Date java et formattage dans le template ?
		LocalDateTime date,
		// Le ou les nom(s) de rucher(s) de destination (Retrait) ou de provenance (Ajout) ou
		// "Inconnue" (localisation à faire, attention on peut avoir Inconnue et d'autre noms de ruchers)
		Set<String> destProv,
		// le nom de la ruche ajoutée ou retirée (ou des ruches)
		List<String> ruche,
		// les noms des ruches dans le rucher
		List<String> etat,
		// l'id de l'événement ou du premier événement si groupe
		Long eveid) {
}

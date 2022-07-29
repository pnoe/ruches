package ooioo.ruches.rucher;

import java.time.LocalDateTime;

public record Transhumance(
		// Le rucher si transhumances de tous les ruchers
		Rucher rucher,
		// le type de mouvement, true : Ajout dans le rucher, false : Retrait du rucher
		boolean type,
		// la date de l'événement ou du premier événement si groupe
		// Date java et formattage dans le template ?
		LocalDateTime date,
		// le nom du rucher de destination (Retrait) ou de provenance (Ajout) ou
		// "Inconnue" (localisation à faire)
		String destProv,

		// le nombre de ruches ajoutées ou retirées
		int nbMouv,
		// le nom de la ruche ajoutée ou retirée (ou des ruches, séparés par un espace)
		String ruche,

		// le nombre de ruches dans le rucher après l'ajout ou le retrait
		int nbruches,
		// les noms des ruches dans le rucher séparés par un espace
		String etat,

		// l'id de l'événement ou du premier événement si groupe
		Long eveid

) {}

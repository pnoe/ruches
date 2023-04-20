/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals _csrf_token,_csrf_param_name, essaimnoms, ruchesvidesnoms, recolteHausses, rucheEssaim,
	essaimsupprecolttxt, suppessaimevetxt, suppessaimtxt,  evenements, nomessaimsvirgule,
	nexistPasOuPasVidtxt, nomexistdejatxt, Ruchestxt, Essaimstxt, creertxt, urlessaimclone,
	essaimid, pasderuchetxt
*/
"use strict";
function essaimDetail() {
	$('#dispersion').on('click', function() {
		if (!rucheEssaim) {
			alert(pasderuchetxt);
			return false;
		}
		return true;
	});

	$('#essaime').on('click', function() {
		if (!rucheEssaim) {
			alert(pasderuchetxt);
			return false;
		}
		return true;
	});

	$('#supprime').on('click', function() {
		if (recolteHausses) {
			alert(essaimsupprecolttxt);
			return false;
		} else if (evenements) {
			return confirm(suppessaimevetxt);
		} else {
			return confirm(suppessaimtxt);
		}
	});

	$("#clone").on('click', function() {
		function annule() {
			while (iajout--) {
				essaimnoms.pop();
			}
			ruchesvidesnoms.push(...ruchesvidesmem);
		}
		// Saisie des couples nomEssaim;nomRuche séparés par une ","
		const noms = prompt(nomessaimsvirgule);
		if (noms == null || noms == "") {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		const tabNomRucheOk = [];
		const tabNomRucheIncorrect = [];

		// pour mémo ajout essaim dans essaimnoms
		let iajout = 0;
		// pour retrait ruche vide
		const ruchesvidesmem = []; 

		//  filter : on ignore ",," ou ",  ,"
		const nomsarr = noms.split(',').filter(s => s.trim());
		for (const item of nomsarr) {
			const arr = item.split(';');
			// ne nom de l'essaim
			const ne = arr[0].trim();
			// Si le nom de ruche est vide ou blanc, on passe à la ruche suivante
			if ("" === ne) { continue; }
			let nr = "";
			// nr nom de la ruche
			if (arr.length > 1) {
				nr = arr[1].trim();
			}
			if ($.inArray(ne, essaimnoms) != -1) {
				// l'essaim ne existe déjà
				tabNomExiste.push(ne);
				if ((nr != "") && ($.inArray(nr, ruchesvidesnoms) == -1)) {
					// ruche nr incorrecte
					tabNomRucheIncorrect.push(nr);
				}
			} else {
				// on crée l'essaim ne
				tabNomOk.push(ne);
				// ce nom ne doit pas être réutilisé
				essaimnoms.push(ne);
				iajout++;
				if (nr == "") {
					// pas de ruche
					tabNomRucheOk.push("");
				} else if ($.inArray(nr, ruchesvidesnoms) == -1) {
					// ruche nr incorrecte
					tabNomRucheOk.push("");
					tabNomRucheIncorrect.push(nr);
				} else {
					// on met l'essaim ne dans la ruche nr
					tabNomRucheOk.push(nr);
					// ce nom ne doit pas être réutilisé, on l'enlève de la
					//	liste des ruches vides
					const index = ruchesvidesnoms.indexOf(nr);
					if (index > -1) {
						ruchesvidesnoms.splice(index, 1);

						// IL faudra la remettre si annulation du clone...
						ruchesvidesmem.push(nr);

					}
				}
			}
		}
		let prompterreur = "";
		if (tabNomRucheIncorrect.length > 0) {
			prompterreur = Ruchestxt + ' : ' + tabNomRucheIncorrect.join(',') + ' ' + nexistPasOuPasVidtxt;
		}
		if (tabNomExiste.length > 0) {
			if (prompterreur != "") { prompterreur += '\n'; }
			prompterreur += Essaimstxt + ' : ' + tabNomExiste.join(',') + ' ' + nomexistdejatxt;
			if (tabNomOk.length === 0) {
				alert(prompterreur);
				// Pas d'essaim ajouté
				return false;
			}
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return false;
			}
		} else if (tabNomRucheIncorrect.length > 0) {
			// Il n'y a pas d'erreur sur les noms d'essaim mais il y en a sur les 
			//   noms de ruche
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return false;
			}
		}
		if (tabNomOk.length === 0) { return; }
		const requestData = { nomclones: tabNomOk.join(','), nomruches: tabNomRucheOk.join(',') };
		requestData[_csrf_param_name] = _csrf_token;
		$.post(urlessaimclone + essaimid,
			requestData).done(function(data) {
				alert(data);
				document.location.reload(true);
			});
	});
}
/* globals _csrf_token,_csrf_param_name, essaimnoms, ruchesvidesnoms, recolteHausses, rucheEssaim,
	essaimsupprecolttxt, suppessaimevetxt, suppessaimtxt,  evenements, nomessaimsvirgule,
	nexistPasOuPasVidtxt, nomexistdejatxt, Ruchestxt, Essaimstxt, creertxt, urlessaimclone,
	essaimid, pasderuchetxt */
'use strict';
document.addEventListener('DOMContentLoaded', () => {

	document.getElementById('dispersion').addEventListener('click', function(event) {
		if (!rucheEssaim) {
			alert(pasderuchetxt);
			event.preventDefault();
		}
	});

	document.getElementById('essaime').addEventListener('click', function(event) {
		if (!rucheEssaim) {
			alert(pasderuchetxt);
			event.preventDefault();
		}
	});

	document.getElementById('supprime').addEventListener('click', function(event) {
		if (recolteHausses) {
			alert(essaimsupprecolttxt);
			event.preventDefault();
		} else if (evenements) {
			if (!confirm(suppessaimevetxt)) { event.preventDefault(); }
		} else {
			if (!confirm(suppessaimtxt)) { event.preventDefault(); }
		}
	});

	// Attention utiliser  event.preventDefault()
	//   pour supprimer $('#clone').on('click'
	$('#clone').on('click', function() {
		// pour mémo ajout essaim dans essaimnoms
		let iajout = 0;
		// pour retrait ruche vide
		const ruchesvidesmem = [];

		function annule() {
			while (iajout--) {
				essaimnoms.pop();
			}
			ruchesvidesnoms.push(...ruchesvidesmem);
		}
		// Saisie des couples nomEssaim;nomRuche séparés par une ","
		const noms = prompt(nomessaimsvirgule);
		if (noms === null || noms === '') {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		const tabNomRucheOk = [];
		const tabNomRucheIncorrect = [];


		//  filter : on ignore ",," ou ",  ,"
		const nomsarr = noms.split(',').filter(s => s.trim());
		for (const item of nomsarr) {
			const arr = item.split(';');
			// ne nom de l'essaim
			const ne = arr[0].trim();
			// Si le nom de ruche est vide ou blanc, on passe à la ruche suivante
			if ('' === ne) { continue; }
			let nr = '';
			// nr nom de la ruche
			if (arr.length > 1) {
				nr = arr[1].trim();
			}
			if ($.inArray(ne, essaimnoms) === -1) {
				// on crée l'essaim ne
				tabNomOk.push(ne);
				// ce nom ne doit pas être réutilisé
				essaimnoms.push(ne);
				iajout++;
				if (nr === '') {
					// pas de ruche
					tabNomRucheOk.push('');
				} else if ($.inArray(nr, ruchesvidesnoms) === -1) {
					// ruche nr incorrecte
					tabNomRucheOk.push('');
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
			} else {
				// l'essaim ne existe déjà
				tabNomExiste.push(ne);
				if ((nr !== '') && ($.inArray(nr, ruchesvidesnoms) === -1)) {
					// ruche nr incorrecte
					tabNomRucheIncorrect.push(nr);
				}
			}
		}
		let prompterreur = '';
		if (tabNomRucheIncorrect.length > 0) {
			prompterreur = Ruchestxt + ' : ' + tabNomRucheIncorrect.join(',') + ' ' + nexistPasOuPasVidtxt;
		}
		if (tabNomExiste.length > 0) {
			if (prompterreur !== '') { prompterreur += '\n'; }
			prompterreur += Essaimstxt + ' : ' + tabNomExiste.join(',') + ' ' + nomexistdejatxt;
			if (tabNomOk.length === 0) {
				alert(prompterreur);
				// Pas d'essaim ajouté
				return;
			}
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
			}
		} else if (tabNomRucheIncorrect.length > 0) {
			// Il n'y a pas d'erreur sur les noms d'essaim mais il y en a sur les 
			//   noms de ruche
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
			}
		} else if (tabNomOk.length === 0) {
			return;
		} else {
			if (!confirm(creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
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
});
/* globals _csrf_token, _csrf_param_name, ruchenoms, rnpsupprec,
	recolteHausses, evenements, suppruche, nruchvirg,
	nexisted, Ruches, creer, urlclone, rucheid, rnpeves
 */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('supprime').addEventListener('click', event => {
		// Supprimer la ruche si :
		//  - elle n'a pas participé à des récoltes)
		//  - aucun événement ne la référence
		if (recolteHausses) {
			// recolteHausses true s'il existe des hausses de récolte associées à cette ruche
			alert(rnpsupprec);
		} else if (evenements) {
			alert(rnpeves);
		} else if (confirm(suppruche)) {
			return;
		}
		event.preventDefault();
	});

	document.getElementById('clone').addEventListener('click', () => {
		// Clone de la ruche
		let iajout = 0;
		function annule() {
			while (iajout--) {
				ruchenoms.pop();
			}
		}
		// Saisi des noms des ruches à créer séparés par une virgule.
		const noms = prompt(nruchvirg);
		if (!noms) {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		// filter supprime les noms vides ou blancs
		// map trim supprime les blancs aux extrémités des noms
		noms.split(',').filter(s => s.trim()).map(item => item.trim()).forEach(function(item) {
			if (ruchenoms.includes(item)) {
				tabNomExiste.push(item);
			} else {
				// On va créer la ruche item
				tabNomOk.push(item);
				// Ce nom ne doit pas être réutilisé
				ruchenoms.push(item);
				iajout++;
			}
		});
		if (tabNomExiste.length > 0) {
			const promptexistedeja = `${Ruches} : ${tabNomExiste.join(',')} ${nexisted}`;
			if (tabNomOk.length === 0) {
				alert(promptexistedeja);
				return;
			}
			if (!confirm(`${promptexistedeja}. ${creer} ${tabNomOk.join(',')} ?`)) {
				annule();
				return;
			}
		} else if (tabNomOk.length === 0) {
			return;
		} else if (!confirm(`${creer} ${tabNomOk.join(',')} ?`)) {
			annule();
			return;
		}
		// Clone des ruches, appel de l'url ruche/clone/hausseid
		fetch(`${urlclone}${rucheid}`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams({
				[_csrf_param_name]: _csrf_token,
				nomclones: tabNomOk.join(','),
			})
		})
			.then(response => {
				if (response.ok) {
					return response.text();
				}
				throw new Error('Erreur en réponse du réseau.');
			})
			.then(text => alert(text))
			.catch(error => alert('Fetch error:', error));
	});
});
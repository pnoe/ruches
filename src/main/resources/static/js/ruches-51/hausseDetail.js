/* globals
_csrf_token, _csrf_param_name, haussenoms, recolteHausses, haussesupp, supphausseeve,
supphausse, nomhaussesv, nomexistentdeja,  evenements, urlclone, hausseid, hausses, creer
 */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('supprime').addEventListener('click', event => {
		// Supprimer la hausse, si 
		//  - elle n'a pas participé à des récoltes
		//  - aucun événement ne la référence
		if (recolteHausses) {
			// recolteHausses true s'il existe des hausses de récolte associées à cette hausse
			alert(haussesupp);
		} else if (evenements) {
			alert(supphausseeve);
		} else if (confirm(supphausse)) {
			return;
		}
		event.preventDefault();
	});

	document.getElementById('clone').addEventListener('click', () => {
		// Clone de la hausse
		let iajout = 0;
		function annule() {
			while (iajout--) {
				haussenoms.pop();
			}
		}
		// Saisie des noms des hausses à créer séparés par une virgule.
		const noms = prompt(nomhaussesv);
		if (!noms) {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		// filter supprime les noms vides ou blancs
		// map trim supprime les blancs aux extrémités des noms
		noms.split(',').filter(s => s.trim()).map(item => item.trim()).forEach(function(item) {
			if (haussenoms.includes(item)) {
				tabNomExiste.push(item);
			} else {
				// On va créer la hausse item
				tabNomOk.push(item);
				// Ce nom ne doit pas être réutilisé.
				haussenoms.push(item);
				iajout++;
			}
		});
		if (tabNomExiste.length > 0) {
			const promptexistedeja = `${hausses} : ${tabNomExiste.join(',')} ${nomexistentdeja}`;
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
		// Clone des hausses, appel de l'url hausse/clone/hausseid
		fetch(`${urlclone}${hausseid}`, {
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
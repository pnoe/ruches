/* globals _csrf_token, _csrf_param_name, ruchenoms, rnpsupprec,
	recolteHausses, evenements, suppreven, suppruche, nruchvirg,
	nexisted, Ruches, creer, urlclone, rucheid
 */
/* exported rucheDetail */
'use strict';
function rucheDetail() {
	document.getElementById('supprime').addEventListener('click', (event) => {
		if (recolteHausses) {
			alert(rnpsupprec);
		} else if (evenements > 0) {
			if (confirm(suppreven)) {
				return true;
			}
		} else {
			if (confirm(suppruche)) {
				return true;
			}
		}
		event.preventDefault();
		return false;
	});
	document.getElementById('clone').addEventListener('click', () => {
		function annule() {
			while (iajout--) {
				ruchenoms.pop();
			}
		}
		const noms = prompt(nruchvirg);
		let iajout = 0;
		if (noms === null || noms === '') {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		/*[- filter supprime les noms vides ou blancs -]*/
		/*[- map trim supprime les blancs aux extrémités des noms -]*/
		noms.split(',').filter(s => s.trim()).map(item => item.trim()).forEach(function(item) {
			if ($.inArray(item, ruchenoms) === -1) {
				/*[- on crée la ruche item -]*/
				tabNomOk.push(item);
				/*[- ce nom ne doit pas être réutilisé -]*/
				ruchenoms.push(item);
				iajout++;
			} else {
				tabNomExiste.push(item);
			}
		});
		if (tabNomExiste.length > 0) {
			const promptexistedeja = Ruches + ' : ' + tabNomExiste.join(',') + ' ' + nexisted;
			if (tabNomOk.length === 0) {
				alert(promptexistedeja);
				return;
			}
			if (!confirm(promptexistedeja + '. ' + creer + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
			}
		} else if (tabNomOk.length === 0) {
			return;
		} else {
			if (!confirm(creer + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
			}
		}
		const requestData = { nomclones: tabNomOk.join(',') };
		requestData[_csrf_param_name] = _csrf_token;
		$.post(urlclone + rucheid,
			requestData).done(function(data) {
				alert(data);
				document.location.reload(true);
			});
	});
}
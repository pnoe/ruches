/* globals
_csrf_token	_csrf_param_name haussenoms recolteHausses haussesupp supphausseeve
supphausse nomhaussesv nomexistentdeja  evenements  urlclone  hausseid  hausses  creer
 */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('supprime').addEventListener('click', (event) => {
		if (recolteHausses) {
			alert(haussesupp);
		} else if (evenements) {
			if (confirm(supphausseeve)) {
				return;
			}
		} else if (confirm(supphausse)) {
			return;
		}
		event.preventDefault();
	});
	function nomexiste(value) {
		return $.inArray(value, haussenoms) !== -1;
	}
	$('#clone').click(function() {
		function annule() {
			while (iajout--) {
				haussenoms.pop();
			}
		}
		const noms = prompt(nomhaussesv);
		let iajout = 0;
		if (noms !== null && noms !== '') {
			const tabNomExiste = [];
			const tabNomOk = [];
			noms.split(',').filter(s => s.trim()).map(item => item.trim()).forEach(function(item) {
				if (nomexiste(item)) {
					tabNomExiste.push(item);
				} else {
					tabNomOk.push(item);
					/*[- ce nom ne doit pas être réutilisé -]*/
					haussenoms.push(item);
					iajout++;
				}
			});
			if (tabNomExiste.length > 0) {
				const promptexistedeja = hausses + ' : ' + tabNomExiste.join(',') + ' ' + nomexistentdeja;
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
			} else if (!confirm(creer + ' ' + tabNomOk.join(',') + ' ?')) {
				annule();
				return;
			}
			const requestData = { nomclones: tabNomOk.join(',') };
			requestData[_csrf_param_name] = _csrf_token;
			$.post(urlclone + hausseid,
				requestData).done(function(data) {
					alert(data);
					document.location.reload(true);
				});
		}
	});
});
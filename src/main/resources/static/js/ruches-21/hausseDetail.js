/* globals
_csrf_token, _csrf_param_name, haussenoms, recolteHausses, haussesupp, supphausseeve,
supphausse, nomhaussesv, nomexistentdeja,  evenements, urlclone, hausseid, hausses, creer
 */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('supprime').addEventListener('click', event => {
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

	document.getElementById('clone').addEventListener('click', () => {
		let iajout = 0;
		function annule() {
			while (iajout--) {
				haussenoms.pop();
			}
		}
		const noms = prompt(nomhaussesv);
		if (noms === null || noms === '') {
			return;
		}
		const tabNomExiste = [];
		const tabNomOk = [];
		noms.split(',').filter(s => s.trim()).map(item => item.trim()).forEach(function(item) {
			if (haussenoms.includes(item)) {
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
		const req = new XMLHttpRequest();
		req.open('POST', urlclone + hausseid, true);
		// req.setRequestHeader('x-csrf-token', _csrf_token);
		req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					alert(req.responseText);
				}
			}
		};
		req.send(_csrf_param_name + '=' + _csrf_token +
			'&nomclones=' + tabNomOk.join(','));
	});
});
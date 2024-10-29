/* globals
	Hausses, buttTxtPrint, buttTxtCol, urlCommLot, selectHaTrait, dtListe, eveComm
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	// dtListe est dans include.js
	const table = dtListe('hausses', Hausses, buttTxtPrint, buttTxtCol, 'paysage');
	const co = document.getElementById('commentaire');
	function updateLinks(_e, _dt, type) {
		if (type === 'row') {
			let noms = '';
			table.rows({ selected: true }).data().pluck(0).each(function(value) {
				const a = document.createElement('template');
				a.innerHTML = value;
				noms += a.content.children[0].getAttribute('href').
					substring(a.content.children[0].getAttribute('href').lastIndexOf('/') + 1) + ',';
			});
			if (noms) {
				noms = noms.substring(0, noms.length - 1);
				co.setAttribute('href',
					urlCommLot + noms);
			} else {
				co.setAttribute('href', '#');
			}
		}
	}
	table.on('select deselect', updateLinks);
	co.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(eveComm + ' : ' + selectHaTrait);
		}
	});
});
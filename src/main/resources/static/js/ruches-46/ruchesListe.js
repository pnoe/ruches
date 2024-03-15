/* globals
	Ruches, buttTxtPrint, buttTxtCol, urlCommLot, selectRuTrait, dtListe
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	// dtListe est dans include.js
	const table = dtListe('ruches', Ruches, buttTxtPrint, buttTxtCol, 'landscape');
	const co = document.getElementById('commentaire');
	function updateLinks(_e, _dt, type) {
		if (type === 'row') {
			let noms = '';
			table.rows({
				selected: true
			}).data().pluck(0).each(
				function(value) {
					const a = document.createElement('template');
					a.innerHTML = value;
					noms += a.content.children[0].getAttribute('href').substring(
						a.content.children[0].getAttribute('href').lastIndexOf('/') + 1)
						+ ',';
				});
			if (noms) {
				co.setAttribute('href',
					urlCommLot + noms.substring(0, noms.length - 1));
			} else {
				co.setAttribute('href', '#');
			}
		}
	}
	table.on('select deselect', updateLinks);
	co.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selectRuTrait);
		}
	});
});
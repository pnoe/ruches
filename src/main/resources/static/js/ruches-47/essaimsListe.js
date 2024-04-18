/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, urlCadreLot, urlDispLot, selectEssTrt, dtListe */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	// dtListe est dans include.js
	const table = dtListe('essaims', Essaims, buttontextprint, buttontextcol, 'lanscape');
	const tr = document.getElementById('traitement');
	const su = document.getElementById('sucre');
	const co = document.getElementById('commentaire');
	const ca = document.getElementById('cadre');
	
	const di = document.getElementById('dispersion');
	
	function updateLinks(_e, _dt, type) { // }, indexes) {
		if (type === 'row') {
			let noms = '';
			table.rows({ selected: true }).data().pluck(0).each(function(value) { // , index) {
				const a = document.createElement('template');
				a.innerHTML = value;
				noms += a.content.children[0].getAttribute('href').
					substring(a.content.children[0].getAttribute('href').lastIndexOf('/') + 1) + ',';
			});
			if (noms) {
				// On enlève la dernière virgule et met à jour les urls de traitement.
				noms = noms.substring(0, noms.length - 1);
				tr.setAttribute('href', urlTraitLot + noms);
				su.setAttribute('href', urlSucreLot + noms);
				co.setAttribute('href', urlCommLot + noms);
				ca.setAttribute('href', urlCadreLot + noms);
				di.setAttribute('href', urlDispLot + noms);
			} else {
				tr.setAttribute('href', '#');
				su.setAttribute('href', '#');
				co.setAttribute('href', '#');
				ca.setAttribute('href', '#');
				di.setAttribute('href', '#');
			}
		}
	}
	table.on('select deselect', updateLinks);
	document.querySelectorAll('.selection').forEach(sel => {
		sel.addEventListener('click', event => {
			if (event.target.getAttribute('href') === '#') {
				alert(selectEssTrt);
			}
		});
	});
});
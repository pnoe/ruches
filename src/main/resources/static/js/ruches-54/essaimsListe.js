/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, urlCadreLot, urlDispLot, urlMarkLot, selectEssTrt, 
	evComm, evSucre, evTrait, evCadre, disp, marq, dtListe */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	// dtListe est dans include.js
	const table = dtListe('essaims', Essaims, buttontextprint, buttontextcol, 'landscape');
	const tr = document.getElementById('traitement');
	const su = document.getElementById('sucre');
	const co = document.getElementById('commentaire');
	const ca = document.getElementById('cadre');
	const di = document.getElementById('dispersion');
	const ma = document.getElementById('marquage');
	const els = [[tr, urlTraitLot], [su, urlSucreLot], [co, urlCommLot]
		, [ca, urlCadreLot], [di, urlDispLot], [ma, urlMarkLot]];
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
				// Enlève la dernière virgule
				noms = noms.substring(0, noms.length - 1);
				// Mise à jour des urls de traitement.
				els.forEach(e => {
					e[0].setAttribute('href', e[1] + noms);
				});
			} else {
				els.forEach(e => {
					e[0].setAttribute('href', '#');
				});
			}
		}
	}
	table.on('select deselect', updateLinks);
	tr.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(evTrait + ' : ' + selectEssTrt);
		}
	});
	su.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(evSucre + ' : ' + selectEssTrt);
		}
	});
	co.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(evComm + ' : ' + selectEssTrt);
		}
	});
	ca.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(evCadre + ' : ' + selectEssTrt);
		}
	});

	di.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(disp + ' : ' + selectEssTrt);
		}
	});
	ma.addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(marq + ' : ' + selectEssTrt);
		} else {
			let nomsStr = '';
			// reEx pour recherche du nom de l'essaim dans link colonne nom de l'essaim
			const reEx = />(.*)</;
			// sel tableau des lignes selectionnées
			const sel = table.rows({ selected: true }).data().toArray();
			// on pourrait voir quels essaims sont déjà marqué pour afficher
			//  essaims déjà marqués / essaims à marquer
			//  et éventuellement ne rien faire si pas d'essaim à marquer
			sel.forEach(e => {
				// on applique reEx à la première colonne et on prend ensuite
				// le deuxième élément correspondant à la capture (.*) de reEx
				nomsStr += reEx.exec(e[0])[1] + ', ';
			});
			nomsStr = nomsStr.substring(0, nomsStr.length - 2);
			if (!confirm('Marquer les essaims ' + nomsStr + ' ?')) {
				event.preventDefault();
			}
		}
	});
});
/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, urlCadreLot, urlDispLot, urlMarkLot, selectEssTrt, 
	dtListe */
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
	let noms = '';
	function updateLinks(_e, _dt, type) { // }, indexes) {
		if (type === 'row') {
			noms = '';
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
				ma.setAttribute('href', urlMarkLot + noms);
			} else {
				tr.setAttribute('href', '#');
				su.setAttribute('href', '#');
				co.setAttribute('href', '#');
				ca.setAttribute('href', '#');
				di.setAttribute('href', '#');
				ma.setAttribute('href', '#');
			}
		}
	}
	table.on('select deselect', updateLinks);
	document.querySelectorAll('.selection').forEach(sel => {
		sel.addEventListener('click', event => {
			if (event.target.getAttribute('href') === '#') {
				alert(selectEssTrt);
			} else if (event.target.getAttribute('id') === 'marquage') {
				let nomsStr = '';
				// reEx pour recherche du nom de l'essaim dans link colonne nom de l'essaim
				const reEx = RegExp('>(.*)<');
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
});
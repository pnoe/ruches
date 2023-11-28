/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, urlCadreLot, selectEssTrt, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const idTbl = '#essaims';
	const table = new DataTable(idTbl, {
		select: { style: 'multi+shift' },
		dom: 'Blftip',
		scrollX: true,
		buttons: [
			'csv',
			{
				extend: 'pdf', exportOptions: { columns: ':visible' },
				title: function() {
					return Essaims + ' ' + new Date().toLocaleDateString() +
						(table.search().length === 0 ? '' : ' <' + table.search() + '>');
				},
				orientation: 'landscape'
			},
			{ extend: 'print', text: buttontextprint },
			{ extend: 'colvis', text: buttontextcol }
		]
	});
	const tr = document.getElementById('traitement');
	const su = document.getElementById('sucre');
	const co = document.getElementById('commentaire');
	const ca = document.getElementById('cadre');
	function updateLinks(e, dt, type) { // }, indexes) {
		if (type === 'row') {
			let noms = '';
			table.rows({ selected: true }).data().pluck(0).each(function(value) { // , index) {
				const a = document.createElement('template');
				a.innerHTML = value;
				noms += a.content.children[0].getAttribute('href').
					substring(a.content.children[0].getAttribute('href').lastIndexOf('/') + 1) + ',';
			});
			if (noms) {
				/*[- on enlève la dernière virgule et met à jour l'url de traitement -]*/
				noms = noms.substring(0, noms.length - 1);
				tr.setAttribute('href', urlTraitLot + noms);
				su.setAttribute('href', urlSucreLot + noms);
				co.setAttribute('href', urlCommLot + noms);
				ca.setAttribute('href', urlCadreLot + noms);
			} else {
				tr.setAttribute('href', '#');
				su.setAttribute('href', '#');
				co.setAttribute('href', '#');
				ca.setAttribute('href', '#');
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
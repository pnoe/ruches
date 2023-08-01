/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, selectEssTrt DataTable */
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
				action: function(e, dt, node, config) {
					config.title = Essaims + ' ' + (new Date()).toLocaleDateString();
					const inputSearch = table.search();
					if (inputSearch.length !== 0) {
						config.title += ' <' + inputSearch + '>';
					}
					$.fn.dataTable.ext.buttons.pdfHtml5.action.call(this, e, dt, node, config);
				},
				orientation: 'landscape'
			},
			{ extend: 'print', text: buttontextprint },
			{ extend: 'colvis', text: buttontextcol }
		]
	});
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
				document.getElementById('traitement').setAttribute('href',
					urlTraitLot + noms);
				document.getElementById('sucre').setAttribute('href',
					urlSucreLot + noms);
				document.getElementById('commentaire').setAttribute('href',
					urlCommLot + noms);
			} else {
				document.getElementById('traitement').setAttribute('href', '#');
				document.getElementById('sucre').setAttribute('href', '#');
				document.getElementById('commentaire').setAttribute('href', '#');
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
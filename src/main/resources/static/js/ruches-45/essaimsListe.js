/* globals Essaims, buttontextprint, buttontextcol, urlTraitLot, urlSucreLot,
	urlCommLot, urlCadreLot, selectEssTrt, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const table = new DataTable('#essaims', {
		select: { style: 'multi+shift' },
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							let title = Essaims + ' ' + (new Date()).toLocaleDateString();
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							doc.content[0].text = title;
						},
						orientation: 'landscape'
					},
					{
						extend: 'print', text: buttontextprint,
						exportOptions: { columns: ':visible' },
					},
					{ extend: 'colvis', text: buttontextcol },
					'pageLength'
				]
			}
		},
		// https://datatables.net/examples/api/multi_filter.html
		initComplete: function() {
			this.api()
				.columns()
				// https://datatables.net/reference/api/columns().every()
				.every(function() {
					const column = this; // indispensable pour addEventListener
					const input = document.createElement('input');
					input.placeholder = column.footer().textContent;
					column.footer().replaceChildren(input);
					input.addEventListener('keyup', () => {
						if (column.search() !== this.value) {
							column.search(input.value).draw();
						}
					});
					const state = column.state.loaded();
					if (state) {
						const val = state.columns[column.index()];
						input.value = val.search.search;
					}
				});
		}
	});
	const tr = document.getElementById('traitement');
	const su = document.getElementById('sucre');
	const co = document.getElementById('commentaire');
	const ca = document.getElementById('cadre');
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
				// On enlève la dernière virgule et met à jour l'url de traitement.
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
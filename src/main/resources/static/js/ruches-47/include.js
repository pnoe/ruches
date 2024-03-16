/* globals urlValMessFr, urlDatatMessFr, datatPageLength, tout, DataTable */
/* exported dtListe */
// eslint errors sur datatables every method	
/* eslint-disable array-callback-return */
'use strict';
function updateTheme() {
	document.querySelector('html').setAttribute('data-bs-theme',
		window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
}
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', updateTheme);
updateTheme();
if ((window.navigator.language.indexOf('fr') !== -1)) {
	const script = document.createElement('script');
	script.src = urlValMessFr;
	script.async = true;
	document.head.appendChild(script);
	$.extend(true, $.fn.dataTable.defaults, {
		language: {
			url: urlDatatMessFr
		},
	});
}
$.extend(true, $.fn.dataTable.defaults, {
	stateSave: true,
	lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, tout]],
	pageLength: parseInt(datatPageLength),
});

function dtListe(idTable, titre, txtPrint, txtCol, orientation) {
	// Pour forcer la visibilité des colonnes en pdf
	// exportOptions: { columns: '0:visible, 1:visible... }
	// car bug datatables avec colspan
	const tbodyTr = document.querySelectorAll('#essaims tbody tr');
	const visib = tbodyTr.length === 0 ? ':visible' :
		[...Array(tbodyTr[0].cells.length).keys()].map(x => x + ':visIdx');
	const table = new DataTable('#' + idTable, {
		select: { style: 'multi+shift' },
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: visib },
						customize: function(doc) {
							let title = titre + ' ' + (new Date()).toLocaleDateString();
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							table
								.columns()
								.every(function() {
									const input = this.footer().firstChild;
									if (input.value.length !== 0) {
										title += '<' + input.placeholder + ': ' + input.value + '>';
									}
								});
							doc.content[0].text = title;
						},
						orientation: orientation
					},
					{
						extend: 'print', text: txtPrint,
						exportOptions: { columns: ':visible' },
						orientation: orientation
					},
					{ extend: 'colvis', text: txtCol },
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
					// Pour initialiser les footers avec les strings de recherche sauvegardées. 
					const state = column.state.loaded();
					if (state) {
						input.value = state.columns[column.index()].search.search;
					}
				});
		}
	});
	return table;
}
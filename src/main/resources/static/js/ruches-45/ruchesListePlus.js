/* globals
	Ruches, buttTxtPrint, buttTxtCol, DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('rucher').addEventListener('change', event => {
		table.columns(4).search(event.target.value).draw();
	});
	const table = new DataTable('#ruchesplus', {
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							const sru = document.getElementById('rucher');
							const valr = sru.options[sru.selectedIndex].value;
							let title = Ruches + ' ' + (new Date()).toLocaleDateString() +
								(table.search().length === 0 ? '' : ' <' + table.search() + '>') +
								(valr === '' ? '' : ' [' + valr + ']');
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							doc.content[0].text = title;
						},
						orientation: 'landscape'
					},
					{
						extend: 'print',
						text: buttTxtPrint,
						exportOptions: { columns: ':visible' },
						orientation: 'landscape'
					},
					{ extend: 'colvis', text: buttTxtCol },
					'pageLength'
				]
			}
		}
	});
});
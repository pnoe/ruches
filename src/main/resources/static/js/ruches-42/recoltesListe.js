/* globals Recoltes, buttontextprint, buttontextcol, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const idTbl = '#recoltes';
	const table = new DataTable(idTbl, {
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							let title = Recoltes + ' ' + (new Date()).toLocaleDateString();
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
		order: [[0, 'desc']]
	});
});
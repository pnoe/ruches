/* globals
	Ruches, rucher, nomrucher, buttTxtPrint, buttTxtCol, DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const table = new DataTable('#ruches', {
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							let title = `${Ruches} ${rucher} ${nomrucher} ${(new Date()).toLocaleDateString()}`;
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							doc.content[0].text = title;
						},
						orientation: 'landscape'
					},
					{
						extend: 'print', text: buttTxtPrint,
						exportOptions: { columns: ':visible' },
					},
					{ extend: 'colvis', text: buttTxtCol },
					'pageLength'
				]
			}
		}
	});
});
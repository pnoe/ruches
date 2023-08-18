/* globals Recoltes, buttontextprint, buttontextcol, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const idTbl = '#recoltes';
	new DataTable(idTbl, {
		dom: 'Blftip',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf',
			exportOptions: {
				columns: ':visible'
			},
			action: function(e, dt, node, config) {
				config.title = Recoltes + ' ' + (new Date()).toLocaleDateString();
				const inputSearch = this.search();
				if (inputSearch.length !== 0) {
					config.title += ' <' + inputSearch + '>';
				}
				$.fn.dataTable.ext.buttons.pdfHtml5.action.call(this, e, dt, node, config);
			}
		}, {
				extend: 'print',
				text: buttontextprint
			}, {
				extend: 'colvis',
				text: buttontextcol
			}],
		order: [[0, 'desc']]
	});
});
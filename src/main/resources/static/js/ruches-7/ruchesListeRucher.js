/* globals
	Ruches rucher nomrucher buttTxtPrint buttTxtCol DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const idTbl = '#ruches';
	new DataTable(idTbl, {
		dom: '<"buttonsData" Blf>t<"buttonsData" ip>',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf', exportOptions: { columns: ':visible' },
			action: function(e, dt, node, config) {
				config.title = `${Ruches} ${rucher} ${nomrucher} ${(new Date()).toLocaleDateString()}`;
				const inputSearch = this.search();
				if (inputSearch.length !== 0) {
					config.title += ' <' + inputSearch + '>';
				}
				$.fn.dataTable.ext.buttons.pdfHtml5.action.call(this, e, dt, node, config);
			},
			orientation: 'landscape'
		}, {
				extend: 'print',
				text: buttTxtPrint,
				exportOptions: { columns: ':visible' },
				orientation: 'landscape'
			},
			{ extend: 'colvis', text: buttTxtCol }
		]
	});
});
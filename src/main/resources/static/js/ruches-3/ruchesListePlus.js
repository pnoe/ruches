/* globals
	Ruches buttTxtPrint buttTxtCol DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('rucher').addEventListener('change', (event) => {
		table.columns(4).search(event.target.value).draw();
	});
	const idTbl = '#ruchesplus';
	const table = new DataTable(idTbl, {
		dom: '<"buttonsData" Blf>t<"buttonsData" ip>',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf', exportOptions: { columns: ':visible' },
			action: function(e, dt, node, config) {
				config.title = Ruches + ' ' + (new Date()).toLocaleDateString();
				const inputSearch = table.search();
				if (inputSearch.length !== 0) {
					config.title += ' <' + inputSearch + '>';
				}
				const sru = document.getElementById('rucher');
				const valr = sru.options[sru.selectedIndex].value;
				if (valr !== '') {
					config.title += ' [' + valr + ']';
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
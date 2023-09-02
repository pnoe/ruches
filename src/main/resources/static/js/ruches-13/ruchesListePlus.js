/* globals
	Ruches buttTxtPrint buttTxtCol DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('rucher').addEventListener('change', event => {
		table.columns(4).search(event.target.value).draw();
	});
	const idTbl = '#ruchesplus';
	const table = new DataTable(idTbl, {
		dom: '<"buttonsData" Blf>t<"buttonsData" ip>',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf', exportOptions: { columns: ':visible' },
			title: function() {
				const sru = document.getElementById('rucher');
				const valr = sru.options[sru.selectedIndex].value;
				return Ruches + ' ' + new Date().toLocaleDateString() +
					(table.search().length === 0 ? '' : ' <' + table.search() + '>') +
					(valr === '' ? '' : ' [' + valr + ']')
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
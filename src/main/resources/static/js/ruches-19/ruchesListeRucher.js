/* globals
	Ruches, rucher, nomrucher, buttTxtPrint, buttTxtCol, DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const idTbl = '#ruches';
	const table = new DataTable(idTbl, {
		dom: '<"buttonsData" Blf>t<"buttonsData" ip>',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf', exportOptions: { columns: ':visible' },
			title: function() {
				return `${Ruches} ${rucher} ${nomrucher} ${(new Date()).toLocaleDateString()}` +
					(table.search().length === 0 ? '' : ' <' + table.search() + '>');
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
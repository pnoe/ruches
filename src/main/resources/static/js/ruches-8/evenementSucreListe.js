/* globals
	  buttTxtPrint buttTxtCol Evenements  DataTable
*/
/* exported evenementSucreListe */
'use strict';
function evenementSucreListe() {
	const idTbl = '#evenementssucre';
	const table = new DataTable(idTbl, {
		order: [[0, 'desc']],
		dom: 'Blftip',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf',
			exportOptions: {
				columns: ':visible'
			},
			title: function() {
				return Evenements + ' ' + new Date().toLocaleDateString() +
					(table.search().length === 0 ? '' : ' <' + table.search() + '>');
			}
		}, {
				extend: 'print',
				text: buttTxtPrint
			}, {
				extend: 'colvis',
				text: buttTxtCol
			}],
		footerCallback: function() {
			const api = this.api();
			// Remove the formatting to get integer data for summation
			function valPoids(i) {
				return typeof i === 'string' ? i.replace(/[,]/, '.') * 1 : typeof i === 'number' ? i : 0;
			}
			const coltotal = 4;
			const total = api.column(coltotal, {
				search: 'applied'
			}).data().reduce(function(a, b) {
				return valPoids(a) + valPoids(b);
			}, 0);
			const pageTotal = api.column(coltotal, {
				page: 'current'
			}).data().reduce(function(a, b) {
				return valPoids(a) + valPoids(b);
			}, 0);
			$(api.column(coltotal).footer()).html(
				Number.parseFloat(total).toFixed(2) + '<br/>page '
				+ Number.parseFloat(pageTotal).toFixed(2));
		}
	});
}

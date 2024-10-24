/* globals
	  buttTxtPrint, buttTxtCol, Evenements, DataTable
*/
/* exported evenementSucreListe */
'use strict';
function evenementSucreListe() {
	const table = new DataTable('#evenementssucre', {
		order: [[0, 'desc']],
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							let title = Evenements + ' ' + (new Date()).toLocaleDateString();
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							doc.content[0].text = title;
						}
					},
					{
						extend: 'print', text: buttTxtPrint,
						exportOptions: { columns: ':visible' },
					},
					{ extend: 'colvis', text: buttTxtCol },
					'pageLength'
				]
			}
		},
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
			api.column(coltotal).footer().innerHTML =
				Number.parseFloat(total).toFixed(2) + '<br/>page '
				+ Number.parseFloat(pageTotal).toFixed(2);
		}
	});
}

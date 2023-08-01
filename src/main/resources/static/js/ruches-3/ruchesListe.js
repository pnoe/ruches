/* globals
	Ruches  buttTxtPrint  buttTxtCol  urlCommLot  selectRuTrait DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('rucher').addEventListener('change', (event) => {
		table.columns(2).search(event.target.value).draw();
	});
	const idTbl = '#ruches';
	const table = new DataTable(idTbl, {
		select: {
			style: 'multi+shift'
		},
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
			}, {
				extend: 'colvis', text: buttTxtCol
			}
		]
	});
	function updateLinks(e, dt, type) {
		if (type === 'row') {
			let noms = '';
			table.rows({
				selected: true
			}).data().pluck(0).each(
				function(value) {
					const a = document.createElement('template');
					a.innerHTML = value;
					noms += a.content.children[0].getAttribute('href').substring(
						a.content.children[0].getAttribute('href').lastIndexOf('/') + 1)
						+ ',';
				});
			if (noms) {
				document.getElementById('commentaire').setAttribute('href',
					urlCommLot + noms.substring(0, noms.length - 1));
			} else {
				document.getElementById('commentaire').setAttribute('href', '#');
			}
		}
	}
	table.on('select deselect', updateLinks);
	document.getElementById('commentaire').addEventListener('click', (event) => {
		if (event.target.getAttribute('href') === '#') {
			alert(selectRuTrait);
		}
	});
});
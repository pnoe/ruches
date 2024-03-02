/* globals
	Ruches, buttTxtPrint, buttTxtCol, urlCommLot, selectRuTrait, DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('rucher').addEventListener('change', event => {
		table.columns(2).search(event.target.value).draw();
	});
	const table = new DataTable('#ruches', {
		select: {
			style: 'multi+shift'
		},
		scrollX: true,
		layout: {
			topStart: {
				buttons: [
					'csv',
					{
						extend: 'pdfHtml5',
						exportOptions: { columns: ':visible' },
						customize: function(doc) {
							const sru = document.getElementById('rucher');
							const valr = sru.options[sru.selectedIndex].value;
							doc.content[0].text = Ruches + ' ' + (new Date()).toLocaleDateString() +
								(table.search().length === 0 ? '' : ' <' + table.search() + '>') +
								(valr === '' ? '' : ' [' + valr + ']');
						},
						orientation: 'landscape'
					},
					{
						extend: 'print',
						text: buttTxtPrint,
						exportOptions: { columns: ':visible' },
						orientation: 'landscape'
					},
					{ extend: 'colvis', text: buttTxtCol },
					'pageLength'
				]
			}
		}
	});
	function updateLinks(_e, _dt, type) {
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
	document.getElementById('commentaire').addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selectRuTrait);
		}
	});
});
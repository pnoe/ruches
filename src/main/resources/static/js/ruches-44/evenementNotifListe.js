/* globals
url, Evenements, Notifications, btp, btc, DataTable, bootstrap
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	new bootstrap.Popover(document.getElementsByClassName('bi-question-lg')[0], {
		html: true
	});
	document.getElementById('tous').addEventListener('change', function(event) {
		// location = url + event.target.checked;
		window.location.assign(url + event.target.checked);
	});
	const table = new DataTable('#evenementsnotif', {
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
							let title = Evenements + ' ' + Notifications + ' ' + (new Date()).toLocaleDateString();
							const inputSearch = table.search();
							if (inputSearch.length !== 0) {
								title += ' <' + inputSearch + '>';
							}
							doc.content[0].text = title;
						}
					},
					{
						extend: 'print', text: btp,
						exportOptions: { columns: ':visible' },
					},
					{ extend: 'colvis', text: btc },
					'pageLength'
				]
			}
		}
	});
});
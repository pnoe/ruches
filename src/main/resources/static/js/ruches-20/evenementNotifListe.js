/* globals
url, Evenements, Notifications, btp, btc, DataTable, bootstrap
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	new bootstrap.Popover(document.getElementsByClassName('bi-question-lg')[0], {
		html: true
	});
	document.getElementById('tous').addEventListener('change', function(event) {
		location = url + event.target.checked;
	});
	new DataTable('#evenementsnotif', {
		order: [[0, 'desc']],
		dom: 'Blftip',
		scrollX: true,
		buttons: ['csv', {
			extend: 'pdf',
			exportOptions: {
				columns: ':visible'
			},
			title: Evenements + ' ' + Notifications + ' ' + (new Date()).toLocaleDateString()
		}, {
				extend: 'print',
				text: btp
			}, {
				extend: 'colvis',
				text: btc
			}]
	});
});
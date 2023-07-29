/* globals
url Evenements Notifications btp btc DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	$('.bi-question-lg').popover({
		html: true
	});
	$('.popover-dismiss').popover({
		trigger: 'focus'
	});
	$('#tous').on('change', function() {
		location = url + ($(this).is(':checked'));
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
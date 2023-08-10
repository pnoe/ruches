/* globals Evenements, buttontextprint, buttontextcol, DataTable */
/* exported evenementListe */
'use strict';
function evenementListe(initDatatable) {
	const cookieOpt = ';SameSite=Strict;path=' + window.location.pathname;
	if (initDatatable) {
		const table = new DataTable('#evenements', {
			order: [[0, 'desc']],
			dom: 'Blftip',
			scrollX: true,
			buttons: [
				'csv',
				{
					extend: 'pdf', exportOptions: { columns: ':visible' },
					title: function() {
						return Evenements + ' ' + new Date().toLocaleDateString() +
							(table.search().length === 0 ? '' : ' <' + table.search() + '>');
					},
				},
				{ extend: 'print', text: buttontextprint },
				{ extend: 'colvis', text: buttontextcol }
			]
		});
	}
	document.getElementById('periode').addEventListener('change', (event) => {
		if (event.target.value !== '6') {
			document.cookie = 'p=' + event.target.value + cookieOpt;
			event.target.form.submit();
		}
	});
	document.getElementById('cal').addEventListener('click', (event) => {
		if (event.target.value === '6') {
			$(event.target.parentNode).dateRangePicker({
				autoClose: true,
				language: (window.navigator.language.substring(0, 2)),
				startOfWeek: 'monday',
				separator: ' - '
			}).bind('datepicker-change', function(evt, obj) {
				evt.stopPropagation();
				const d1 = obj.date1.toISOString();
				const d2 = obj.date2.toISOString();
				window.document.cookie = 'p=6' + cookieOpt;
				window.document.cookie = 'd1=' + d1 + cookieOpt;
				window.document.cookie = 'd2=' + d2 + cookieOpt;
				window.document.cookie = 'dx=' + encodeURIComponent(obj.value) + cookieOpt;
				document.getElementById('date1').value = d1;
				document.getElementById('date2').value = d2;
				document.getElementById('datestext').value = obj.value;
				this.form.submit();
			});
		}
	});
}
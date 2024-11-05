/* globals Evenements, buttontextprint, buttontextcol, DataTable */
/* exported evenementListe */
'use strict';
function evenementListe(initDatatable) {
	const cookieOpt = ';SameSite=Strict;path=' + window.location.pathname;
	if (initDatatable) {
		// https://datatables.net/forums/discussion/78174/align-cells-left-after-2-0
		// Forcé l'alignement à gauche en html pour liste eve traitements groupés
		const table = new DataTable('#evenements', {
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
							extend: 'print', text: buttontextprint,
							exportOptions: { columns: ':visible' },
						},
						{ extend: 'colvis', text: buttontextcol },
						'pageLength'
					]
				}
			}
		});
	}
	const periode = document.getElementById('periode');
	if (periode) {
		periode.addEventListener('change', event => {
			if (event.target.value !== '6') {
				document.cookie = 'p=' + event.target.value + cookieOpt;
				event.target.form.submit();
			}
		});
	}
	const cal = document.getElementById('cal');
	if (cal) {
		cal.addEventListener('click', event => {
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
}
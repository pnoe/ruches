/* jshint  esversion: 6, browser: true, jquery: true */
/* globals listeEvenements, buttontextprint, buttontextcol */

function evenementListe(initDatatable) {
	if (initDatatable) {
		$('#evenements').DataTable({
			order: [[0, 'desc']],
			dom: 'Blftip',
			buttons: [
				'csv',
				{
					extend: 'pdf', exportOptions: { columns: ':visible' },
					title: listeEvenements + " " + (new Date()).toLocaleDateString()
				},
				{ extend: 'print', text: buttontextprint },
				{ extend: 'colvis', text: buttontextcol }
			]
		});
	}
	$('#periode').on('change', function(evt) {
		if ($(this).val() !== '6') {
			this.form.submit();
		}
	});
	$('#cal').on('click', function(evt) {
		if ($(this).parent().val() === '6') {
			$(this).parent().dateRangePicker({
				autoClose: true,
				startOfWeek: 'monday',
				separator: ' - ',
				setValue: function(s) {
					$(this).parent().find("option:selected").text(s);
				}
			}).bind('datepicker-change', function(evt, obj) {
				evt.stopPropagation();
				$(this).data('dateRangePicker');
				$('#date1').val(obj.date1.toISOString());
				$('#date2').val(obj.date2.toISOString());
				$('#datestext').val($(this).parent().find("option:selected").text());
				this.form.submit();
			});
		}
	});
}
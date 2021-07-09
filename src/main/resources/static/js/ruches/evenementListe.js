/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals listeEvenements, buttontextprint, buttontextcol */

function evenementListe(initDatatable) {
	const cookieOpt = ";SameSite=Strict;path=" + window.location.pathname;
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
	$('#periode').on('change', function() {
		if ($(this).val() !== '6') {
			document.cookie = "p=" + $(this).val() + cookieOpt;
			this.form.submit();
		}
	});
	$('#cal').on('click', function() {
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
				const d1 = obj.date1.toISOString();
				const d2 = obj.date2.toISOString();
				window.document.cookie = "p=6" + cookieOpt;
				window.document.cookie = "d1=" + d1 + cookieOpt;
				window.document.cookie = "d2=" + d2 + cookieOpt;
				window.document.cookie = "dx=" +  encodeURIComponent(obj.value) + cookieOpt;
				$('#date1').val(d1);
				$('#date2').val(d2);
				$('#datestext').val(obj.value);
				this.form.submit();
			});
		}
	});
}
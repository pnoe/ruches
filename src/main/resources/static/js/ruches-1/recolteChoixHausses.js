/* globals urlRetrait, urlAjout, recolteId, selHausRet, selHausAjout, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	function updateLinks(e, dt, type) {
		let id;
		let href;
		let table;
		if (this.id === 'ajoutHausseRecolte') {
			id = $('#ajouterHausses');
			href = urlAjout;
			table = tableAjout;
		} else {
			id = $('#retirerHausses');
			href = urlRetrait;
			table = tableRetrait;
		}
		if (type === 'row') {
			let hausseNoms = '';
			table.rows({
				selected: true
			}).data().pluck(1).each(function(value) {
				hausseNoms += value + ',';
			});
			if (hausseNoms) {
				/*[- on enlève la dernière virgule et met à jour l'url de traitement -]*/
				hausseNoms = hausseNoms.substring(0,
					hausseNoms.length - 1);
				id
					.attr('href', href + recolteId + '/'
						+ hausseNoms);
			} else {
				id.attr('href', '#');
			}
		}
	}
	const tableRetrait = new DataTable('#retraitHausseRecolte', {
		select: {
			style: 'multi+shift'
		}
	});
	tableRetrait.on('select deselect', updateLinks);
	$('#retirerHausses').on('click', function() {
		if ($(this).attr('href') === '#') {
			alert(selHausRet);
		}
	});
	const tableAjout = new DataTable('#ajoutHausseRecolte', {
		select: {
			style: 'multi+shift'
		}
	});
	tableAjout.on('select deselect', updateLinks);
	$('#ajouterHausses').on('click', function() {
		if ($(this).attr('href') === '#') {
			alert(selHausAjout);
		}
	});
});
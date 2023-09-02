/* globals urlRetrait, urlAjout, recolteId, selHausRet, selHausAjout, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	function updateLinks(e, dt, type) {
		let id;
		let href;
		let table;
		if (this.id === 'ajoutHausseRecolte') {
			id = document.getElementById('ajouterHausses');
			href = urlAjout;
			table = tableAjout;
		} else {
			id = document.getElementById('retirerHausses');
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
				id.setAttribute('href', href + recolteId + '/'
					+ hausseNoms);
			} else {
				id.setAttribute('href', '#');
			}
		}
	}
	const tableRetrait = new DataTable('#retraitHausseRecolte', {
		select: {
			style: 'multi+shift'
		}
	});
	tableRetrait.on('select deselect', updateLinks);
	document.getElementById('retirerHausses').addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selHausRet);
			event.preventDefault(); // pas indispensable link #
		}
	});
	const tableAjout = new DataTable('#ajoutHausseRecolte', {
		select: {
			style: 'multi+shift'
		}
	});
	tableAjout.on('select deselect', updateLinks);
	document.getElementById('ajouterHausses').addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selHausAjout);
			event.preventDefault();
		}
	});

});
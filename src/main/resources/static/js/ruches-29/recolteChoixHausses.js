/* globals urlRetrait, urlAjout, recolteId, selHausRet, selHausAjout, DataTable */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const ttChk = document.getElementById('toutes');
	if (location.href.endsWith('true')) {
		ttChk.checked = true;
	}
	ttChk.addEventListener('click', event => {
		location = location.href.slice(0, location.href.lastIndexOf('/'))
			+ '/' + event.target.checked;
	});
	function updateLinks(_e, _dt, type) {
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
			let hausseIds = '';
			table.rows({
				selected: true
			}).data().pluck(1).each(function(value) {
				// pluck(1) 2ième td dans le tableau, soit
				// l'id de la hausse. Attention à l'ordre !
				hausseIds += value + ',';
			});
			if (hausseIds) {
				// On enlève la dernière virgule et met à jour l'url de traitement.
				hausseIds = hausseIds.substring(0,
					hausseIds.length - 1);
				id.setAttribute('href', href + recolteId + '/'
					+ hausseIds);
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
	const tableAjout = new DataTable('#ajoutHausseRecolte', {
		select: {
			style: 'multi+shift'
		}
	});
	tableAjout.on('select deselect', updateLinks);
	document.getElementById('retirerHausses').addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selHausRet);
			event.preventDefault(); // pas indispensable link #
		}
	});
	document.getElementById('ajouterHausses').addEventListener('click', event => {
		if (event.target.getAttribute('href') === '#') {
			alert(selHausAjout);
			event.preventDefault();
		}
	});

});
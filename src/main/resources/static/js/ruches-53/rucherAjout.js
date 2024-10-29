/* globals DataTable, urlAjout, rucherId, selRuchesAjout */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const tableajout = new DataTable('#ajoutRuches', {
		select: {
			style: 'multi+shift'
		}
	});
	new DataTable('#retraitRuches', {});
	const actionAjoutRuches = document.getElementById('actionAjoutRuches');
	function updateLinkAjout(_e, _dt, type) {
		// Mise à jour des ids à ajouter à l'url pour l'ajout des ruches sélectionnées.
		if (type === 'row') {
			let rucheIds = '';
			// Retrouver l'id de la ruche dans l'attribut data-id de l'élément tr
			// https://datatables.net/reference/api/rows().nodes()
			tableajout.rows({
				selected: true
			}).nodes().each(function(value) {
				rucheIds += value.getAttribute('data-id') + ',';
			});
			if (rucheIds) {
				// On enlève la dernière virgule et met à jour l'url de traitement
				rucheIds = rucheIds.substring(0, rucheIds.length - 1);
				actionAjoutRuches.setAttribute('href', urlAjout + rucherId + '/' + rucheIds);
			} else {
				actionAjoutRuches.setAttribute('href', '#actionAjoutRuches');
			}
		}
	}
	tableajout.on('select deselect', updateLinkAjout);
	actionAjoutRuches.addEventListener('click', () => {
		if (actionAjoutRuches.getAttribute('href') === '#actionAjoutRuches') {
			alert(selRuchesAjout);
		}
	});
});
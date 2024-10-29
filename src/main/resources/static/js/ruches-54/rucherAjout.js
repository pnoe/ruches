/* globals DataTable, urlAjout, rucherId, selRuchesAjout */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	DataTable.type('num', {
		// https://datatables.net/manual/data/types
		// https://datatables.net/forums/discussion/78174/align-cells-left-after-2-0
		// Pour que les noms de ruches "01" "02" soient alignés à gauche comme les strings
		// Sinon l'alignement se fait à droite quand il n'y a que des noms de type num
		className: 'string'
	});
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
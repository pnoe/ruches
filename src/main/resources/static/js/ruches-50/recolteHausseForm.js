/* globals pdsAvSupPdsAp, ruchesurl, essaimId, rucheId, rucherId
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	$.validator.addMethod('greaterThanEqual', function(value, element, param) {
		const target = $(param);
		if (target.val() === '') {
			return true;
		}
		if (this.settings.onfocusout && target.not('.validate-greaterThanEqual-blur').length) {
			target.addClass('validate-greaterThanEqual-blur').on('blur.validate-greaterThanEqual', function() {
				$(element).valid();
			});
		}
		return parseFloat(value) >= parseFloat(target.val());
	}, pdsAvSupPdsAp);
	$('#recolteHausseForm').validate({
		errorClass: 'erreur-form',
		errorPlacement: function(error, element) {
			error.insertBefore(element.closest('div'));
		},
		rules: {
			poidsAvant: {
				required: true,
				greaterThanEqual: '#poidsApres'
			},
			poidsApres: {
				required: true
			}
		}
	});
	document.getElementById('poidsAvant').focus();
	const essaimSel = document.getElementById('essaim');
	const rucheSel = document.getElementById('ruche');
	const rucherSel = document.getElementById('rucher');

	function addOpt(elemSel, idNoms, entId) {
		elemSel.remove(0);
		// L'option aucune permet de saisir une hausse récolte non 
		// renseignée pour un des paramètre hausse, ruche, rucher ou essaim.
		idNoms.unshift({ id: '', nom: 'Aucun(e)' });
		idNoms.forEach(el => {
			const opt = document.createElement('option');
			opt.value = el.id;
			opt.text = el.nom;
			if (el.id === entId) {
				opt.selected = true;
			}
			elemSel.add(opt);
		});
	}

	addEventListener('submit', () => {
		// Enable les listes déroulantes essaim, ruche, rucher
		// pour qu'elles soit envoyées lors du submit.
		essaimSel.disabled = false;
		rucheSel.disabled = false;
		rucherSel.disabled = false;
	});

	document.getElementById('toggleplus').addEventListener('click', event => {
		// ne pas utiliser disabled les select car cela renvoie null
		//  pour les champs ruche, essaim et rucher
		const req = new XMLHttpRequest();
		req.open('GET', ruchesurl + 'recolte/listesPlus', true);
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					const resp = JSON.parse(req.responseText);
					addOpt(essaimSel, resp.essaims, essaimId);
					addOpt(rucheSel, resp.ruches, rucheId);
					addOpt(rucherSel, resp.ruchers, rucherId);
					essaimSel.disabled = false;
					rucheSel.disabled = false;
					rucherSel.disabled = false;
					essaimSel.focus();
				}
			}
		};
		req.send();
		event.currentTarget.disabled = true;
		// once true : l'écouteur d'événement est supprimé pour éviter
		//  plusieurs chargement des listes.
	}, { once: true });
});
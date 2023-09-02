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
		idNoms.unshift({ id: '', nom: 'Aucun(e)' });
		idNoms.forEach((el) => {
			const opt = document.createElement('option');
			opt.value = el.id;
			opt.text = el.nom;
			if (el.id === entId) {
				opt.selected = true;
			}
			elemSel.add(opt);
		});
	}

	document.getElementById('toggleplus').addEventListener('click', () => {
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
					essaimSel.focus();
				}
			}
		};
		req.send();
	}, { once: true });
});
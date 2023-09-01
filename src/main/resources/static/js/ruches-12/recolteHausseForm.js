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
	const toogleplus = document.querySelectorAll('.toggleplus');
	let init = true;

	function addOpt(selId, idNoms, entId) {
		const elemSel = document.getElementById(selId);
		elemSel.remove(0);
		idNoms.unshift({id: '', nom: 'Aucun(e)'});
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
		if (init) {
			const req = new XMLHttpRequest();
			req.open('GET', ruchesurl + 'recolte/listesPlus', true);
			req.onload = function() {
				if (req.readyState === 4) {
					if (req.status === 200) {
						const resp = JSON.parse(req.responseText);
						addOpt('essaim', resp.essaims, essaimId);
						addOpt('ruche', resp.ruches, rucheId);
						addOpt('rucher', resp.ruchers, rucherId);
					}
				}
			};
			req.send();
			init = false;
		}

		toogleplus.forEach(sel => {
			sel.style.display = sel.style.display === 'none' ? 'block' : 'none';
		});
	});
});
/* globals complet */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const ruchesClass = document.querySelectorAll('.ruches');
	if (complet === 'true') {
		document.getElementById('showruches').checked = true;
		ruchesClass.forEach(item => {
			item.style.display = 'block';
		});
	}
	document.getElementById('showruches').addEventListener('click', event => {
		ruchesClass.forEach(item => {
			item.style.display = event.target.checked ? 'block' : 'none';
		});
	});
	const form = $('#evenementForm');
	const selectElement = document.getElementById('type');
	form.validate({
		errorClass: 'erreur-form',
		errorPlacement: function(error, element) {
			error.insertBefore(element.closest('div'));
		},
		rules: {
			date: {
				required: true
			},
			commentaire: {
				required: function() {
					return ['COMMENTAIRERUCHE', 'COMMENTAIRERUCHER', 'COMMENTAIREESSAIM', 'COMMENTAIREHAUSSE']
						.includes(selectElement.value);
				}
			},
			ruche: {
				required: function() {
					return ['RUCHEAJOUTRUCHER', 'AJOUTESSAIMRUCHE', 'HAUSSEPOSERUCHE', 'HAUSSERETRAITRUCHE',
						'COMMENTAIRERUCHE']
						.includes(selectElement.value);
				}
			},
			rucher: {
				required: function() {
					return ['RUCHEAJOUTRUCHER', 'COMMENTAIRERUCHER']
						.includes(selectElement.value);
				}
			},
			essaim: {
				required: function() {
					return ['AJOUTESSAIMRUCHE', 'ESSAIMTRAITEMENT', 'ESSAIMTRAITEMENTFIN', 'ESSAIMSUCRE',
						'COMMENTAIREESSAIM', 'ESSAIMDISPERSION', 'RUCHEPESEE', 'RUCHECADRE']
						.includes(selectElement.value);
				}
			},
			hausse: {
				required: function() {
					return ['HAUSSEPOSERUCHE', 'HAUSSERETRAITRUCHE', 'HAUSSEREMPLISSAGE', 'COMMENTAIREHAUSSE']
						.includes(selectElement.value);
				}
			}
		}
	});
	/*[- Pour trier les noms de types d'événements -]*/
	const selOpt = selectElement.value;
	const optionElements = Array.from(selectElement.querySelectorAll('option'));
	optionElements.sort((a, b) => a.textContent.localeCompare(b.textContent));
	selectElement.innerHTML = '';
	optionElements.forEach(option => selectElement.appendChild(option));
	selectElement.value = selOpt;
	selectElement.addEventListener('change', () => {
		form.valid();
	});
});
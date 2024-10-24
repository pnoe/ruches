/* globals complet, traitements */
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
	// https://jqueryvalidation.org/category/validator/
	jQuery.validator.addMethod('traitement', function(value) {
		// First argument: Current value. Second argument: Validated element. Third argument: Parameters.
		if ((selectElement.value === 'ESSAIMTRAITEMENT') || (selectElement.value === 'ESSAIMTRAITEMENTFIN')) {
			// Si eve traitement teste que le champ Valeur est dans l'enum type de traitements
			return traitements.includes(value);
		} else {
			// Si autre événement on valide
			return true;
		}
	}, 'Valeur possibles : ' + traitements.join(', '));
	// jQuery.validator.messages.number ne fonctionne pas directement
	// dans addMethod, on récupère la chaine anglaise.
	const numberMess = jQuery.validator.messages.number;
	jQuery.validator.addMethod('numberEve', function(value, element) {
		// Le champ valeur est un numérique si le champ type est :
		//  un commentaire pour le nombre de jours de notification
		//  une pesée de ruche pour le poids, un eve ruchecadre pour le nombre de cadres,
		//  hausses pose et retrait pour le numéro d'ordre
		//  hausse remplissage pour le % de remplissage
		//  essaim sucre pour le poids de sucre
		if (['COMMENTAIRERUCHE', 'COMMENTAIRERUCHER', 'COMMENTAIREESSAIM', 'COMMENTAIREHAUSSE',
			'RUCHEPESEE', 'RUCHECADRE', 'HAUSSEPOSERUCHE', 'HAUSSERETRAITRUCHE', 'HAUSSEREMPLISSAGE',
			'ESSAIMSUCRE'
		]
			.includes(selectElement.value)) {
			// https://github.com/jquery-validation/jquery-validation/blob/master/src/core.js
			return this.optional(element) || /^(?:-?\d+|-?\d{1,3}(?:,\d{3})+)?(?:-?\.\d+)?$/.test(value);
		} else {
			return true;
		}
	}, numberMess);
	form.validate({
		errorClass: 'erreur-form',
		errorPlacement: function(error, element) {
			error.insertBefore(element.closest('div'));
		},
		rules: {
			date: {
				required: true
			},
			valeur: {
				required: function() {
					// Le champ valeur est obligatoire si le champ type est RUCHEPESEE
					//   selectElement = document.getElementById('type');
					return ['RUCHEPESEE']
						.includes(selectElement.value);
				},
				// Validation pour eves ou valeur est un numérique
				// https://jqueryvalidation.org/number-method/
				// number: false,  fonctionne, mais
				// number: function() {return false;},   ne fonctionne pas
				// d'où la règle custom numberEve
				numberEve: true,
				// Validation pour eves traitement.
				traitement: true
			},
			commentaire: {
				required: function() {
					// Le champ commentaire est obligatoire si le champ type est un COMMENTAIRE
					return ['COMMENTAIRERUCHE', 'COMMENTAIRERUCHER', 'COMMENTAIREESSAIM', 'COMMENTAIREHAUSSE']
						.includes(selectElement.value);
				}
			},
			ruche: {
				required: function() {
					return ['RUCHEAJOUTRUCHER', 'AJOUTESSAIMRUCHE', 'HAUSSEPOSERUCHE', 'HAUSSERETRAITRUCHE',
						'COMMENTAIRERUCHE', 'RUCHEPESEE', 'RUCHECADRE']
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
	// Pour trier les noms de types d'événements
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
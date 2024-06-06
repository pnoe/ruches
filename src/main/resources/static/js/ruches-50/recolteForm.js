/* globals */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	document.getElementById('typeMiel').focus();
	$('#recolteForm').validate({
		errorClass: 'erreur-form',
		errorPlacement: function(error, element) {
			error.insertBefore(element.closest('div'));
		},
		rules: {
			date: {
				required: true
			},
			poidsMiel: {
				required: true
			}
		}
	});
	/*[- Pour trier les noms de types de miel -]*/
	const selectElement = document.getElementById('typeMiel');
	const selOpt = selectElement.value;
	const optionElements = Array.from(selectElement.querySelectorAll('option'));
	optionElements.sort((a, b) => a.textContent.localeCompare(b.textContent));
	selectElement.innerHTML = '';
	optionElements.forEach(option => selectElement.appendChild(option));
	selectElement.value = selOpt;
});
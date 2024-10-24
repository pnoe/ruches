/* globals doitEtreSupA, dateFirst, timeFirst, ruche, ruchesource, ruchetxt,
	  lEssaim, essaimtxt, essaim, resteEnPlace, estDeplace, videDansLeRuher, dansLeRucher  */
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const rucherucherid = ruche.rucher?.id;
	const ruchesourcerucherid = ruchesource?.rucher?.id;
	const swapCheck = document.getElementById('swapPositions');
	$.validator.addMethod('greaterThanEqual', function(value, element, param) {
		return (swapCheck?.checked &&
			(rucherucherid !== ruchesourcerucherid))
			? (Date.parse(value) > Date.parse(param)) : true;
	}, doitEtreSupA + ' {0}');
	$('#evenementForm').validate({
		errorClass: 'erreur-form',
		errorPlacement: function(error, element) {
			error.insertBefore(element.closest('div'));
		},
		rules: {
			date: {
				required: true,
				greaterThanEqual: dateFirst + ' ' + timeFirst
			},
			commentaire: {
				maxlength: 255
			}
		}
	});
	$('#date').datetimepicker({
		minDate: false,
		minTime: false
	});
	function swapChange() {
		const isChk = swapCheck.checked &&
			(rucherucherid !== ruchesourcerucherid);
		$('#date').datetimepicker({
			minDate: isChk ? dateFirst : false,
			minTime: isChk ? timeFirst : false
		});
		afficheEtatEnreg();
	}
	if (swapCheck) { swapCheck.addEventListener('change', swapChange); }
	const etatRucheSource = document.getElementById('etatRucheSource');
	const deplacementE = document.getElementById('deplacementE');
	const etrTxt = `${ruchetxt} ${ruche.nom}, ${essaimtxt} ${essaim.nom} ${dansLeRucher} `;
	let etrSTxt, deplTxt;
	if (ruchesource) {
		etrSTxt = ruchetxt + ' ' + ruchesource.nom + ' ' + videDansLeRuher + ' ';
		deplTxt = lEssaim + ' ' + essaim.nom + ' ';
	}
	function afficheEtatEnreg() {
		if (ruchesource) {
			etatRucheSource.textContent = etrSTxt +
				(swapCheck.checked ? ruche.rucher.nom : ruchesource.rucher.nom);
			deplacementE.textContent = deplTxt +
				(swapCheck.checked ? resteEnPlace : estDeplace);
		}
		etatRuche.textContent = etrTxt + (swapCheck?.checked ? ruchesource.rucher.nom : ruche.rucher.nom);
	}
	afficheEtatEnreg();
});

/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals _csrf_token,_csrf_param_name, essaimnoms, ruchesvidesnoms, recolteHausses, rucheEssaim,
    essaimsupprecolttxt, suppessaimevetxt, suppessaimtxt,  evenements, nomessaimsvirgule,
    nexistPasOuPasVidtxt, nomexistdejatxt, Ruchestxt, Essaimstxt, creertxt, urlessaimclone,
    essaimid, pasderuchetxt
*/
"use strict";
function essaimDetail() {
	$('#dispersion').on('click', function () {
		if(!rucheEssaim) {
			alert(pasderuchetxt);
			return false;
		}
		return true;
    });
    
 	$('#supprime').on('click', function () {
		if(recolteHausses) {
			alert(essaimsupprecolttxt);
			return false;
		} else if (evenements) {
	        return confirm(suppessaimevetxt);			
		} else {
			return confirm(suppessaimtxt);
		}
    });
    
	$("#clone").on('click', function() {
		/*[- Saisie des couples nomEssaim;nomRuche séparés par une "," -]*/
		const noms = prompt(nomessaimsvirgule);
		if (noms == null || noms == "") {
		    return;
		}
		const tabNomExiste=[];
		const tabNomOk=[];
		const tabNomRucheOk=[];
		const tabNomRucheIncorrect=[];
		const nomsarr = noms.split(',');
		for (const item of nomsarr) {
			const arr = item.split(';');
			/*[- ne nom de l'essaim -]*/
			const ne = arr[0].trim();
			let nr = "";
			/*[- nr nom de la ruche -]*/
			if (arr.length > 1) {
				nr = arr[1].trim();
			}		
			if ($.inArray(ne, essaimnoms) != -1) {
				/*[- l'essaim ne existe déjà -]*/
				tabNomExiste.push(ne);
				if ((nr != "") && ($.inArray(nr, ruchesvidesnoms) == -1)) {
					/*[- ruche nr incorrecte -]*/
					tabNomRucheIncorrect.push(nr);
				}
			} else {
				/*[- on crée l'essaim ne -]*/
				tabNomOk.push(ne);
				/*[- ce nom ne doit pas être réutilisé -]*/
				essaimnoms.push(ne);
				if (nr == "") {
					/*[- pas de ruche -]*/
					tabNomRucheOk.push("");
				} else if ($.inArray(nr, ruchesvidesnoms) == -1) {
					/*[- ruche nr incorrecte -]*/
					tabNomRucheOk.push("");
					tabNomRucheIncorrect.push(nr);
				} else {
					/*[- on met l'essaim ne dans la ruche nr -]*/
					tabNomRucheOk.push(nr);
					/*[- ce nom ne doit pas être réutilisé, on l'enlève de la
						liste des ruches vides -]*/
					const index = ruchesvidesnoms.indexOf(nr);
					if (index > -1) {
						ruchesvidesnoms.splice(index, 1);
					}
				}
			}
		}
		let prompterreur = "";
		if (tabNomRucheIncorrect.length > 0) {
			prompterreur = Ruchestxt + ' : ' + tabNomRucheIncorrect.join(',') + ' ' + nexistPasOuPasVidtxt;
		}
		if (tabNomExiste.length > 0) {
			if (prompterreur != "") { prompterreur += '\n'; }		
			prompterreur += Essaimstxt + ' : ' + tabNomExiste.join(',') + ' ' + nomexistdejatxt;
			if (tabNomOk.length == 0) {
				alert(prompterreur);
				return false;					
			}
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				return false;
			}
		} else if (tabNomRucheIncorrect.length > 0) {
			if (!confirm(prompterreur + '. ' + creertxt + ' ' + tabNomOk.join(',') + ' ?')) {
				return false;
			}
		}
	    const requestData = {nomclones : tabNomOk.join(','), nomruches : tabNomRucheOk.join(',')};
	    requestData[_csrf_param_name] = _csrf_token;
		$.post(urlessaimclone + essaimid,
				requestData).done(function (data) { 
					alert( data );
					document.location.reload(true);
		});
	});
}
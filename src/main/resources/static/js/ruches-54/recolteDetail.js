/* globals
	_csrf_token, _csrf_param_name, suppRecHauss, dateRecEpoch
	enlevHRecDe30, urlRecHDepot, recId, total, pasDeHausses, pasDeHaussesTxt, DataTable
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	const itDate = document.getElementById('date');
	const itDateOk = document.getElementById('dateOK');
	const cookieOpt = ';SameSite=Strict;path=' + window.location.pathname;
	itDate.style.display = 'none';
	itDateOk.style.display = 'none';

	function toggleDateInputs(show) {
		itDate.style.display = show ? 'block' : 'none';
		itDateOk.style.display = show ? 'block' : 'none';
	}

	function postRequest(url, data, onSuccess, onError) {
		fetch(url, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
			},
			body: new URLSearchParams(data),
		})
			.then(response => response.text())
			.then(text => {
				if (onSuccess) { onSuccess(text); }
			})
			.catch(error => {
				if (onError) { onError(error); }
				alert('Request failed', error);
			});
	}

	document.getElementById('haussesMiel').addEventListener('click', event => {
		if (pasDeHausses) {
			alert(pasDeHaussesTxt);
			event.preventDefault();
		}
	});

	document.getElementById('enlevehausses').addEventListener('click', function() {
		// Retire tous les hausses de la récolte recId de leurs ruches.
		// La fonction est désactivée après 30 jours 
		// 30 * 24 * 3600 * 1000 =  2592000000 en millisecondes
		if (Date.now() > dateRecEpoch * 1000 + 2592000000) {
			alert(enlevHRecDe30);
			return;
		}
		postRequest(urlRecHDepot + recId, {
			[_csrf_param_name]: _csrf_token,
		}, responseText => {
			if (responseText.endsWith('?')) {
				if (!confirm(responseText)) {
					return;
				}
			} else {
				alert(responseText);
				return;
			}
			const now = new Date();
			itDate.value = `${now.getFullYear()}/${now.getMonth() + 1}/${now.getDate()} ${now.getHours()}:${now.getMinutes()}`;
			toggleDateInputs(true);
			itDate.focus();
		});
	});

	itDateOk.addEventListener('click', function() {
		// Validation du retrait des hausses de leurs ruches par clic sur
		// le bouton sous le calendrier après choix de la date de retrait.
		const date = itDate.value;
		toggleDateInputs(false);
		postRequest(urlRecHDepot + recId, {
			[_csrf_param_name]: _csrf_token,
			date: date
		}, responseText => {
			alert(responseText);
		});
	});

	document.getElementById('supprime').addEventListener('click', event => {
		if (!confirm(suppRecHauss)) {
			event.preventDefault();
		}
	});

	function addCell(tr, content, colSpan = 1, classN = '') {
		const td = document.createElement('td');
		td.colSpan = colSpan;
		td.textContent = content;
		if (classN) { td.className = classN }
		tr.appendChild(td);
	}

	function tblStartRender(rows, group) {
		// On somme les poids de miel d'une ruche
		// const mielTotal = rows.data().pluck(4).reduce(function(a, b) {
		const mielTotal = rows.data().pluck(5).reduce(function(a, b) {
			return a + parseFloat(b.replace(/,/g, '.'));
		}, 0.00);
		const tr = document.createElement('tr');
		addCell(tr, '');
		addCell(tr, total);
		const ruche = document.createElement('td');
		ruche.insertAdjacentHTML('afterbegin', group);
		tr.appendChild(ruche);
		addCell(tr, '', 2);
		addCell(tr, new Intl.NumberFormat(navigator.language, {
			minimumFractionDigits: 2
		}).format(mielTotal), 1, 'num');
		addCell(tr, '', 2);
		return tr;
	}
	const groupChk = document.getElementById('group');
	if (groupChk) {
		// si pas de hausses de récolte, la case n'est pas affichée
		let group;
		// lecture du cookie de nom 'groupRH'
		const cookieVal = document.cookie
			.split('; ')
			.find(row => row.startsWith('groupRH='))
			?.split('=')[1];
		if (cookieVal === undefined) {
			document.cookie = 'groupRH=true'+ cookieOpt;
			group = true;
		} else {
			group = cookieVal === 'true';
		}
		groupChk.checked = group;
		// Tri par ruche, puis par hausse
		const ordre = [[2, 'asc'], [1, 'asc']];
		const rowGOpt = {
			dataSrc: 2,
			startRender: null,
			endRender: tblStartRender
		};
		if (group) {
			new DataTable('#hausses', {
				orderFixed: ordre,
				rowGroup: rowGOpt
			});
		} else {
			new DataTable('#hausses', {
				order: ordre,
			});
		}
		groupChk.addEventListener('change', event => {
			group = event.target.checked;
			document.cookie = 'groupRH=' + group + cookieOpt;
			if (group) {
				new DataTable('#hausses', {
					destroy: true,
					orderFixed: ordre,
					rowGroup: rowGOpt
				});
			} else {
				new DataTable('#hausses', {
					destroy: true,
					order: ordre,
				});
			}
		});
	}

});
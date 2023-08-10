/* globals
	_csrf_token, _csrf_param_name, suppRecHauss, dateRecEpoch, enlevTtHauRec,
	enlevHRecDe30, urlRecHDepot, recId, total, DataTable
*/
'use strict';
document.addEventListener('DOMContentLoaded', () => {
	const itDate = document.getElementById('date');
	const itDateOk = document.getElementById('dateOK');
	itDate.style.display = 'none';
	itDateOk.style.display = 'none';
	document.getElementById('enlevehausses').addEventListener('click', function() {
		/*[- */
		// La fonction est désactivée après 30 jours 
		// 30 * 24 * 3600 * 1000 =  2592000000 
		/* -]*/
		if (Date.now() > dateRecEpoch * 1000 + 2592000000) {
			alert(enlevHRecDe30);
			return;
		}
		if (!confirm(enlevTtHauRec)) {
			return;
		}
		itDate.style.display = 'block';
		itDateOk.style.display = 'block';
		const d = new Date().toISOString().replace('T', ' ');
		itDate.setAttribute('value', d.slice(0, d.lastIndexOf(':')));
		itDate.focus();
	});
	itDateOk.addEventListener('click', function() {
		const date = itDate.value;
		itDate.style.display = 'none';
		itDateOk.style.display = 'none';
		const requestData = {};
		requestData[_csrf_param_name] = _csrf_token;
		requestData.date = date;
		$.post(urlRecHDepot + recId, requestData).done(function(data) {
			alert(data);
		});
	});
	document.getElementById('supprime').addEventListener('click', (event) => {
		if (confirm(suppRecHauss)) {
			return;
		}
		event.preventDefault();
	});
	function addCell(tr, content, colSpan = 1, classN = '') {
		const td = document.createElement('td');
		td.colSpan = colSpan;
		td.textContent = content;
		if (classN !== '') { td.className = classN }
		tr.appendChild(td);
	}
	new DataTable('#hausses', {
		orderFixed: [[1, 'asc'], [0, 'asc']],
		rowGroup: {
			dataSrc: 1, // on groupe sur les ruches
			startRender: null,
			endRender: function(rows, group) {
				const mielTotal = rows.data().pluck(4).reduce(function(a, b) {
					return a + parseFloat(b.replace(/,/g, '.'));
				}, 0.00);
				const tr = document.createElement('tr');
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
		}
	});
});
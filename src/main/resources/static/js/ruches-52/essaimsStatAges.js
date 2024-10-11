/* globals d3, Chart, agesHisto, pas, 
	ageDesReines, nbReinesTotal, Reines
*/
'use strict';

// https://github.com/code-nebula/chart-color-generator
function calculatePoint(i, intervalSize, colorRangeInfo) {
	const { colorStart, colorEnd, useEndAsStart } = colorRangeInfo;
	return (useEndAsStart ? (colorEnd - (i * intervalSize))
		: (colorStart + (i * intervalSize)));
}

function interpolateColors(dataLength, colorScale, colorRangeInfo) {
	const { colorStart, colorEnd } = colorRangeInfo;
	const intervalSize = (colorEnd - colorStart) / dataLength;
	let colorPoint;
	const colorArray = [];
	for (let i = 0; i < dataLength; i++) {
		colorPoint = calculatePoint(i, intervalSize, colorRangeInfo);
		colorArray.push(colorScale(colorPoint));
	}
	return colorArray;
}

document.addEventListener('DOMContentLoaded', () => {

	new DataTable('#essaims', {
		paging: true,
		searching: false,
		info: true,
		scrollX: true
	});
	
	// Voir infos.html
	document.getElementById('collapseB').addEventListener('shown.bs.collapse', () => {
		$($.fn.dataTable.tables(true)).DataTable().columns.adjust();
	}, { once: true });

	/*
		new DataTable('#essaims', {
			// actifs :
			// le controle de pagination en bas de la page est icomplet à l'initialisation
			// le tri sur duree passe par trois états !
			order: [[1, 'asc']],
			layout: {
				topStart: {
					buttons: [
						'pageLength'
					]
				}
			}
		});
	*/

	const cookieOpt = ';SameSite=Strict;path=' + window.location.pathname;
	// Pour le formulaire de choix du nombre de mois par tranche d'âge
	//  select id="pas"
	document.getElementById('pas').addEventListener('change', event => {
		document.cookie = 'p=' + event.target.value + cookieOpt;
		event.target.form.submit();
	});
	document.getElementById('actif').addEventListener('change', event => {
		// document.cookie = 'a=' + event.target.value + cookieOpt;
		event.target.form.submit();
	});

	const colorScale = d3.interpolateRainbow;
	const colorRangeInfo = {
		colorStart: 0,
		colorEnd: 1,
		useEndAsStart: false
	};
	const colors = interpolateColors(agesHisto.length, colorScale, colorRangeInfo);
	const labels = [];
	for (let i = 0; i < agesHisto.length; i++) {
		labels[i] = (i * pas) + '-' + ((i + 1) * pas) + 'mois';
	}
	const data = {
		datasets: [{
			data: agesHisto,
			backgroundColor: colors,
			label: Reines,
		}],
		labels: labels
	};
	new Chart('ctx', {
		type: 'bar',
		data: data,
		options: {
			plugins: {
				legend: {
					display: false,
				}
			},
			scales: {
				x: {
					display: true,
					title: {
						display: true,
						text: ageDesReines,
						font: {
							size: 14,
						},
					}
				},
				y: {
					display: true,
					title: {
						display: true,
						text: nbReinesTotal + ' ' + agesHisto.reduce((a, c) => a + c, 0),
						font: {
							size: 12,
						},
					}
				}
			}
		},
	});


});
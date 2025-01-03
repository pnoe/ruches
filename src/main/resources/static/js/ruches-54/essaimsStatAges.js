/* globals d3, Chart, agesHisto, pas, 
	ageDesReines, nbReines, Reines, mois
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

	const tbl = new DataTable('#essaims', {
		order: [[1, 'asc']],
		paging: true,
		searching: false,
		info: true,
		scrollX: true
	});

	// Pour afficher correctement les headers de la table. Voir infos.html.
	document.getElementById('collapseB').addEventListener('shown.bs.collapse', () => {
		tbl.columns.adjust();
	}, { once: true });

	const cookieOpt = ';SameSite=Strict;path=' + window.location.pathname;
	// Pour le formulaire de choix du nombre de mois par tranche d'âge
	//  select id="pas"
	document.getElementById('pas').addEventListener('change', event => {
		document.cookie = 'p=' + event.target.value + cookieOpt;
		event.target.form.submit();
	});
	document.getElementById('actif').addEventListener('change', event => {
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
		labels[i] = (i * pas) + '-' + ((i + 1) * pas);
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
						text: ageDesReines + ' (' + mois + ')',
						font: {
							size: 14,
						},
					}
				},
				y: {
					display: true,
					title: {
						display: true,
						text: nbReines,
						font: {
							size: 12,
						},
					}
				}
			}
		},
	});


});
/* globals d3, Chart, agesHisto, pas, ageMinJours, ageMaxJours, Max, Min, ageMoyenJours, 
	EcartType, ageVarianceJours, Moyenne, ageDesReines, nbReinesTotal, jours, Reines
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
						text: ageDesReines + ' - ' +
							Min + ' ' + ageMinJours + ' ' +
							Max + ' ' + ageMaxJours + ' ' +
							Moyenne + ' ' + ageMoyenJours + ' ' +
							EcartType + ' ' + ageVarianceJours + jours + ' ',
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
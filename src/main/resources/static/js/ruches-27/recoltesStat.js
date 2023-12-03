/* globals 
	d3, Chart, poidsmiel, nomsessaims, poidsTotal
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
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	new Chart('ctx', {
		type: 'doughnut',
		data: {
			datasets: [{
				data: poidsmiel,
				backgroundColor: interpolateColors(poidsmiel.length, d3.interpolateRainbow, {
					colorStart: 0,
					colorEnd: 1,
					useEndAsStart: false
				})
			}],
			labels: nomsessaims
		},
		options: {
			plugins: {
				legend: {
					position: 'bottom'
				},
				tooltip: {
					callbacks: {
						label: function(tooltipItem) {
							const poids = tooltipItem.dataset.data[tooltipItem.dataIndex];
							return tooltipItem.label + ' ' + (poids / 1000).toLocaleString(lang, digits2) + 'kg ' +
								(poids * 100 / poidsTotal).toLocaleString(lang, digits2) + '%';
						}
					}
				}
			}
		}
	});
});
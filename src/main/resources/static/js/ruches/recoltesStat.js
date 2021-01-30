/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals d3, Chart,
   poidsmiel, nomsessaims, poidsTotal
*/

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

function graphe() {
	const colorScale = d3.interpolateRainbow;
	const colorRangeInfo = {
		colorStart: 0,
		colorEnd: 1,
		useEndAsStart: false
	};
	const colors = interpolateColors(poidsmiel.length, colorScale, colorRangeInfo);
	const data = {
		datasets: [{
			data: poidsmiel,
			backgroundColor: colors
		}],
		labels: nomsessaims
	};
	new Chart('ctx', {
		type: 'doughnut',
		data: data,
		options: {
			plugins: {
				legend: {
					position: 'bottom'
				},
				tooltip: {
					callbacks: {
						label: function(tooltipItem) {
							const poids = tooltipItem.dataset.data[tooltipItem.dataIndex];
							return tooltipItem.label + ' ' + (poids / 1000).toFixed(2) + 'kg ' +
								(poids * 100 / poidsTotal).toFixed(2) + '%';
						}
					}
				}
			}
		}
	});
}
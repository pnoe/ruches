/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals d3, Chart
*/

let barChart;

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
	const colors = interpolateColors(agesHisto.length, colorScale, colorRangeInfo);
	let labels = [];
	for (let i = 0; i < agesHisto.length; i++) {
		labels[i] = (i * 6) + '-' + ((i + 1) * 6) + 'mois';
	}
	const data = {
		datasets: [{
			data: agesHisto,
			backgroundColor: colors
		}],
		labels: labels
	};
	barChart = new Chart('ctx', {
		type: 'bar',
		data: data,
		options: {
		    plugins: {
		      legend: {
		        display: false,
		      }
		    }
		  },
	});

	
}
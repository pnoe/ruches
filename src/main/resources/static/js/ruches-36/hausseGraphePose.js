/* globals dates, nbPosees */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length == 0) { return; }
	Chart.defaults.elements.point.radius = 6; // default = 3
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				label: 'Nombre de hausses posées',
				yAxisID: 'y',
				data: dates.map((v, i) => { return [v * 1000, nbPosees[i]]; })
			}],
		},
		options: {
			scales: {
				x: {
					type: 'time'
				},
				y: {
					position: 'left'
				}
			},
			plugins: {
				zoom: {
					// https://www.chartjs.org/chartjs-plugin-zoom
					// https://www.chartjs.org/chartjs-plugin-zoom/samples/drag/timeseries.html
					// https://www.chartjs.org/chartjs-plugin-zoom/guide/options.html
					pan: {
						enabled: true,
						// modifierKey: 'ctrl',
						mode: 'x',
						//  [1 542 383 004,1614188220,1616584080,1650031920];
						// threshold: 10000000
					},
					limits: {
						x: { min: 'original', max: 'original' },
					},
					zoom: {
						wheel: {
							enabled: true,
						},
						pinch: {
							enabled: true
						},
						mode: 'x'
					}
				},
				legend: {
					labels: {
						usePointStyle: true,
					},
				}
			}
		}
	});
	document.getElementById('zoomini').addEventListener('click', () => {
		graphe.resetZoom();
	});
	document.getElementById('scales').addEventListener('click', e => {
		graphe.options.scales.y.display = e.target.checked;
		graphe.update();
	});
});
/* globals Chart, datesNb, ruchestxt */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (datesNb.length === 0) { return; }
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				stepped: true,
				label: ruchestxt,
				yAxisID: 'y',
				data: datesNb
			}]
		},
		options: {
			// https://www.chartjs.org/docs/latest/configuration/responsive.html
			// Si false, le taille du canevas augmente verticallement indéfiniment
			maintainAspectRatio: true,
			scales: {
				x: {
					type: 'time',
					time: {
						minUnit: 'day',
						tooltipFormat: 'DDD'
						// https://moment.github.io/luxon/#/formatting
						// https://www.chartjs.org/docs/latest/samples/scales/time-line.html
					}
				},
				y: {
					position: 'left'
				}
			},
			plugins: {
				zoom: {
					pan: {
						enabled: true,
						mode: 'x',
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
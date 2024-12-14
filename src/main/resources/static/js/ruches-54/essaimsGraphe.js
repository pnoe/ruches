/* globals Chart, ruches, datesTotal, ruchesProd, datesProdTotal, essaims, dates, datesProd, essaimsProd  */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	const graphe = new Chart('ctx', {
		type: 'line',
		data: {
			datasets: [{
				// stepped: true,
				label: essaims,
				yAxisID: 'y',
				data: dates
			}, {
				// stepped: true,
				label: essaimsProd,
				yAxisID: 'y',
				data: datesProd
			}, {
				// stepped: true,
				label: ruches,
				yAxisID: 'y',
				data: datesTotal,
				hidden: true,
			}, {
				// stepped: true,
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal,
				hidden: true,
			}]
		},
		options: {
			// https://www.chartjs.org/docs/latest/configuration/responsive.html
			maintainAspectRatio: false,
			// https://www.chartjs.org/docs/latest/configuration/elements.html
			elements: { line: { stepped: true } },
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
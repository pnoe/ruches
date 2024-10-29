/* globals Chart, ruches, datesTotal, ruchesProd, datesProdTotal, 
  datesProd, essaimsProd, dates, essaims */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (datesTotal.length === 0) { return; }
	const graphe = new Chart('ctx', {
		type: 'line',
		data: {
			datasets: [{
				label: ruches,
				yAxisID: 'y',
				data: datesTotal
			}, {
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal
			}, {
				label: essaims,
				yAxisID: 'y',
				data: dates,
				hidden: true,
			}, {
				label: essaimsProd,
				yAxisID: 'y',
				data: datesProd,
				hidden: true,
			}],
		},
		options: {
			// https://www.chartjs.org/docs/latest/configuration/elements.html
			elements: {line : {stepped: true}},
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
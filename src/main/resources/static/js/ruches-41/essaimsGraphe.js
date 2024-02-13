/* globals Chart, ruches, datesTotal, ruchesProd, datesProdTotal, essaims, dates, datesProd, essaimsProd  */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				stepped: true,
				label: essaims,
				yAxisID: 'y',
				data: dates
			}, {
				type: 'line',
				stepped: true,
				label: essaimsProd,
				yAxisID: 'y',
				data: datesProd
			}, {
				type: 'line',
				stepped: true,
				label: ruches,
				yAxisID: 'y',
				data: datesTotal,
				hidden: true,
			}, {
				type: 'line',
				stepped: true,
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal,
				hidden: true,
			}]
		},
		options: {
			scales: {
				x: {
					type: 'time',
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
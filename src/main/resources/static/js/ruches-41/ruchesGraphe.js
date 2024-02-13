/* globals Chart, ruches, datesTotal, ruchesProd, datesProdTotal, 
  datesProd, essaimsProd, dates, essaims */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (datesTotal.length === 0) { return; }
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				stepped: true,
				label: ruches,
				yAxisID: 'y',
				data: datesTotal
			}, {
				type: 'line',
				stepped: true,
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal
			}, {
				type: 'line',
				stepped: true,
				label: essaims,
				yAxisID: 'y',
				data: dates,
				hidden: true,
			}, {
				type: 'line',
				stepped: true,
				label: essaimsProd,
				yAxisID: 'y',
				data: datesProd,
				hidden: true,
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
/* globals Chart, datesTotal, nbTotal */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (datesTotal.length === 0) { return; }
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				label: 'Ruches',
				yAxisID: 'y',
				data: datesTotal.map((v, i) => { return [v * 1000, nbTotal[i]]; }),
			}],
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
			/*
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
			*/
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
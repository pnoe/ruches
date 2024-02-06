/* globals Chart, ruches, datesTotal, nbTotal, ruchesProd, datesProdTotal, nbProdTotal */
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
				data: datesTotal.map((v, i) => { return [v * 1000, nbTotal[i]]; }),
			}, {
				type: 'line',
				stepped: true,
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal.map((v, i) => { return [v * 1000, nbProdTotal[i]]; }),
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
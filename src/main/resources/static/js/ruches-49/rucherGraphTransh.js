/* globals Chart, datesNb, ruchestxt, ruches, type, ajout, ajouttxt, retraittxt, 
datesRec, recNbRuch, pdsListe, pdsPPtxt */
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
			}, {
				type: 'line',
				label: 'Récolte',
				yAxisID: 'yr',
				data: datesRec.map((v, i) => { return [v * 1000, pdsListe[i]]; }),
				pointStyle: 'rectRot'
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
				},
				yr: {
					position: 'right'
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
				tooltip: {
					usePointStyle: true,
					callbacks: {
						afterLabel: function(context) {
							if (context.datasetIndex === 0) {
								if (ajout[context.dataIndex] !== '') {
									return [ruches[context.dataIndex],
									((type[context.dataIndex] === 1) ? ajouttxt : retraittxt) +
									' ' + ajout[context.dataIndex].split(',').length +
									': ' + ajout[context.dataIndex]];
								}
							} else {
								return [ruchestxt + ': ' + recNbRuch[context.dataIndex],
								pdsPPtxt + ': ' + (pdsListe[context.dataIndex] / recNbRuch[context.dataIndex]).toFixed(2)];
							}
						}
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
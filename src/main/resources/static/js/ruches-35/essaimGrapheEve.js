/* globals Chart, dates, poids, poidsSucre, datesSucre,
 poidsRec, datesRec, ruchersRec, datesTrait,
 datesRucher, nomsRucher, datesCadre, nbsCadre, vide
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (vide) { return; }
	Chart.defaults.elements.point.radius = 6; // default = 3
	const graphe = new Chart('ctx', {
		data: {
			// dates evenement.getDate().toEpochSecond(ZoneOffset.UTC));
			// poids Float.parseFloat(evenement.getValeur()));
			// https://www.chartjs.org/docs/latest/axes/cartesian/time.html
			// https://www.chartjs.org/docs/latest/charts/mixed.html
			// https://www.chartjs.org/docs/latest/charts/bar.html#barthickness
			// https://www.chartjs.org/docs/latest/samples/line/multi-axis.html
			// https://www.chartjs.org/docs/latest/configuration/elements.html#point-styles
			datasets: [{
				type: 'line',
				label: 'Pesée',
				yAxisID: 'yp',
				data: dates.map((v, i) => { return [v * 1000, poids[i]]; })
			}, {
				type: 'line',
				label: 'Sucre',
				yAxisID: 'ys',
				data: datesSucre.map((v, i) => { return [v * 1000, poidsSucre[i]]; }),
				pointStyle: 'rectRounded'
			}, {
				type: 'line',
				label: 'Récolte',
				yAxisID: 'yr',
				data: datesRec.map((v, i) => { return [v * 1000, poidsRec[i]]; }),
				pointStyle: 'rectRot'
			}, {
				type: 'scatter',
				label: 'Traitements',
				yAxisID: 'yt',
				data: datesTrait.map(v => { return [v * 1000, 0]; }),
				pointStyle: 'rect'
			}, {
				type: 'scatter',
				label: 'Rucher',
				yAxisID: 'yt',
				data: datesRucher.map(v => { return [v * 1000, 0]; }),
				pointStyle: 'triangle'
			}, {
				type: 'line',
				stepped: true,
				label: 'Cadres',
				yAxisID: 'yc',
				data: datesCadre.map((v, i) => { return [v * 1000, nbsCadre[i]]; }),
				pointStyle: 'star'
			}],
		},
		options: {
			scales: {
				x: {
					type: 'time'
				},
				yp: {
					position: 'left'
				},
				ys: {
					position: 'right'
				},
				yr: {
					position: 'right'
				},
				yt: {
					display: false
				},
				yc: {
					display: false
				}
			},
			plugins: {
				zoom: {
					limits: {
						x: { min: 'original', max: 'original' },
					},
					/*
					pan: {
						enabled: true,
						// scaleMode: 'x',
						// modifierKey: 'ctrl',
						mode: 'x',
						//  [1 542 383 004,1614188220,1616584080,1650031920];
						threshold: 1000000
					},
					*/
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
				},
				tooltip: {
					usePointStyle: true,
					callbacks: {
						afterLabel: function(context) {
							// https://www.chartjs.org/docs/latest/configuration/tooltip.html
							// footer est global pour tous les datasets.
							if (context.datasetIndex === 2) {
								// Ajouter le nom du rucher pour la courbe des récoltes.
								return ruchersRec[context.dataIndex];
							} else if (context.datasetIndex === 4) {
								// Si dataSet des changements de ruchers, ajouter le nom du rucher.
								return nomsRucher[context.dataIndex];
							}
						},
						label: function(context) {
							if (context.datasetIndex === 3) {
								// Si dataSet des traitements, ne pas afficher y.
								// context.parsed.x renvoie le timestamp
								// context.dataset.label renvoie le label du dataset
								return [(new Date(context.parsed.x)).toLocaleString(), context.dataset.label];
							} else if (context.datasetIndex === 4) {
								// Si dataSet des chgt de ruchers, ne pas afficher y. Ajout de ' : '
								// et nom du rucher dans afterLabel.
								return [(new Date(context.parsed.x)).toLocaleString(), context.dataset.label + ' : '];
							}
						},
					}
				}
			}
		}
	});
	const zoomini = document.getElementById('zoomini');
	zoomini.addEventListener('click', () => {
		graphe.resetZoom();
	});
});
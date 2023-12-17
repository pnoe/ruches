/* globals Chart, dates, poids, poidsSucre, datesSucre
 , poidsRec, datesRec, ruchersRec, datesTrait
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	new Chart('ctx', {
		data: {
			// dates evenement.getDate().toEpochSecond(ZoneOffset.UTC));
			// poids Float.parseFloat(evenement.getValeur()));
			// https://www.chartjs.org/docs/latest/axes/cartesian/time.html
			// https://www.chartjs.org/docs/latest/charts/mixed.html
			// https://www.chartjs.org/docs/latest/charts/bar.html#barthickness
			// https://www.chartjs.org/docs/latest/samples/line/multi-axis.html
			datasets: [{
				type: 'line',
				label: 'Ruche',
				yAxisID: 'yp',
				data: dates.map((v, i) => { return [v * 1000, poids[i]]; })
			}, {
				type: 'line', // scatter',
				label: 'Sucre',
				yAxisID: 'ys',
				data: datesSucre.map((v, i) => { return [v * 1000, poidsSucre[i]]; })
			}, {
				type: 'line', // scatter',
				label: 'Récolte',
				yAxisID: 'yr',
				data: datesRec.map((v, i) => { return [v * 1000, poidsRec[i]]; })
			}, {
				type: 'scatter',
				label: 'Traitements',
				yAxisID: 'yt',
				data: datesTrait.map(v => { return [v * 1000, 0]; })
			}],
		},
		options: {
			scales: {
				x: {
					type: 'time',
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
				}
			},
			plugins: {
				tooltip: {
					callbacks: {
						afterLabel: function(context) {
							// Ajouter le nom du rucher pour la courbe des récoltes.
							// https://www.chartjs.org/docs/latest/configuration/tooltip.html
							// footer est global pour tous les datasets.
							if (context.datasetIndex === 2) {
								// Si dataSet des récoltes.
								return ruchersRec[context.dataIndex];
							}
						},
						label: function(context) {
							if (context.datasetIndex === 3) {
								// Si dataSet des traitements, ne pas afficher y.
								// context.parsed.x renvoie le timestamp
								// context.dataset.label renvoie le label du dataset
								return [(new Date(context.parsed.x)).toLocaleString(), context.dataset.label];
							}
						}
					}
				}
			}
		}
	});
});
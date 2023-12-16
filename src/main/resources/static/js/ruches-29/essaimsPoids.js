/* globals Chart, dates, poids
 , poidsSucre, datesSucre
 , poidsRec, datesRec, ruchersRec
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
				label: 'Poids',
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
				}
			},
			plugins: {
				tooltip: {
					callbacks: {
						afterLabel: function(context) {
							// Ajouter le nom du rucher pour la courbe des récoltes.
							// https://www.chartjs.org/docs/latest/configuration/tooltip.html
							if (context.datasetIndex === 2) {
								// Si dataSet des récoltes.
								return ruchersRec[context.dataIndex];
							}
						}
					}
				}
			}
		}
	});
});
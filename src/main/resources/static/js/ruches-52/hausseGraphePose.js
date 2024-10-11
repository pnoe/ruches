/* globals Chart, dates, nbPosees, datesTotal, nbTotal, datesRec, nbHaussesRec
  nbHaussesP, nbHaussesT, recNbHausses  
 */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	const graphe = new Chart('ctx', {
		type: 'line',
		data: {
			datasets: [{
				stepped: true,
				label: nbHaussesP,
				yAxisID: 'y',
				data: dates.map((v, i) => { return [v * 1000, nbPosees[i]]; }),
				fill: true
			}, {
				stepped: true,
				label: nbHaussesT,
				yAxisID: 'y',
				data: datesTotal.map((v, i) => { return [v * 1000, nbTotal[i]]; }),
				fill: '-1'
			}, {
				// type: 'bar',   rien ne s'affiche !?
				// https://www.chartjs.org/docs/latest/charts/mixed.html
				// https://www.chartjs.org/docs/latest/samples/other-charts/combo-bar-line.html
				// idem si ce dataset est mis en premier.
				// idem si type: 'bar' est mis avant data:
				// idem si même axe y
				type: 'scatter',
				label: recNbHausses,
				yAxisID: 'y',
				data: datesRec.map((v, i) => { return [v * 1000, nbHaussesRec[i]]; }),
				pointStyle: 'triangle'
			}],
		},
		options: {
			scales: {
				x: {
					type: 'time',
					// On limite aux dates du dataset des hausses posées.
					min: dates[0] * 1000 - 200000000,
					max: dates[dates.lentgh - 1] * 1000 + 200000000
				},
				y: {
					position: 'left'
				}
			},
			plugins: {
				zoom: {
					// https://www.chartjs.org/chartjs-plugin-zoom
					// https://www.chartjs.org/chartjs-plugin-zoom/samples/drag/timeseries.html
					// https://www.chartjs.org/chartjs-plugin-zoom/guide/options.html
					pan: {
						enabled: true,
						// modifierKey: 'ctrl',
						mode: 'x',
						//  [1 542 383 004,1614188220,1616584080,1650031920];
						// threshold: 10000000
					},
					limits: {
						// les limites ne fonctionnent pas bien avec min et max imposés sur x
						// ça ne marche pas mieux avec les valeurs de "dates" au lieu de 'original'
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
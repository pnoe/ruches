/* globals Chart, essaims, dates, poids, ruches
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	const datasets = [];
	dates.forEach((d, i) => {
		datasets.push(
			{
				label: essaims[i].nom,
				data: d.map((v, j) => { return [v * 1000, poids[i][j] - ruches[i].poidsVide] })
			}
		);
	});
	new Chart('ctx', {
		type: 'line',
		data: {
			datasets: datasets
		},
		options: {
			scales: {
				x: {
					type: 'time',
				}
			},
		},
	});
});
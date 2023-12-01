/* globals Chart, essaim, dates, poids
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	new Chart('ctx', {
		type: 'line',
		data: {
			datasets: [{
				label: essaim.nom,
				data: dates.map((v, i) => { return [v * 1000, poids[i]]; })
			}],
		},
		options: {
			scales: {
				x: {
					type: 'time',
				},
				y: {
				}
			},
		},
	});
});
/* globals Chart, essaim, dates, poids
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	/*
	const data = dates.map((v, i) => { return [v * 1000, poids[i]]; });
	const min = Math.min(...data.map(x => x[0]));
	const max = Math.max(...data.map(x => x[0]));
	alert(JSON.stringify(data)
		+ ' -essaim.nom- ' + essaim.nom
		+ ' -min- ' + min
		+ ' -max- ' + max
	);
	*/
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
					// min: min,
					// max: max,
				},
				y: {
				}
			},
		},
	});
});
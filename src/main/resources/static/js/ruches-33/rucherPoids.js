/* globals Chart, essaims, dates, poids, ruches, ruche
*/
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (essaims.length === 0) { return; }
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
			plugins: {
				tooltip: {
					callbacks: {
						footer: (items => {
							let txt = '';
							items.forEach(function(item) {
								// Boucle si plusieurs points superposés (x et y identiques)
								//   le footer regroupe les deux ruches et leurs poids.
								//   \n fonctionne bien, c'est vérifié.
								// Affiche en pied de tooltip : 
								// Ruche: <nom> <poidsTotal> kg
								txt += ruche + ': ' + ruches[item.datasetIndex].nom + ' ' + 
								(item.parsed.y +  ruches[item.datasetIndex].poidsVide) + 'kg\n';
							});
							return txt.slice(0, -1);
						}),
					}
				}
			}
		},
	});
});
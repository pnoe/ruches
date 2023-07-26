/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals Chart, Hausses, Pots, HaussesEssaim,
	  poidsMielHausses, poidsMielPots, debutAnnee, nbIEssaims
*/
/* exported grapheRecolte */
'use strict';
function grapheRecolte() {
	const labels = [];
	for (let i = 0; i < poidsMielHausses.length; i++) {
		labels[i] = i + debutAnnee;
	}
	const data = {
		datasets: [{
			data: poidsMielHausses,
			backgroundColor: '#40f373',
			label: Hausses,
			yAxisID: 'ypoids',
		},
		{
			data: poidsMielPots,
			backgroundColor: '#1ac7c2',
			label: Pots,
			yAxisID: 'ypoids',
		},
		{
			data: nbIEssaims,
			backgroundColor: '#ff596b',
			label: HaussesEssaim,
			yAxisID: 'ypoidsessaim',
		},
		],
		labels: labels
	};
	new Chart('ctx', {
		type: 'bar',
		data: data,
		options: {
			plugins: {
				legend: {
					display: true,
					position: 'bottom',
				},
			},
			scales: {
				ypoids: {
					type: 'linear',
					display: 'auto',
					position: 'left'
				},
				ypoidsessaim: {
					type: 'linear',
					display: 'auto',
					position: 'right',
					grid: {
						drawOnChartArea: false
					}
				}
			}
		},
	});
}
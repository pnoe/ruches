/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals Chart
*/

function grapheRecolte() {
	let labels = [];
	for (let i = 0; i < poidsMielHausses.length; i++) {
		labels[i] = i + debutAnnee;
	}
	const data = {
		datasets: [{
			data: poidsMielHausses,
			backgroundColor: '#40f373',
			label: 'Hausses',
			
		},{
			data: poidsMielPots,
			backgroundColor: '#1ac7c2',
			label: 'Pots',
			
		},
		],
		labels: labels
	};
	new Chart('ctx', {
		type: 'bar',
		data: data,	
		options: {
			plugins: {
				title: {
					display: true,
					text: 'Poids de miel par année en kg',
					position: 'bottom',
				},
				legend: {
					display: true,
					position: 'bottom',
					// title: 'Hello !',
				},
			},
		},
	});
}
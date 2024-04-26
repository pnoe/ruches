/* globals Chart, ruches, datesTotal, ruchesProd, datesProdTotal, essaims, dates, datesProd, essaimsProd  */
'use strict';

document.addEventListener('DOMContentLoaded', () => {
	if (dates.length === 0) { return; }
	const graphe = new Chart('ctx', {
		data: {
			datasets: [{
				type: 'line',
				stepped: true,
				label: essaims,
				yAxisID: 'y',
				data: dates
			}, {
				type: 'line',
				stepped: true,
				label: essaimsProd,
				yAxisID: 'y',
				data: datesProd
			}, {
				type: 'line',
				stepped: true,
				label: ruches,
				yAxisID: 'y',
				data: datesTotal,
				hidden: true,
			}, {
				type: 'line',
				stepped: true,
				label: ruchesProd,
				yAxisID: 'y',
				data: datesProdTotal,
				hidden: true,
			}]
		},
		options: {
			// https://www.chartjs.org/docs/latest/configuration/responsive.html
			maintainAspectRatio: false,
			scales: {
				x: {
					type: 'time',
					time: {
						minUnit: 'day',
						tooltipFormat: 'DDD'
						// https://moment.github.io/luxon/#/formatting
						// https://www.chartjs.org/docs/latest/samples/scales/time-line.html
					}
				},
				y: {
					position: 'left'
				}
			},
			plugins: {
				zoom: {
					pan: {
						enabled: true,
						mode: 'x',
					},
					limits: {
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

/*
	window.addEventListener("resize", resizeCanvas, false);
	const canvas = document.getElementById("ctx");
	function resizeCanvas() {
		if (window.innerHeight >= (9 * window.innerWidth / 16)) {
			canvas.width = window.innerWidth;
			canvas.height = Math.floor(9 * canvas.width / 16);
		}
		else {
			canvas.height = window.innerHeight;
			canvas.width = Math.floor(16 * canvas.height / 9);
		}
		graphe.update();
	};
*/ 
  
	
	
});
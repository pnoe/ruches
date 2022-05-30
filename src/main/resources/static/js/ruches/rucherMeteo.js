/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals longitude,latitude,openweathermapKey,urlPrefix,Chart */
"use strict";
function rucherMeteo() {
	//	https://erikflowers.github.io/weather-icons/
	//	https://openweathermap.org/api/one-call-api
	$("#accordion").accordion({
		active: 0,
		collapsible: true,
		heightStyle: 'content'
	});
	const optDateTime = { day: "numeric", month: "numeric", hour: "numeric", minute: "numeric" };
	const optDate = { day: "numeric", month: "numeric" };
	
	const lang = navigator.language;
	const digits1 = {maximumFractionDigits:1};
	const digits2 = {maximumFractionDigits:2};
	
	const urlOneCall = urlPrefix + '?lat=' +
		latitude + '&lon=' + longitude +
		'&units=metric&lang=fr&APPID=' + openweathermapKey;
	$.ajax({
		url: urlOneCall + '&exclude=minutely,alerts'
	}).done(function(data) {
		const dt = data.current.dt;
		$('#date').html('<a href="' + urlOneCall + '" target="_blank">' + fdt(dt) + '</a>');
		$('#description').html(data.current.weather[0].description);
		$('#temperature').html(data.current.temp.toLocaleString(lang, digits1) + '°C');
		$('#humidite').html(data.current.humidity + '%');
		$('#rosee').html(data.current.dew_point.toLocaleString(lang, digits1) + '°C');
		$('#pression').html(data.current.pressure + 'hPa');
		$('#tempRessentie').html(data.current.feels_like.toLocaleString(lang, digits1) + '°C');
		$('#uvi').html(data.current.uvi.toLocaleString(lang, digits2));
		$('#ventVitesse').html((data.current.wind_speed * 3.6).toLocaleString(lang, digits1) + 'km/h');
		$('#ventDirection').html(data.current.wind_deg + '° ' + degToCard(data.current.wind_deg) +
			'&nbsp<i class="wi wi-wind from-' + data.current.wind_deg + '-deg"></i>');
		$('#ventRafales').html((data.current.hasOwnProperty('wind_gust') ? (data.current.wind_gust * 3.6).toLocaleString(lang, digits1) : '0') + 'km/h');
		$('#nuages').html(data.current.clouds + '%');
		$('#visibilite').html(data.current.visibility + 'm');
		$('#pluieVol1h').html((data.current.hasOwnProperty('rain') ? (data.current.rain['1h']) : '0') + 'mm');
		$('#neigeVol1h').html((data.current.hasOwnProperty('snow') ? (data.current.snow['1h']) : '0') + 'mm');
		$('#leverSoleil').html(fdt(data.current.sunrise));
		$('#coucherSoleil').html(fdt(data.current.sunset));
		$('#leverLune').html(fdt(data.daily[0].moonrise));
		$('#coucherLune').html(fdt(data.daily[0].moonset));
		$('#phaseLune').html(data.daily[0].moon_phase.toLocaleString(lang, digits1) +
			'&nbsp<i class="wi wi-moon-alt-' + moonIcon(data.daily[0].moon_phase) + '"></i>');
		let htmlPrev = '';
		data.daily.forEach(function(day) {
			htmlPrev += '<tr><td>' + fd(day.dt) + '</td><td>' +
				day.temp.min.toLocaleString(lang, digits1) + '°C</td><td>' +
				day.temp.max.toLocaleString(lang, digits1) + '°C</td><td>' +
				day.clouds + '%' + '</td><td>' +
				(day.rain ? day.rain : 0) + 'mm' + '</td><td>' +
				day.pop.toLocaleString(lang, digits2) + '</td><td>' +
				fdt(day.sunrise) + '</td><td>' +
				fdt(day.sunset) + '</td><td>' +
				fdt(day.moonrise) + '</td><td>' +
				fdt(day.moonset) + '</td><td>' +
				day.moon_phase.toLocaleString(lang, digits2) + '&nbsp<i class="wi wi-moon-alt-' +
				moonIcon(day.moon_phase) + '"></i></td></tr>';
		});
		$('#previsions').append(htmlPrev);
		tempChart(data.hourly);
		const urlHistoPref = urlPrefix + '/timemachine?lat=' +
			latitude + '&lon=' + longitude +
			'&units=metric' +
			'&lang=fr' +
			'&APPID=' + openweathermapKey +
			'&dt=';
		histo(urlHistoPref, parseInt(dt) - 86400, 5, '');
	});

	const CHART_COLORS = {
		red: 'rgb(255, 99, 132)',
		blue: 'rgb(54, 162, 235)'
	};

	function tempChart(dh) {
		let jour = 'xx';
		const labels = dh.map(x => {
				const ddhh = new Date(x.dt * 1000).toLocaleString(undefined,	{ day: "numeric", hour: "numeric" });
				const ret = (jour === ddhh.substring(0, 2)) ? ddhh.substring(ddhh.lastIndexOf(',') + 1) : ddhh;
				jour = ddhh.substring(0, 2);
				return ret;
		});
		new Chart('tempGraphe', {
			type: 'line',
			data: {
				labels: labels,
				datasets: [
					{
						data: dh.map(x => x.temp),
						label: 'Température',
						borderColor: CHART_COLORS.red,
						yAxisID: 'ytemp'
					},
					{
						data: dh.map(x => x.pressure),
						label: 'Pression',
						borderColor: CHART_COLORS.blue,
						yAxisID: 'ypress'
					}
				]
			},
			options: {
				plugins: {
					legend: {
						display: false
					}
				},
				interaction: {
					intersect: false,
					mode: 'index'
				},
				scales: {
					ytemp: {
						type: 'linear',
						display: true,
						position: 'left'
					},
					ypress: {
						type: 'linear',
						display: true,
						position: 'right',
						grid: {
          					drawOnChartArea: false
        				}
					}
				}
			}
		});
	}

	function histo(pref, time, n, htmlHisto) {
		if (n == 0) {
			$('#historique').append(htmlHisto);
			return;
		}
		const urlHisto = pref + time;
		$.ajax({
			url: urlHisto
		}).done(function(data) {
			htmlHisto += '<tr><td><a href="' + urlHisto + '" target="_blank">' +
				fdt(data.current.dt) + '</a></td><td>' +
				data.current.temp.toLocaleString(lang, digits1) + '°C</td><td>' +
				data.current.clouds + '%</td><td>' +
				(data.current.wind_speed * 3.6).toLocaleString(lang, digits1) + 'km/h</td><td>' +
				data.current.wind_deg + '° ' + degToCard(data.current.wind_deg) +
				'&nbsp<i class="wi wi-wind from-' + data.current.wind_deg + '-deg"></i></td></tr>';
			histo(pref, time - 86400, n - 1, htmlHisto);
		});
	}

	function fdt(t) {
		return new Date(t * 1000).toLocaleString(undefined, optDateTime).replace(' à', '');
	}

	function fd(t) {
		return new Date(t * 1000).toLocaleString(undefined, optDate);
	}
	
	function degToCard(deg) {
		return ["N","NNE","NE","ENE","E","ESE",
		  "SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"][Math.round(deg / 22.5) % 16];
	}
	
	function moonIcon(phase) {
		return ['new','waxing-crescent-1','waxing-crescent-2','waxing-crescent-3','waxing-crescent-4','waxing-crescent-5',
		  'waxing-crescent-6','first-quarter','waxing-gibbous-1','waxing-gibbous-2','waxing-gibbous-3','waxing-gibbous-4',
		  'waxing-gibbous-5','waxing-gibbous-6','full','waning-gibbous-1','waning-gibbous-2','waning-gibbous-3','waning-gibbous-4',
		  'waning-gibbous-5','waning-gibbous-6','third-quarter','waning-crescent-1','waning-crescent-2','waning-crescent-3',
		  'waning-crescent-4','waning-crescent-5','waning-crescent-6'][Math.floor(phase * 28) % 28];  
	}
	
}

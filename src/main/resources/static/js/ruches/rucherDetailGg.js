/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals google, exportGpx, exportKml,
	rucheParcours, distParcours, rucher, rucherMapZoom, longitudeCentre, latitudeCentre,
	 rayonsButinage, distButinage, urlruches, nomHausses, essaimtxt, pasdessaimtxt,
	 ruchetxt, haussestxt, pasdehaussetxt, ruches, ruchestxt, distancedeparcourstxt,
	 entreetxt, _csrf_token */

let parcours;
let map;
const markersRuche = [];
let markerRucher;
let infowindowp;
const circlesButinage = [];
const lang = navigator.language;
const digits2 = {maximumFractionDigits:2};

function initMap() {
	map = new google.maps.Map(document.getElementById('map'), {
		center: {
			lat: rucher.latitude,
			lng: rucher.longitude
		},
		zoom: parseInt(rucherMapZoom),
		mapTypeId: 'satellite'
	});
	const infowindow = new google.maps.InfoWindow();
	for (let i = 0; i < ruches.length; i++) {
		const markerRuche = new google.maps.Marker({
			position: {
				lat: ruches[i].latitude,
				lng: ruches[i].longitude
			},
			map: map,
			icon: {
				path: "M-20,0a20,20 0 1,0 40,0a20,20 0 1,0 -40,0",
				fillColor: (ruches[i].essaim == null) ? '#FE00FE' : ruches[i].essaim.reineCouleurMarquage,
				fillOpacity: 0.5,
				anchor: new google.maps.Point(0, 0),
				strokeWeight: 0,
				scale: 0.70
			},
			label: {
				text: (((ruches[i].essaim != null) && ruches[i].essaim.reineMarquee) ? '*' : '') + ruches[i].nom,
				fontSize: '15px',
				fontWeight: ((ruches[i].essaim != null) && ruches[i].essaim.reineMarquee) ? 'bold' : 'normal'
			},
			rucheid: ruches[i].id,
			essaimnom: (ruches[i].essaim == null) ? '' : ruches[i].essaim.nom,
			essaimid: (ruches[i].essaim == null) ? '' : ruches[i].essaim.id,
			haussesnom: nomHausses[i]
		});
		google.maps.event.addListener(markerRuche, 'dragend', function(event) {
			sauveRuchePosition(event, markerRuche);
		});
		google.maps.event.addListener(markerRuche, 'click', function() {
			infowindow.setContent(
				'<a href="' + urlruches + 'ruche/' + markerRuche.rucheid + '">' +
				ruchetxt + ' ' + markerRuche.label.text + "</a><br/>" +
				((markerRuche.essaimnom === '') ? pasdessaimtxt :
					'<a href="' + urlruches + 'essaim/' + markerRuche.essaimid + '">' +
					essaimtxt + ' ' + markerRuche.essaimnom + '</a>') +
				'<br/>' +
				((markerRuche.haussesnom === '') ? pasdehaussetxt :
					haussestxt + ' ' + markerRuche.haussesnom)
			);
			infowindow.open(map, markerRuche);
		});
		markersRuche.push(markerRuche);
	}
	markerRucher = new google.maps.Marker({
		position: {
			lat: rucher.latitude,
			lng: rucher.longitude
		},
		map: map,
		icon: {
			path: "M-20,0a20,20 0 1,0 40,0a20,20 0 1,0 -40,0",
			fillColor: '#FFFFFF',
			fillOpacity: 0.5,
			anchor: new google.maps.Point(0, 0),
			strokeWeight: 0,
			scale: 0.70
		},
		label: {
			text: entreetxt,
			fontSize: '15px',
			fontWeight: 'normal'
		}
	});
	google.maps.event.addListener(markerRucher, 'dragend', function(event) {
		sauveRucherPosition(event);
	});
	google.maps.event.addListener(markerRucher, 'click', function() {
		infowindow.setContent(
			ruches.length + ' ' + ruchestxt + '<br/>' + distancedeparcourstxt +
			' ' + distParcours.toLocaleString(lang, digits2) + 'm');
		infowindow.open(map, markerRucher);
	});
	markersRuche.push(markerRucher);
	newParcours();
	for (const r of rayonsButinage) {
		circlesButinage.push(new google.maps.Circle({
			strokeColor: "#0000FF",
			strokeOpacity: 0.8,
			strokeWeight: 1,
			fillColor: "#0000FF",
			fillOpacity: 0.02,
			map,
			center: {
				lat: latitudeCentre,
				lng: longitudeCentre
			},
			radius: r,
			visible: false
		}));
	}
}

function sauveRucherPosition(e) {
	const req = new XMLHttpRequest();
	req.open('POST',
		urlruches + 'rucher/deplace/' + rucher.id + '/' + e.latLng.lat() + '/' + e.latLng.lng(),
		true);
	req.setRequestHeader('x-csrf-token', _csrf_token);
	req.onload = function() {
		if (req.readyState === 4) {
			if (req.status === 200) {
				if (req.responseText !== "OK") {
					alert(req.responseText);
				} else {
					parcours.setMap(null);
					parcoursRedraw();
				}
			}
		}
	};
	req.send(null);
}

function parcoursRedraw(redraw = false) {
	// requete xmlhttp pour actualiser le parcours
	const req2 = new XMLHttpRequest();
	// const redrawX = redraw?'1':'0';
	if (redraw) {
		if (infowindowp === undefined) {
			infowindowp = new google.maps.InfoWindow();
		}
		infowindowp.close();
		infowindowp.setContent('Calcul en cours...');
		infowindowp.open(map, markerRucher);
	}
	req2.open('GET', urlruches + 'rucher/parcours/' + rucher.id + '/' + redraw, true);
	req2.onload = function() {
		if (req2.readyState === 4) {
			if (req2.status === 200) {
				let response = JSON.parse(req2.responseText);
				// distParcours et rucheParcours var globales             
				if (redraw && (response.distParcours + 0.1 > distParcours)) {
					infowindowp.close();
					infowindowp.setContent(
						"Pas d'amélioration<br/>" +
						distancedeparcourstxt + ' ' + distParcours.toLocaleString(lang, digits2) + 'm'
					);
					infowindowp.open(map, markerRucher);
					return;
				}
				if (redraw) { parcours.setMap(null); }
				let dist = distParcours;
				distParcours = response.distParcours;
				rucheParcours = response.rucheParcours;
				newParcours();
				// Pour affichage de la longueur du parcours après re-calcul sur popup Entrée
				if (redraw) {
					infowindowp.close();
					infowindowp.setContent(
						'La distance est diminuée de ' + (dist - distParcours).toLocaleString(lang, digits2) +
						'm<br/>' + distancedeparcourstxt +
						' ' + distParcours.toLocaleString(lang, digits2) + 'm'
					);
					infowindowp.open(map, markerRucher);
				}
			}
		}
	};
	req2.send();
}

function sauveRuchePosition(e, markerRuche) {
	const req = new XMLHttpRequest();
	req.open('POST',
		urlruches + 'ruche/deplace/' + markerRuche.rucheid + '/' + e.latLng.lat() + '/' + e.latLng.lng(),
		true);
	req.setRequestHeader('x-csrf-token', _csrf_token);
	req.onload = function() {
		if (req.readyState === 4) {
			if (req.status === 200) {
				if (req.responseText !== "OK") {
					alert(req.responseText);
				} else {
					parcours.setMap(null);
					parcoursRedraw();
				}
			}
		}
	};
	req.send(null);
}

function newParcours() {
	// https://developers.google.com/maps/documentation/javascript/shapes?hl=fr#polylines
	const coordsLineString = [];
	for (const rucheP of rucheParcours) {
		const coords = { lat: rucheP.latitude, lng: rucheP.longitude };
		coordsLineString.push(coords);
	}
	parcours = new google.maps.Polyline({
		path: coordsLineString,
		geodesic: true,
		strokeColor: '#FF0000',
		strokeOpacity: 1.0,
		strokeWeight: 2
	});
	parcours.setMap(map);
}

$(document).ready(function() {
	$('#dragMarker').change(function() {
		for (const markersR of markersRuche) {
			markersR.setDraggable(!this.checked);
		}
	});
	$('.liste').click(function(e) {
		window.location = urlruches + 'ruche/liste/' + rucher.id +
			'?parcours=' + encodeURIComponent(JSON.stringify(rucheParcours)) +
			'&plus=' + (e.target.id != 'liste');
	});
	$('#export-gpx').click(function() {
		exportGpx();
	});
	$('#export-kml').click(function() {
		exportKml();
	});
	$('#parcours-redraw').click(function() {
		parcoursRedraw(true);
	});
	$("#searchtext").keyup(function(event) {
		if (event.keyCode === 13) {
			let searchtext = $("#searchtext").val().toUpperCase();
			for (const marker of markersRuche) {
				if ((marker.label.text.toUpperCase() === searchtext) ||
					(marker.label.text.toUpperCase() === '*' + searchtext) ||
					(marker.essaimnom.toUpperCase() === searchtext)) {
					google.maps.event.trigger(marker, 'click');
				}
			}
		}
	});
	$('#cercles').attr("title", distButinage + ' ' + rayonsButinage.join(', ') + 'm');
	$('#cercles').click(function() {
		const visi = !circlesButinage[0].getVisible();
		for (const c of circlesButinage) {
			c.setVisible(visi);
		}
		const bgCol = visi ? 'rgba(0, 0, 255, 0.2)' : '';
		$('#cercles').css({ 'background-color': bgCol });
	});

});
/* globals google, pasderuchetxt,
	ruchers, nomRuches,	urlruches, ruchertxt, lesRuchestxt,	ruchersmapzoom, _csrf_token
 */
/* exported initMap */
'use strict';
const markersRucher = [];

function initMap() {
	let mapcenterlong;
	let mapcenterlat;
	for (const rucher of ruchers) {
		if (rucher.depot === true) {
			mapcenterlong = rucher.longitude;
			mapcenterlat = rucher.latitude;
		}
	}
	const map = new google.maps.Map(document.getElementById('map'), {
		center: {
			lat: mapcenterlat,
			lng: mapcenterlong
		},
		zoom: parseInt(ruchersmapzoom)
	});
	const infowindow = new google.maps.InfoWindow();
	for (let i = 0; i < ruchers.length; i++) {
		const markerRucher = new google.maps.Marker({
			position: {
				lat: ruchers[i].latitude,
				lng: ruchers[i].longitude
			},
			map: map,
			label: ruchers[i].nom,
			rucherid: ruchers[i].id,
			ruchesnom: nomRuches[i]
		});
		google.maps.event.addListener(markerRucher, 'dragend', function(event) {
			sauveRucherPosition(event, markerRucher);
		});
		google.maps.event.addListener(markerRucher, 'click', function() {
			infowindow.setContent(
				'<a href="' + urlruches + 'rucher/Gg/' + markerRucher.rucherid + '">' +
				ruchertxt + ' ' + markerRucher.label + '</a><br/>' +
				((markerRucher.ruchesnom === '') ? pasderuchetxt :
					lesRuchestxt + ' ' + markerRucher.ruchesnom));
			infowindow.open(map, markerRucher);
		});
		markersRucher.push(markerRucher);
	}
	function sauveRucherPosition(e, markerRucher) {
		const req = new XMLHttpRequest();
		req.open('POST',
			urlruches + 'rucher/deplace/' + markerRucher.rucherid + '/' + e.latLng.lat() + '/' + e.latLng.lng(),
			true);
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.send(null);
	}
}

document.addEventListener('DOMContentLoaded', () => {
	$('#dragMarker').change(function() {
		for (const marker of markersRucher) {
			marker.setDraggable(!this.checked);
		}
	});
	$('#searchtext').keyup(function(event) {
		if (event.keyCode === 13) {
			const searchtext = $('#searchtext').val().toUpperCase();
			for (const marker of markersRucher) {
				if (marker.label.toUpperCase().includes(searchtext)) {
					google.maps.event.trigger(marker, 'click');
				}
			}
		}
	});
});

/* globals
   ol, overlay, drawControl, drawLayer, iconFeatureEntree,
   rucheParcours, distParcours, ruches, rucher, nomHausses,
   ruchestxt, distancedeparcourstxt, pasDeGeoLoc, recherchePos, locImpossible  */
/* exported geoloc, exportGpx, exportKml */
'use strict';

function geoloc() {
	// Note: In Firefox 55 and higher, only HTTPS pages will be able to request location.
	// about:config  If the geo.enabled preference is bolded and "user set" to false, 
	// double-click it to restore the default value of true
	// https://developer.mozilla.org/fr/docs/Web/API/Geolocation_API
	const options = {
		enableHighAccuracy: true,
		timeout: 60000,
		maximumAge: 0,
	};
	function success(position) {
		const alti = position.coords.altitude === null ? '---' : position.coords.altitude.toFixed(2);
		const altiAcc = position.coords.altitudeAccuracy === null ? '---' : position.coords.altitudeAccuracy.toFixed(2) + 'm';
		document.getElementById('popup-content').innerHTML = 
		`Latitude ${position.coords.latitude.toFixed(4)}<br/>Longitude ${position.coords.longitude.toFixed(4)}<br/>
		Précision ${position.coords.accuracy.toFixed(2)}m<br/>Altitude ${alti}<br/>
		PrécisionAlt ${altiAcc}<br/>`;
		const coords = [];
		coords.push(position.coords.longitude);
		coords.push(position.coords.latitude);
		const iconFeature = new ol.Feature({
			type: 'Point',
			geometry: new ol.geom.Point(ol.proj.fromLonLat(coords)),
		});
		overlay.setPosition(iconFeature.getGeometry().getCoordinates());
		iconFeature.setStyle(
			new ol.style.Style({
				image: new ol.style.Icon(drawControl._getIconStyleOptions(drawControl.options.markersList[0]))
			})
		);
		drawControl._updateMeasure(iconFeature, 'Point');
		drawLayer.getSource().addFeature(iconFeature);
	}

	function error() {
		document.getElementById('popup-content').innerHTML = pasDeGeoLoc;
	}

	if ('geolocation' in navigator) {
		document.getElementById('popup-content').innerHTML = recherchePos;
		overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
		navigator.geolocation.getCurrentPosition(success, error, options);
	} else {
		alert(locImpossible);
	}
}

function exportGpx() {
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	let gpxcontent = `<?xml version="1.0" encoding="UTF-8" standalone="no" ?>
		<gpx xmlns="http://www.topografix.com/GPX/1/1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		  xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd"
		  version="1.1" creator="ooioo/ruches">
		<metadata>
		  <link href="https://gitlab.com/ooioo/ruches"><text>gitlab ruches</text></link>
		  <time>${new Date().toISOString()}</time>
		</metadata>`;
	for (const rucheP of rucheParcours) {
		gpxcontent += '<wpt lat="' + rucheP.latitude +
			'" lon="' + rucheP.longitude + '"><desc><![CDATA[';
		if (rucheP.id === 0) {
			gpxcontent += 'Entrée';
		} else {
			const rucheidx = ruches.findIndex(x => x.id === rucheP.id);
			const ruche = ruches[rucheidx];
			gpxcontent += 'Ruche ' + ruche.nom + '. ' +
				((ruche.essaim === null) ? "Pas d'essaim" : ('Essaim ' + ruche.essaim.nom)) + '. ' +
				((nomHausses[rucheidx] === '') ? 'Pas de hausse' : ('Hausse(s) ' + nomHausses[rucheidx]));
		}
		gpxcontent += ']]></desc></wpt>';
	}
	gpxcontent +=
		'<trk><name><![CDATA[Rucher ' + rucher.nom + ']]></name><desc>' +
		ruches.length + ' ' + ruchestxt + ', ' + distancedeparcourstxt + ' ' +
		distParcours.toLocaleString(lang, digits2) + 'm</desc><trkseg>';
	for (const rucheP of rucheParcours) {
		gpxcontent += '<trkpt lat="' + rucheP.latitude +
			'" lon="' + rucheP.longitude + '"></trkpt>';
	}
	gpxcontent += '</trkseg></trk></gpx>';
	const link = document.getElementById('gpx-download');
	const file = new Blob([gpxcontent], { type: 'application/gpx+xml;charset=utf-8' });
	link.href = URL.createObjectURL(file);
	link.download = rucher.nom + '.gpx';
	link.click();
	URL.revokeObjectURL(link.href);
}

function exportKml() {
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	const styleStrBegin = `<IconStyle><Icon>
		<href>http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png</href>
		</Icon><scale>1</scale><color>`;
	const styleStrEnd = `</color></IconStyle>
		<BalloonStyle><text><![CDATA[$[description]]]></text></BalloonStyle></Style>`;
	let kmlcontent = '<?xml version="1.0" encoding="UTF-8"?>' +
		'<kml xmlns="http://www.opengis.net/kml/2.2"><Document>' +
		'<Style id="iconblue">' + styleStrBegin + 'ffff0000' + styleStrEnd +
		'<Style id="iconwhite">' + styleStrBegin + 'ffffffff' + styleStrEnd +
		'<Style id="iconyellow">' + styleStrBegin + 'ff00ffff' + styleStrEnd +
		'<Style id="iconred">' + styleStrBegin + 'ff0000ff' + styleStrEnd +
		'<Style id="icongreen">' + styleStrBegin + 'ff00ff00' + styleStrEnd +
		'<Style id="iconorange">' + styleStrBegin + 'ff0080ff' + styleStrEnd +
		// couleur iconnull à vérifier
		'<Style id="iconnull">' + styleStrBegin + 'fffe00fe' + styleStrEnd;
	let nom;
	let description;
	let couleur;
	const coulicon = {
		BLUE: '#iconblue',
		WHITE: '#iconwhite',
		YELLOW: '#iconyellow',
		RED: '#iconred',
		GREEN: '#icongreen',
		ORANGE: '#iconorange'
	};
	for (let i = 1; i < rucheParcours.length; i++) { // i = 1 pour éviter deux icones Entrée
		kmlcontent += '<Placemark><open>0</open><styleUrl>#iconorange</styleUrl><name>';
		if (rucheParcours[i].id === 0) {
			nom = '<![CDATA[Entrée';
			description = '<![CDATA[Entrée : ' + ruches.length + ' ' + ruchestxt + '<br/>' + distancedeparcourstxt + ' ' +
				distParcours.toLocaleString(lang, digits2) + ' m';
			couleur = '#iconwhite';
		} else {
			const rucheidx = ruches.findIndex(x => x.id === rucheParcours[i].id);
			const ruche = ruches[rucheidx];
			nom = '<![CDATA[' + ruche.nom;
			description = '<![CDATA[Ruche ' + ruche.nom;
			if (ruche.essaim === null) {
				description += "<br/>Pas d'essaim" +
					((nomHausses[rucheidx] === '') ? '<br/>Pas de hausse' : ('<br/>Hausse(s) ' + nomHausses[rucheidx]));
				couleur = '#iconnull';
			} else {
				description += '<br/>Essaim ' + ruche.essaim.nom +
					((nomHausses[rucheidx] === '') ? '<br/>Pas de hausse' : ('<br/>Hausse(s) ' + nomHausses[rucheidx]));
				couleur = coulicon[ruche.essaim.reineCouleurMarquage];
			}
		}
		kmlcontent += nom + ']]></name><description>' + description +
			']]></description><styleUrl>' + couleur + '</styleUrl><Point>' +
			'<coordinates>' + rucheParcours[i].longitude +
			',' + rucheParcours[i].latitude +
			'</coordinates></Point></Placemark>';
	}
	kmlcontent += '<Style id="yellowLineGreenPoly"><LineStyle>' +
		'<color>7f00ffff</color><width>4</width></LineStyle></Style>' +
		'<Placemark><name>Parcours</name><description>' +
		ruches.length + ' ' + ruchestxt + ', ' +
		distancedeparcourstxt + ' ' + distParcours.toLocaleString(lang, digits2) + ' m' +
		'</description><styleUrl>#yellowLineGreenPoly</styleUrl><LineString><coordinates> ';
	for (const rucheP of rucheParcours) {
		kmlcontent += rucheP.longitude + ',' +
			rucheP.latitude + ',0 ';
	}
	kmlcontent += '</coordinates></LineString></Placemark></Document></kml>';
	const link = document.getElementById('kml-download');
	const file = new Blob([kmlcontent], { type: 'vnd.google-earth.kml+xml;charset=utf-8' });
	link.href = URL.createObjectURL(file);
	link.download = rucher.nom + '.kml';
	link.click();
	URL.revokeObjectURL(link.href);
}

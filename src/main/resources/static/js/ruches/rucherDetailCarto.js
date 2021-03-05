/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals
   rucheParcours, distParcours, ruches, rucher, nomHausses,
   ruchestxt, distancedeparcourstxt  */

function exportGpx() {
	let gpxcontent = '<?xml version="1.0" encoding="UTF-8" standalone="no" ?>' +
		'<gpx xmlns="http://www.topografix.com/GPX/1/1" ' +
		'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ' +
		'xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd" ' +
		'version="1.1" creator="ooioo/ruches">' +
		'<metadata><link href="https://gitlab.com/ooioo/ruches"><text>gitlab ruches</text></link>' +
		'<time>' + new Date().toISOString() + '</time>' +
		'</metadata>';
	for (const rucheP of rucheParcours) {
		gpxcontent += '<wpt lat="' + rucheP.latitude +
			'" lon="' + rucheP.longitude + '"><desc><![CDATA[';
		if (rucheP.id === 0) {
			gpxcontent += "Entrée";
		} else {
			let rucheidx = ruches.findIndex(x => x.id === rucheP.id);
			let ruche = ruches[rucheidx];
			gpxcontent += 'Ruche ' + ruche.nom + '. ' +
				((ruche.essaim == null) ? "Pas d'essaim" : ('Essaim ' + ruche.essaim.nom)) + '. ' +
				((nomHausses[rucheidx] == '') ? 'Pas de hausse' : ('Hausse(s) ' + nomHausses[rucheidx]));
		}
		gpxcontent += ']]></desc></wpt>';
	}
	gpxcontent +=
		'<trk><name><![CDATA[Rucher ' + rucher.nom + ']]></name><desc>' +
		ruches.length + ' ' + ruchestxt + ', ' + distancedeparcourstxt + ' ' +
		distParcours.toFixed(2) + 'm</desc><trkseg>';
	for (const rucheP of rucheParcours) {
		gpxcontent += '<trkpt lat="' + rucheP.latitude +
			'" lon="' + rucheP.longitude + '"></trkpt>';
	}
	gpxcontent += '</trkseg></trk></gpx>';
	let link = document.getElementById('gpx-download');
	const file = new Blob([gpxcontent], { type: 'application/gpx+xml;charset=utf-8' });
	link.href = URL.createObjectURL(file);
	link.download = rucher.nom + ".gpx";
	link.click();
	URL.revokeObjectURL(link.href);
}

function exportKml() {
	let styleStrBegin = '<IconStyle><Icon>' +
		'<href>http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png</href>' +
		'</Icon><scale>1</scale><color>';
	let styleStrEnd = '</color></IconStyle>' +
		'<BalloonStyle><text><![CDATA[$[description]]]></text></BalloonStyle>' +
		'</Style>';
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
	for (let i = 1; i < rucheParcours.length; i++) { // i = 1 pour éviter deux icones Entrée
		kmlcontent += '<Placemark><open>0</open><styleUrl>#iconorange</styleUrl><name>';
		if (rucheParcours[i].id === 0) {
			nom = '<![CDATA[Entrée';
			description = '<![CDATA[Entrée : ' + ruches.length + ' ' + ruchestxt + '<br/>' + distancedeparcourstxt + ' ' +
				distParcours.toFixed(2) + 'm';
			couleur = '#iconwhite';
		} else {
			let rucheidx = ruches.findIndex(x => x.id === rucheParcours[i].id);
			let ruche = ruches[rucheidx];
			nom = '<![CDATA[' + ruche.nom;
			description = '<![CDATA[Ruche ' + ruche.nom;
			if (ruche.essaim == null) {
				description += "<br/>Pas d'essaim" +
					((nomHausses[rucheidx] == '') ? '<br/>Pas de hausse' : ('<br/>Hausse(s) ' + nomHausses[rucheidx]));
				couleur = '#iconnull';
			} else {
				description += '<br/>Essaim ' + ruche.essaim.nom +
					((nomHausses[rucheidx] == '') ? '<br/>Pas de hausse' : ('<br/>Hausse(s) ' + nomHausses[rucheidx]));
				switch (ruche.essaim.reineCouleurMarquage) {
					case 'BLUE':
						couleur = '#iconblue';
						break;
					case 'WHITE':
						couleur = '#iconwhite';
						break;
					case 'YELLOW':
						couleur = '#iconyellow';
						break;
					case 'RED':
						couleur = '#iconred';
						break;
					case 'GREEN':
						couleur = '#icongreen';
						break;
					case 'ORANGE':
						couleur = '#iconorange';
						break;
				}
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
		distancedeparcourstxt + ' ' + distParcours.toFixed(2) + 'm' +
		'</description><styleUrl>#yellowLineGreenPoly</styleUrl><LineString><coordinates> ';
	for (const rucheP of rucheParcours) {
		kmlcontent += rucheP.longitude + ',' +
			rucheP.latitude + ',0 ';
	}
	kmlcontent += '</coordinates></LineString></Placemark></Document></kml>';
	let link = document.getElementById('kml-download');
	const file = new Blob([kmlcontent], { type: 'vnd.google-earth.kml+xml;charset=utf-8' });
	link.href = URL.createObjectURL(file);
	link.download = rucher.nom + ".kml";
	link.click();
	URL.revokeObjectURL(link.href);
}

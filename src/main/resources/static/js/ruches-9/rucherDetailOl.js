/* globals ol, domtoimage, jsPDF, exportGpx, exportKml,
   rucheParcours:writable, distParcours:writable, longitudeCentre, latitudeCentre, rayonsButinage, cercles, distButinage, ruches, 
   rucher, nomHausses, rapprochertxt, pleinecran, lesRuches, couchemarqueursruches, essaimtxt, pasdessaimtxt, 
   ruchetxt, lesHausses, pasdehaussetxt, parcourstxt, ignCarteLiscense,
   parcoursoptimumtxt, ruchestxt, distancedeparcourstxt, entreetxt, ruchesurl, _csrf_token, dessinEnregistretxt,
   distRuchesOk, distMaxRuche, geoloc, bootstrap
   */
/* exported rucherDetail jsPDF */
'use strict';

let iconFeatureEntree;
let drawLayer;
let drawControl;
let overlay;
function rucherDetail(ign) {
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	const agriAnnee = '2021';
	const agriLegend = 'https://www.geoportail.gouv.fr/depot/layers/LANDUSE.AGRICULTURE' +
		agriAnnee + '/legendes/LANDUSE.AGRICULTURE' + agriAnnee + '-legend.png';
	const agriLayer = 'LANDUSE.AGRICULTURE.LATEST';
	const agriDescription =
		'Registre parcellaire graphique : zones de culture déclarées par les exploitants en ' +
		agriAnnee;
	const agriTitle = 'Registre parcellaire graphique';
	function rTerreLat(latitude) {
		const a = 6378137.0;
		const b = 6356752.3142;
		// https://en.m.wikipedia.org/wiki/Earth_radius#Geocentric_radius
		const fi = Math.PI * latitude / 180.0;
		const d1 = a * Math.cos(fi);
		const d2 = b * Math.sin(fi);
		const n1 = a * d1;
		const n2 = b * d2;
		return Math.sqrt((n1 * n1 + n2 * n2) / (d1 * d1 + d2 * d2));
	}
	function distanceTerre(diamTerre, lat2, lat1, lon2, lon1) {
		// ou utiliser ol.sphere.distance
		const sinDiffLat = Math.sin(Math.PI * (lat2 - lat1) / 360.0);
		const sinDiffLon = Math.sin(Math.PI * (lon2 - lon1) / 360.0);
		return diamTerre * Math.asin(Math.sqrt(sinDiffLat * sinDiffLat +
			Math.cos(Math.PI * lat1 / 180.0) *
			Math.cos(Math.PI * lat2 / 180.0) * sinDiffLon * sinDiffLon
		));
	}
	document.getElementsByClassName('rapproche')[0].addEventListener('click', (event) => {
		const longRucher = rucher.longitude;
		const latRucher = rucher.latitude;
		const diametreTerre = (isNaN(rucher.altitude) ? 0 : rucher.altitude) +
			2 * rTerreLat(latRucher);
		const rucheRapp = [];
		for (let i = 0; i < ruches.length; i += 1) {
			if (distanceTerre(diametreTerre, latRucher, ruches[i].latitude, longRucher,
				ruches[i].longitude) > distMaxRuche) {
				rucheRapp.push(ruches[i].nom);
			}
		}
		if (rucheRapp.length > 0) {
			if (confirm(rapprochertxt + ' (' + rucheRapp.join() + ') ?')) {
				return;
			}
		} else {
			alert(distRuchesOk);
		}
		event.preventDefault();
	});
	new bootstrap.Popover(document.getElementsByClassName('bi-question-lg')[0], {
		html: true
	});
	const markerRuches = [];
	const closer = document.getElementById('popup-closer');
	overlay = new ol.Overlay({
		element: document.getElementById('popup'),
		autoPan: true,
		autoPanAnimation: {
			duration: 250
		}
	});
	closer.onclick = function() {
		overlay.setPosition(undefined);
		closer.blur();
		return false;
	};
	for (let i = 0; i < ruches.length; i += 1) {
		const coordsMarker = [];
		coordsMarker.push(ruches[i].longitude);
		coordsMarker.push(ruches[i].latitude);
		const iconFeature = new ol.Feature({
			geometry: new ol.geom.Point(ol.proj.fromLonLat(coordsMarker)),
			idx: i
		});
		iconFeature.setStyle(
			new ol.style.Style({
				image: new ol.style.Circle({
					radius: 12,
					fill: new ol.style.Fill({
						color: (ruches[i].essaim === null) ? '#FE00FE' : ruches[i].essaim.reineCouleurMarquage
					})
				}),
				text: new ol.style.Text({
					text: (((ruches[i].essaim !== null) && ruches[i].essaim.reineMarquee) ? '*' : '') + ruches[i].nom,
					font: '14px sans-serif'
				})
			})
		);
		markerRuches.push(iconFeature);
	}
	const coordsEntree = [];
	coordsEntree.push(rucher.longitude);
	coordsEntree.push(rucher.latitude);
	// const
	iconFeatureEntree = new ol.Feature({
		idx: -1,
		geometry: new ol.geom.Point(ol.proj.fromLonLat(coordsEntree))
	});
	iconFeatureEntree.setStyle(
		new ol.style.Style({
			image: new ol.style.Circle({
				radius: 12,
				fill: new ol.style.Fill({
					color: '#FFFFFF'
				})
			}),
			text: new ol.style.Text({
				text: entreetxt
			})
		})
	);
	markerRuches.push(iconFeatureEntree);
	let vectorLineLayer = newVectorLineLayer();
	const mapcenter = [];
	mapcenter.push(rucher.longitude);
	mapcenter.push(rucher.latitude);
	const vectorLayer = new ol.layer.Vector({
		source: new ol.source.Vector({
			features: markerRuches
		})
	});
	const coordsCentre = [];
	coordsCentre.push(longitudeCentre);
	coordsCentre.push(latitudeCentre);
	const olProjCentre = ol.proj.fromLonLat(coordsCentre);
	const sourceCercles = new ol.source.Vector();
	const resol = ol.proj.getPointResolution('EPSG:3857', 1, ol.proj.fromLonLat(coordsCentre));
	for (const r of rayonsButinage) {
		sourceCercles.addFeature(new ol.Feature(new ol.geom.Circle(olProjCentre, r / resol)));
	}
	const styleCercles = new ol.style.Style({
		stroke: new ol.style.Stroke({
			color: 'blue',
			width: 1
		}),
		fill: new ol.style.Fill({
			color: 'rgba(0, 0, 255, 0.02)'
		})
	});
	const cerclesLayer = new ol.layer.Vector({
		visible: false,
		source: sourceCercles,
		style: styleCercles
	});
	cerclesLayer.on('change:visible', function() {
		const view = map.getView();
		if (this.get('visible') && view.getZoom() > 16) {
			view.setZoom(14);
		}
	});
	const select = new ol.interaction.Select({
		layers: [vectorLayer],
		toggleCondition: ol.events.condition.never,
		style: false
	});
	select.setActive(
		!document.getElementById('dragMarker').checked
	);
	select.on('select', function(e) {
		e.selected.forEach(function(feature) {
			const style = feature.getStyle();
			const txt = style.getText().getText();
			style.getText().setText('[' + txt + ']'); feature.setStyle(style);
		});
		e.deselected.forEach(function(feature) {
			const style = feature.getStyle();
			const txt = style.getText().getText();
			style.getText().setText(txt.substring(1, txt.length - 1)); feature.setStyle(style);
		});
	});
	const translate = new ol.interaction.Translate({
		features: select.getFeatures(),
		layers: [vectorLayer]
	});
	translate.setActive(
		!document.getElementById('dragMarker').checked
	);
	// Utiliser KMLExtended pour sauver et lire les textes	
	/// const formatKML = new ol.format.KMLExtended({});
	// Ne fonctionne pas
	const formatKML = new ol.format.KML({});
	const dessinsFeatures = (rucher.dessin === null) ? new ol.Collection() : new ol.Collection(formatKML.readFeatures(rucher.dessin));
	drawLayer = new ol.layer.Vector({
		source: new ol.source.Vector({
			features: dessinsFeatures
		})
	});
	const map = new ol.Map({
		interactions: ol.interaction.defaults({ doubleClickZoom: false }).extend([select, translate]),
		controls: ol.control.defaults({
			attribution: false
		}),
		target: 'map',
		layers: [
			vectorLayer,
			vectorLineLayer,
			cerclesLayer,
			drawLayer
		],
		overlays: [overlay],
		view: new ol.View({
			center: ol.proj.fromLonLat(mapcenter),
			zoom: 19
		})
	});
	drawControl = new ol.control.Drawing({
		layer: drawLayer,
		tools: {
			text: false
		},
		labels: {
			export: 'Enregistrer'
		},
	});
	map.addControl(drawControl);
	map.addControl(new ol.control.ZoomToExtent({
		extent: vectorLayer.getSource().getExtent().map((item, index) => item - [1, 1, -1, -1][index]),
		label: 'Z',
		tipLabel: 'Zoom Ruches',
	}));
	ol.control.Drawing.prototype.onExportFeatureClick = function() {
		const req = new XMLHttpRequest();
		req.open('POST', ruchesurl + 'rucher/sauveDessin/' + rucher.id, true);
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					if (req.responseText === 'OK') {
						alert(dessinEnregistretxt);
					} else {
						alert(req.responseText);
					}
				}
			}
		};
		req.send(formatKML.writeFeatures(drawLayer.getSource().getFeatures()));
	};
	const gfi = new ol.control.GetFeatureInfo({
		layers: [{
			obj: drawLayer,
			event: 'contextmenu'
		}],
		options: {
			hidden: true,
			auto: false
		}
	});
	map.addControl(gfi);
	const layerSwitcher = new ol.control.LayerSwitcher({
		layers: [{
			layer: vectorLayer,
			config: {
				title: lesRuches,
				description: couchemarqueursruches
			}
		},
		{
			layer: drawLayer,
			config: {
				title: 'Dessin',
				description: 'Dessin manuels'
			}
		},
		{
			layer: vectorLineLayer,
			config: {
				title: parcourstxt,
				description: parcoursoptimumtxt
			}
		},
		{
			layer: cerclesLayer,
			config: {
				title: cercles,
				description: distButinage + ' ' + rayonsButinage.join(', ') + ' m'
			}
		}]
	});
	const layersMap = map.getLayers();
	if (ign) {
		const olAgriLayer = new ol.layer.GeoportalWMTS({
			layer: agriLayer,
			olParams: {
				visible: false
			}
		});
		layersMap.insertAt(0, olAgriLayer);
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: 'CADASTRALPARCELS.PARCELS',
			olParams: {
				visible: false
			}
		}));
		if (ignCarteLiscense) {
			layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
				layer: 'GEOGRAPHICALGRIDSYSTEMS.MAPS'
			}));
		}
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: 'GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2',
			olParams: {
				visible: !ignCarteLiscense
			}
		}));
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: 'ORTHOIMAGERY.ORTHOPHOTOS'
		}));
		map.addControl(new ol.control.ElevationPath());
		map.addControl(layerSwitcher);
		layerSwitcher.removeLayer(olAgriLayer);
		layerSwitcher.addLayer(olAgriLayer, {
			title: agriTitle,
			description: agriDescription,
			legends: [{ url: agriLegend }]
		});
		map.addControl(new ol.control.GeoportalMousePosition({
			collapsed: true
		}));
	} else {
		const osmLayer = new ol.layer.Tile({
			source: new ol.source.OSM()
		});
		layersMap.insertAt(0, osmLayer);


		// https://www.ngi.be/website/fr/offre/geodonnees-numeriques/cartoweb-be-2/
		// https://cartoweb.wmts.ngi.be/1.0.0/WMTSCapabilities.xml
		// Belgique Wallonie cartoweb.be couche topo
		const belgiumExtent = [279569.630, 6352288.757, 731563.107, 6711221.861];
		const beLayerURL = 'https://cartoweb.wmts.ngi.be/1.0.0/topo/default/3857/{z}/{y}/{x}.png';
		const beLayer = new ol.layer.Tile({
			extent: belgiumExtent,
			source: new ol.source.XYZ({
				url: beLayerURL
			})
		});
		layersMap.insertAt(0, beLayer);
		// https://geoservices.wallonie.be/INSPIRE/WMS/OI/MapServer/WMSServer?request=GetCapabilities&service=WMS
		// https://geoportail.wallonie.be/catalogue/949d31b0-4994-488d-a454-3a10232e8785.html
		// https://metawal.wallonie.be/geonetwork/srv/fre/catalog.search#/metadata/949d31b0-4994-488d-a454-3a10232e8785
		// Belgique Wallonie SPW couche ortho (1)
		const beOrthoURL = 'https://geoservices.wallonie.be/INSPIRE/WMS/OI/MapServer/WmsServer?';
		const beOrthoLayer = new ol.layer.Tile({
			extent: belgiumExtent,
			source: new ol.source.TileWMS({
				url: beOrthoURL,
				params: {
					layers: '1'
				}
			})
		});
		layersMap.insertAt(0, beOrthoLayer);
		map.addControl(layerSwitcher);
		layerSwitcher.removeLayer(osmLayer);
		layerSwitcher.addLayer(osmLayer, {
			title: 'OpenStreetMap',
			description: 'Carte OpenStreetMap'
		});
		// Belgique
		const beLegendURL = 'https://cartoweb.wmts.ngi.be/legend/topo/default/legende_fr.png';
		layerSwitcher.removeLayer(beLayer);
		layerSwitcher.addLayer(beLayer, {
			title: 'Belgique',
			description: 'Carte Belgique',
			legends: [{ url: beLegendURL }]
		});
		layerSwitcher.removeLayer(beOrthoLayer);
		layerSwitcher.addLayer(beOrthoLayer, {
			title: 'Belgique OrhtoPhotos',
			description: 'Ortho Belgique'
		});
	}
	map.addControl(new ol.control.MeasureLength());
	map.addControl(new ol.control.MeasureArea());
	map.addControl(new ol.control.MeasureAzimuth());
	map.addControl(new ol.control.FullScreen({
		tipLabel: pleinecran
	}));
	document.getElementsByClassName('ol-full-screen')[0].style.left = '43px';
	document.getElementsByClassName('ol-full-screen')[0].style.right = 'unset';
	const selectDoubleClick = new ol.interaction.Select({
		condition: ol.events.condition.doubleClick,
		layers: [vectorLayer]
	});
	map.addInteraction(selectDoubleClick);
	selectDoubleClick.on('select', function(e) {
		const feature = e.target.getFeatures().getArray()[0];
		const idx = feature.get('idx');
		if (idx === -1) {
			document.getElementById('popup-content').innerHTML =
				ruches.length + ' ' + ruchestxt + '<br/>' + distancedeparcourstxt +
				' ' + distParcours.toLocaleString(lang, digits2) + ' m';
		} else {
			const essnom = (ruches[idx].essaim === null) ? '' : ruches[idx].essaim.nom;
			const essid = (ruches[idx].essaim === null) ? '' : ruches[idx].essaim.id;
			document.getElementById('popup-content').innerHTML =
				'<a href="' + ruchesurl + 'ruche/' + ruches[idx].id + '">' +
				ruchetxt + ' ' + ruches[idx].nom +
				'</a><br/>' +
				((essnom === '') ? pasdessaimtxt :
					'<a href="' + ruchesurl + 'essaim/' + essid + '">' +
					essaimtxt + ' ' + essnom + '</a>') +
				'<br/>' +
				((nomHausses[idx] === '') ? pasdehaussetxt :
					lesHausses + ' ' + nomHausses[idx]);
		}
		overlay.setPosition(feature.getGeometry().getCoordinates());
		selectDoubleClick.getFeatures().clear();
	});
	translate.on('translateend', function(evt) {
		// WGS 84 / Pseudo-Mercator - EPSG:3857 : projection
		// EPSG:4326 - World Geodetic System 1984, used in GPS : lat lon
		const idx = evt.features.getArray()[0].get('idx');
		const coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
		const req = new XMLHttpRequest();
		if (idx === -1) {
			req.open('POST', ruchesurl + 'rucher/deplace/' + rucher.id +
				'/' + coord[1] + '/' + coord[0], true);
			rucher.longitude = coord[0];
			rucher.latitude = coord[1];
		} else {
			req.open('POST', ruchesurl + 'ruche/deplace/' + ruches[idx].id +
				'/' + coord[1] + '/' + coord[0], true);
			ruches[idx].longitude = coord[0];
			ruches[idx].latitude = coord[1];
		}
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					if (req.responseText === 'OK') {
						parcoursRedraw();
					} else {
						alert(req.responseText);
					}
				}
			}
		};
		req.onerror = function() {
			alert(req.statusText);
		};
		req.send(null);
	});
	document.getElementById('searchtext').addEventListener('keyup', (event) => {
		if (event.code === 'Enter') {
			const searchtext = document.getElementById('searchtext').value.
				trim().toUpperCase();
			for (const marker of markerRuches) {
				if ((marker.getStyle().getText().getText().toUpperCase() === searchtext) ||
					(marker.getStyle().getText().getText().toUpperCase() === '*' + searchtext) ||
					(Object.prototype.hasOwnProperty.call(marker, 'essaimnom') &&
						(marker.get('essaimnom').toUpperCase() === searchtext))) {
					selectDoubleClick.getFeatures().clear();
					selectDoubleClick.getFeatures().push(marker);
					selectDoubleClick.dispatchEvent({
						type: 'select',
						selected: [marker],
						deselected: []
					});
					break;
				}
			}
		}
	});
	document.querySelectorAll('.liste').forEach(item =>
		item.addEventListener('click', (event) =>
			window.location = ruchesurl + 'ruche/liste/' + rucher.id +
			'?parcours=' + encodeURIComponent(JSON.stringify(rucheParcours.map(rp => rp.id))) +
			'&plus=' + (event.target.id !== 'liste')));
	document.getElementById('export-gpx').addEventListener('click', () => {
		exportGpx();
	});
	document.getElementById('export-kml').addEventListener('click', () => {
		exportKml();
	});
	document.getElementById('geoloc').addEventListener('click', () => {
		geoloc();
	});
	document.getElementById('parcours-redraw').addEventListener('click', () => {
		document.getElementById('popup-content').innerHTML = 'Calcul en cours...';
		overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
		parcoursRedraw(true);
	});
	document.getElementById('dragMarker').addEventListener('change', (event) => {
		select.setActive(!event.target.checked);
		translate.setActive(!event.target.checked);
	});
	if (!ign) {
		document.getElementById('export-png').addEventListener('click', function() {
			map.once('rendercomplete', function() {
				domtoimage.toPng(map.getViewport().querySelectorAll('.ol-layer canvas, canvas.ol-layer')[0])
					.then(function(dataURL) {
						const link = document.getElementById('image-download');
						link.href = dataURL;
						link.click();
					});
			});
			map.renderSync();
		});
		const { jsPDF } = window.jspdf;
		let traitePdf = false;
		document.getElementById('export-pdf').addEventListener('click', function() {
			if (traitePdf) { return; }
			traitePdf = true;
			document.body.style.cursor = 'progress';
			const format = document.getElementById('format').value;
			const resolution = document.getElementById('resolution').value;
			const dims = {
				a3: [420, 297],
				a4: [297, 210]
			};
			const dim = dims[format];
			const width = Math.round(dim[0] * resolution / 25.4);
			const height = Math.round(dim[1] * resolution / 25.4);
			const size = map.getSize();
			const viewResolution = map.getView().getResolution();
			map.once('rendercomplete', function() {
				const exportOptions = {};
				exportOptions.width = width;
				exportOptions.height = height;
				domtoimage.toJpeg(map.getTargetElement().getElementsByClassName('ol-layers')[0], exportOptions)
					.then(function(dataURL) {
						const pdf = new jsPDF('landscape', undefined, format);
						pdf.addImage(dataURL, 'JPEG', 0, 0, dim[0], dim[1]);
						pdf.save(rucher.nom + '.pdf');
						// Reset original map size
						map.setSize(size);
						map.getView().setResolution(viewResolution);
						traitePdf = false;
						document.body.style.cursor = 'auto';
					});
			});
			const printSize = [width, height];
			map.setSize(printSize);
			const scaling = Math.min(width / size[0], height / size[1]);
			map.getView().setResolution(viewResolution / scaling);
		});
	}
	function newVectorLineLayer() {
		const coordsLineString = [];
		for (const rucheP of rucheParcours) {
			const coords = [rucheP.longitude, rucheP.latitude];
			coordsLineString.push(ol.proj.fromLonLat(coords));
		}
		const lineString = new ol.geom.LineString(coordsLineString);
		const lineFeature = new ol.Feature({
			geometry: lineString
		});
		const lineRuches = [];
		lineRuches.push(lineFeature);
		const lineSource = new ol.source.Vector({
			features: lineRuches
		});
		return new ol.layer.Vector({
			source: lineSource
		});
	}
	function parcoursRedraw(redraw = false) {
		const req2 = new XMLHttpRequest();
		req2.open('GET', ruchesurl + 'rucher/parcours/' + rucher.id + '/' + redraw, true);
		req2.onload = function() {
			if (req2.readyState === 4) {
				if (req2.status === 200) {
					const response = JSON.parse(req2.responseText);
					// distParcours et rucheParcours var globales
					if (redraw && (response.distParcours + 0.1 > distParcours)) {
						document.getElementById('popup-content').innerHTML =
							"Pas d'amélioration<br/>" +
							distancedeparcourstxt + ' ' + distParcours.toLocaleString(lang, digits2) + ' m';
						overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
						return;
					}
					const dist = distParcours;
					distParcours = response.distParcours;
					rucheParcours = response.rucheParcours;
					const visible = vectorLineLayer.getVisible();
					map.removeLayer(vectorLineLayer);
					layerSwitcher.removeLayer(vectorLineLayer);
					vectorLineLayer = newVectorLineLayer();
					map.addLayer(vectorLineLayer);
					vectorLineLayer.setVisible(visible);
					layerSwitcher.addLayer(vectorLineLayer, {
						title: parcourstxt,
						description: parcoursoptimumtxt
					});

					if (redraw) {
						document.getElementById('popup-content').innerHTML =
							'La distance est diminuée de ' + (dist - distParcours).toLocaleString(lang, digits2) +
							'm<br/>' + distancedeparcourstxt +
							' ' + distParcours.toLocaleString(lang, digits2) + ' m';
						overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
					}
				}
			}
		};
		req2.send();
	}
}
/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals ol, domtoimage, jsPDF, exportGpx, exportKml,
   rucheParcours, distParcours, longitudeCentre, latitudeCentre, rayonsButinage, cercles, distButinage, ruches, 
   rucher, nomHausses, rapprochertxt, pleinecran, lesRuches, couchemarqueursruches, essaimtxt, pasdessaimtxt, 
   ruchetxt, lesHausses, pasdehaussetxt, parcourstxt,
   parcoursoptimumtxt, ruchestxt, distancedeparcourstxt, entreetxt, ruchesurl, _csrf_token, dessinEnregistretxt */
// "use strict"

function rucherDetail(ign) {
	const lang = navigator.language;
	const digits2 = { maximumFractionDigits: 2 };
	$('.rapproche').on('click', function() {
		return confirm(rapprochertxt);
	});
	$('.bi-question-lg').popover({
		html: true
	});
	$('.popover-dismiss').popover({
		trigger: 'focus'
	});
	const markerRuches = [];
	const closer = document.getElementById('popup-closer');
	const overlay = new ol.Overlay({
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
			type: 'geoMarker',
			geometry: new ol.geom.Point(ol.proj.fromLonLat(coordsMarker)),
			rucheid: ruches[i].id,
			ruchenom: ruches[i].nom,
			essaimnom: (ruches[i].essaim == null) ? '' : ruches[i].essaim.nom,
			essaimid: (ruches[i].essaim == null) ? '' : ruches[i].essaim.id,
			haussesnom: nomHausses[i]
		});
		iconFeature.setStyle(
			new ol.style.Style({
				image: new ol.style.Circle({
					radius: 12,
					fill: new ol.style.Fill({
						color: (ruches[i].essaim == null) ? '#FE00FE' : ruches[i].essaim.reineCouleurMarquage
					})
				}),
				text: new ol.style.Text({
					text: (((ruches[i].essaim != null) && ruches[i].essaim.reineMarquee) ? '*' : '') + ruches[i].nom,
					font: '14px sans-serif'
				})
			})
		);
		markerRuches.push(iconFeature);
	}
	const coordsEntree = [];
	coordsEntree.push(rucher.longitude);
	coordsEntree.push(rucher.latitude);
	const iconFeatureEntree = new ol.Feature({
		type: 'geoMarker',
		rucheid: 'entree',
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
		let view = map.getView();
		if (this.get('visible') && view.getZoom() > 16) {
			view.setZoom(14);
		}
	});
	const select = new ol.interaction.Select({
		layers: [vectorLayer],
		toggleCondition: ol.events.condition.never,
		style: false
	});
	select.on('select', function(e) {
		e.selected.forEach(function(feature) {
			let style = feature.getStyle(); let txt = style.getText().getText();
			style.getText().setText('[' + txt + ']'); feature.setStyle(style);
		});
		e.deselected.forEach(function(feature) {
			let style = feature.getStyle(); let txt = style.getText().getText();
			style.getText().setText(txt.substring(1, txt.length - 1)); feature.setStyle(style);
		});
	});
	const translate = new ol.interaction.Translate({
		features: select.getFeatures(),
		layers: [vectorLayer]
	});
	translate.setActive(!$('#dragMarker')[0].checked);

	// Utiliser KMLExtended pour sauver et lire les textes	
	/// const formatKML = new ol.format.KMLExtended({});
	// Ne fonctionne pas
	const formatKML = new ol.format.KML({});
	
	const dessinsFeatures = (rucher.dessin === null) ? new ol.Collection() : new ol.Collection(formatKML.readFeatures(rucher.dessin));
	const drawLayer = new ol.layer.Vector({
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
	const drawControl = new ol.control.Drawing({
		layer: drawLayer,
		tools: {
			text: false
		},
		labels: {
			export: 'Enregistrer'
		},
	});
	map.addControl(drawControl);
	ol.control.Drawing.prototype.onExportFeatureClick = function() {
		const req = new XMLHttpRequest();
		req.open('POST', ruchesurl + 'rucher/sauveDessin/' + rucher.id, true);
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					if (req.responseText !== "OK") {
						alert(req.responseText);
					} else {
						alert(dessinEnregistretxt);
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
				description: distButinage + ' ' + rayonsButinage.join(', ') + 'm'
			}
		}]
	});
	let layersMap = map.getLayers();
	if (ign) {
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: "CADASTRALPARCELS.PARCELS",
			olParams: {
				visible: false
			}
		}));
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: "GEOGRAPHICALGRIDSYSTEMS.MAPS.SCAN-EXPRESS.STANDARD"
		}));
		layersMap.insertAt(0, new ol.layer.GeoportalWMTS({
			layer: "ORTHOIMAGERY.ORTHOPHOTOS"
		}));
		map.addControl(new ol.control.ElevationPath());
		map.addControl(layerSwitcher);
		map.addControl(new ol.control.GeoportalMousePosition({
			collapsed: true,
			displayCoordinates: false
		}));
	} else {
		const osmLayer = new ol.layer.Tile({
			source: new ol.source.OSM()
		});
		layersMap.insertAt(0, osmLayer);
		map.addControl(layerSwitcher);
		layerSwitcher.removeLayer(osmLayer);
		layerSwitcher.addLayer(osmLayer, {
			title: 'OpenStreetMap',
			description: 'Carte OpenStreetMap'
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
		if (feature.get("rucheid") === 'entree') {
			document.getElementById('popup-content').innerHTML =
				ruches.length + ' ' + ruchestxt + '<br/>' + distancedeparcourstxt +
				' ' + distParcours.toLocaleString(lang, digits2) + 'm';
		} else {
			document.getElementById('popup-content').innerHTML =
				'<a href="' + ruchesurl + 'ruche/' + feature.get('rucheid') + '">' +
				ruchetxt + ' ' + feature.get("ruchenom") +
				'</a><br/>' +
				((feature.get("essaimnom") === '') ? pasdessaimtxt :
					'<a href="' + ruchesurl + 'essaim/' + feature.get('essaimid') + '">' +
					essaimtxt + ' ' + feature.get("essaimnom") + '</a>') +
				'<br/>' +
				((feature.get("haussesnom") === '') ? pasdehaussetxt :
					lesHausses + ' ' + feature.get("haussesnom"));
		}
		overlay.setPosition(feature.getGeometry().getCoordinates());
		selectDoubleClick.getFeatures().clear();
	});

	translate.on('translateend', function(evt) {
		const coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
		const req = new XMLHttpRequest();
		if (evt.features.getArray()[0].get("rucheid") === 'entree') {
			req.open('POST', ruchesurl + 'rucher/deplace/' + rucher.id +
				'/' + coord[1] + '/' + coord[0], true);
		} else {
			req.open('POST', ruchesurl + 'ruche/deplace/' + evt.features.getArray()[0].get("rucheid") +
				'/' + coord[1] + '/' + coord[0], true);
		}
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.onload = function() {
			if (req.readyState === 4) {
				if (req.status === 200) {
					if (req.responseText !== "OK") {
						alert(req.responseText);
					} else {
						parcoursRedraw();
					}
				}
			}
		};
		req.onerror = function() {
			alert(req.statusText);
		};
		req.send(null);
	});

	$("#searchtext").keyup(function(event) {
		if (event.keyCode === 13) {
			let searchtext = $("#searchtext").val().toUpperCase();
			for (const marker of markerRuches) {
				if ((marker.getStyle().getText().getText().toUpperCase() === searchtext) ||
					(marker.getStyle().getText().getText().toUpperCase() === '*' + searchtext) ||
					(marker.get("essaimnom").toUpperCase() === searchtext)) {
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
	$('.liste').click(function(e) {
		window.location = ruchesurl + 'ruche/liste/' + rucher.id +
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
		document.getElementById('popup-content').innerHTML = 'Calcul en cours...';
		overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
		parcoursRedraw(true);
	});

	$('#dragMarker').change(function() {
		translate.setActive(!this.checked);
	});

	if (!ign) {
		// export png
		$('#export-png').click(function() {
			map.once('rendercomplete', function() {
				domtoimage.toPng(map.getTargetElement().getElementsByClassName("ol-layers")[0])
					.then(function(dataURL) {
						let link = document.getElementById('image-download');
						link.href = dataURL;
						link.click();
					});
			});
			map.renderSync();
		});

		// export pdf
		const { jsPDF } = window.jspdf;
		const exportPdf = $('#export-pdf');
		exportPdf.click(function() {
			exportPdf.disabled = true;
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
				let exportOptions = {};
				exportOptions.width = width;
				exportOptions.height = height;
				domtoimage.toJpeg(map.getTargetElement().getElementsByClassName("ol-layers")[0], exportOptions)
					.then(function(dataURL) {
						let pdf = new jsPDF('landscape', undefined, format);
						pdf.addImage(dataURL, 'JPEG', 0, 0, dim[0], dim[1]);
						pdf.save('map.pdf');
						// Reset original map size
						map.setSize(size);
						map.getView().setResolution(viewResolution);
						exportPdf.disabled = false;
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
							distancedeparcourstxt + ' ' + distParcours.toLocaleString(lang, digits2) + 'm';
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
							' ' + distParcours.toLocaleString(lang, digits2) + 'm';
						overlay.setPosition(iconFeatureEntree.getGeometry().getCoordinates());
					}
				}
			}
		};
		req2.send();
	}

}
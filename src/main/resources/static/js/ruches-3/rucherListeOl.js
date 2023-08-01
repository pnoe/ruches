/* globals ol,
	ruchers, nomRuches,	recentertxt, lesRucherstxt,	lesRuchestxt, pasderuchetxt, 
	ruchertxt, couchemarqueursrucherstxt, ignCarteLiscense,
	pleinecrantxt, urlruches, _csrf_token
*/
/* exported rucherListeIgn */
'use strict';
function rucherListeIgn(ign) {
	const urlbrgm = 'https://geoservices.brgm.fr/geologie';
	const urlbrgmlegend = 'http://mapsref.brgm.fr/legendes/geoservices/Geologie1000_legende.jpg';
	const agriAnnee = '2021';
	const agriLegend = 'https://www.geoportail.gouv.fr/depot/layers/LANDUSE.AGRICULTURE' +
		agriAnnee + '/legendes/LANDUSE.AGRICULTURE' + agriAnnee + '-legend.png';
	const agriLayer = 'LANDUSE.AGRICULTURE.LATEST';
	const agriDescription =
		'Registre parcellaire graphique : zones de culture déclarées par les exploitants en '
		+ agriAnnee;
	const agriTitle = 'Registre parcellaire graphique';

	$('.recentre').on('click', function() {
		return confirm(recentertxt);
	});
	$('.bi-question-lg').popover({ html: true });
	$('.popover-dismiss').popover({
		trigger: 'focus'
	});
	const markerRuchers = [];
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
	const mapcenter = [];
	for (let i = 0; i < ruchers.length; i++) {
		const coordsMarker = [];
		coordsMarker.push(ruchers[i].longitude);
		coordsMarker.push(ruchers[i].latitude);
		const iconFeature = new ol.Feature({
			idx: i,
			geometry: new ol.geom.Point(ol.proj.fromLonLat(coordsMarker)),
		});
		iconFeature.setStyle(
			new ol.style.Style({
				image: new ol.style.Circle({
					radius: 12,
					fill: new ol.style.Fill({ color: '#FFFF00' })
				}),
				text: new ol.style.Text({
					text: ruchers[i].nom,
					font: '14px sans-serif'
				})
			})
		);
		markerRuchers.push(iconFeature);
		if (ruchers[i].depot === true) {
			mapcenter.push(ruchers[i].longitude);
			mapcenter.push(ruchers[i].latitude);
		}
	}
	const vectorLayer = new ol.layer.Vector({
		source: new ol.source.Vector({
			features: markerRuchers
		})
	});
	const brgm = new ol.layer.Tile({
		visible: false,
		source: new ol.source.TileWMS({
			url: urlbrgm,
			params: {
				layers: 'geologie',
				format: 'png',
				version: '1.3.0'
			}
		})
	});
	const select = new ol.interaction.Select({
		layers: [vectorLayer],
		toggleCondition: ol.events.condition.never,
		style: false
	});
	select.setActive(!$('#dragMarker')[0].checked);
	select.on('select', function(e) {
		e.selected.forEach(function(feature) {
			const style = feature.getStyle();
			const txt = style.getText().getText();
			style.getText().setText('[' + txt + ']');
			feature.setStyle(style);
		});
		e.deselected.forEach(function(feature) {
			const style = feature.getStyle();
			const txt = style.getText().getText();
			style.getText().setText(txt.substring(1, txt.length - 1));
			feature.setStyle(style);
		});
	});
	const translate = new ol.interaction.Translate({
		features: select.getFeatures(),
		layers: [vectorLayer]
	});
	translate.setActive(!$('#dragMarker')[0].checked);
	const map = new ol.Map({
		// ajout {doubleClickZoom: false}
		interactions: ol.interaction.defaults({ doubleClickZoom: false }).extend([select, translate]),
		controls: ol.control.defaults({
			attribution: false
		}),
		target: 'map',
		layers: [brgm, vectorLayer],
		overlays: [overlay],
		view: new ol.View({
			center: ol.proj.fromLonLat(mapcenter),
			zoom: 11
		})
	});
	const layerSwitcher = new ol.control.LayerSwitcher({
		layers: [
			{
				layer: vectorLayer,
				config: {
					title: lesRucherstxt,
					description: couchemarqueursrucherstxt
				}
			},
			{
				layer: brgm,
				config: {
					title: 'Géologie',
					description: 'Géologie BRGM',
					legends: [{ url: urlbrgmlegend }]
				}
			}
		]
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
		layersMap.insertAt(0, new ol.layer.GeoportalWMS({
			layer: 'LIMITES_ADMINISTRATIVES_EXPRESS.LATEST',
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
			layer: 'ORTHOIMAGERY.ORTHOPHOTOS',
			olParams: {
				visible: false
			}
		}));
		map.addControl(new ol.control.SearchEngine());
		map.addControl(new ol.control.ElevationPath());
		map.addControl(new ol.control.Route());
		map.addControl(new ol.control.GeoportalMousePosition({
			collapsed: true
		}));
		map.addControl(layerSwitcher);
		layerSwitcher.removeLayer(olAgriLayer);
		layerSwitcher.addLayer(olAgriLayer, {
			title: agriTitle,
			description: agriDescription,
			legends: [{ url: agriLegend }]
		});
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
		tipLabel: pleinecrantxt
	}));
	document.getElementsByClassName('ol-full-screen')[0].style.left = '43px';
	document.getElementsByClassName('ol-full-screen')[0].style.right = 'unset';
	const selectDoubleClick = new ol.interaction.Select({
		condition: ol.events.condition.doubleClick,
		layers: [vectorLayer]
	});
	map.addInteraction(selectDoubleClick);
	selectDoubleClick.on('select', function(e) {
		// http://openlayers.org/en/latest/examples/popup.html
		const feature = e.target.getFeatures().getArray()[0];
		const idx = feature.get('idx');
		document.getElementById('popup-content').innerHTML =
			'<a href="' + urlruches + 'rucher/' +
			(ign ? 'Ign/' : 'Osm/') + ruchers[idx].id + '">' +
			ruchertxt + ' ' + ruchers[idx].nom +
			'</a><br/>' +
			((nomRuches[idx] === '') ? pasderuchetxt :
				lesRuchestxt + ' ' + nomRuches[idx]);
		overlay.setPosition(feature.getGeometry().getCoordinates());
		selectDoubleClick.getFeatures().clear();
	});
	translate.on('translateend', function(evt) {
		const coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
		const req = new XMLHttpRequest();
		const idx = evt.features.getArray()[0].get('idx');
		req.open('POST',
			urlruches + 'rucher/deplace/' + ruchers[idx].id +
			'/' + coord[1] + '/' + coord[0],
			true);
		// met à jour les coords du rucher déplacé, idem rucherDetailOl.js
		ruchers[idx].longitude = coord[0];
		ruchers[idx].latitude = coord[1];
		req.setRequestHeader('x-csrf-token', _csrf_token);
		req.send(null);
	});
	$('#searchtext').keyup(function(event) {
		if (event.keyCode === 13) {
			const searchtext = $('#searchtext').val().toUpperCase();
			for (const marker of markerRuchers) {
				if (marker.getStyle().getText().getText().toUpperCase().includes(searchtext)) {
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
	$('#dragMarker').change(function() {
		select.setActive(!this.checked);
		translate.setActive(!this.checked);
	});
}
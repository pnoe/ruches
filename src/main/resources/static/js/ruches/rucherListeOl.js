/* jshint  esversion: 6, browser: true, jquery: true, unused: true, undef: true, varstmt: true */
/* globals ol,
	ruchers, nomRuches,	recentertxt, lesRucherstxt,	lesRuchestxt, ruchertxt, couchemarqueursrucherstxt,
	pleinecrantxt, urlruches, _csrf_token
*/

function rucherListeIgn(ign) {
	$('.recentre').on('click', function () {
        return confirm(recentertxt);
    });
	$('.oi-question-mark').popover({html: true});
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
	        type: 'geoMarker',
	        geometry: new ol.geom.Point(ol.proj.fromLonLat(coordsMarker)),
	        rucherid: ruchers[i].id,
	        ruchernom: ruchers[i].nom,
	        ruchesnom: nomRuches[i]
	      });
		iconFeature.setStyle(
				new ol.style.Style({
			          image: new ol.style.Circle({
			            radius: 12,
			            fill: new ol.style.Fill({color: '#FFFF00'})
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
	const select = new ol.interaction.Select({
			layers: [vectorLayer],
	        toggleCondition: ol.events.condition.never,
	        style: false
	});
	select.on('select', function(e) {   	  
	    	e.selected.forEach(function(feature) { let style = feature.getStyle(); let txt = style.getText().getText(); 
	    		style.getText().setText('['+txt+']'); feature.setStyle(style); });
	    	e.deselected.forEach(function(feature) { let style = feature.getStyle(); let txt = style.getText().getText(); 
	  	  		style.getText().setText(txt.substring(1,txt.length-1)); feature.setStyle(style); });
	    });
	
    const translate = new ol.interaction.Translate({
      features: select.getFeatures(),
      layers: [vectorLayer]
    });
    translate.setActive(!$('#dragMarker')[0].checked);
	const map = new ol.Map({
		// ajout {doubleClickZoom: false}
		interactions: ol.interaction.defaults({doubleClickZoom: false}).extend([select, translate]),
		controls : ol.control.defaults({
	        attribution : false
	    }),
        target: 'map',
        layers: [vectorLayer],        	
        overlays: [overlay],
        view: new ol.View({
            center: ol.proj.fromLonLat(mapcenter),
            zoom: 11
        })
    });
	let layerSwitcher = new ol.control.LayerSwitcher({
	    layers : [{
	        layer: vectorLayer,
	        config: {
	            title: lesRucherstxt,
	            description: couchemarqueursrucherstxt
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
		map.addControl(new ol.control.Route());
		map.addControl(new ol.control.GeoportalMousePosition({
	        collapsed: true,
	        displayCoordinates : false
	    }));
        map.addControl(layerSwitcher);
    } else {
    	const osmLayer =  new ol.layer.Tile({
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
	document.getElementsByClassName('ol-full-screen')[0].style.left='43px';
	document.getElementsByClassName('ol-full-screen')[0].style.right='unset';
    const selectDoubleClick = new ol.interaction.Select({
        condition: ol.events.condition.doubleClick,
        layers: [vectorLayer]
      });
    map.addInteraction(selectDoubleClick);
    selectDoubleClick.on('select', function(e) {
 		// http://openlayers.org/en/latest/examples/popup.html
 		const feature = e.target.getFeatures().getArray()[0];
		document.getElementById('popup-content').innerHTML =
			'<a href="' + urlruches + 'rucher/' + 
			(ign?'Ign/':'Osm/') + feature.get('rucherid') + '">' +
			ruchertxt + ' ' + feature.get("ruchernom") + 
			'</a><br/>' + lesRuchestxt + ' ' + feature.get("ruchesnom");
		overlay.setPosition(feature.getGeometry().getCoordinates());
		selectDoubleClick.getFeatures().clear();
      });
    translate.on('translateend', function (evt) {
    	const coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
	    const req = new XMLHttpRequest();
	    req.open('POST',
	    		urlruches + 'rucher/deplace/' + evt.features.getArray()[0].get("rucherid") +
	      	  '/' + coord[1] + '/' + coord[0], 
	    		true);
	    req.setRequestHeader('x-csrf-token', _csrf_token);
	    req.send(null);
    });
    $("#searchtext").keyup( function(event) {
    	if (event.keyCode === 13) {
	    	let searchtext = $("#searchtext").val().toUpperCase();
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
	  	translate.setActive(!this.checked);
	});
  }
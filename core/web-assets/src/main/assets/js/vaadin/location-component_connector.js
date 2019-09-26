/* eslint no-console: 0 */

const L = require('vendor/leaflet-js');

if (!window.org_opennms_features_topology_app_internal_ui_geographical_LocationComponent) {
    var __onms_getImagePath = function getImagePath() {
        var el = L.DomUtil.create('div',  'leaflet-default-icon-path', document.body);
        var path = L.DomUtil.getStyle(el, 'background-image') ||
                   L.DomUtil.getStyle(el, 'backgroundImage');   // IE8

        document.body.removeChild(el);

        return path.indexOf('url') === 0 ?
                path.replace(/^url\(["']?/, '').replace(/marker-icon\.png["']?\)$/, '') : '';
    };

    window.org_opennms_features_topology_app_internal_ui_geographical_LocationComponent = function LocationComponent() {
        var state = this.getState();

        // The id is configurable, as we may have multiple or to prevent id conflicts
        var mapId = state.mapId;

        // Add the map container
        this.getElement().innerHTML='<div style="width: 100%; height: 100%" id="' + mapId + '"></div>';

        // Create the Map
        var theMap = L.map(mapId);
        L.tileLayer(state.tileLayer, state.layerOptions).addTo(theMap);

        var imagePath = __onms_getImagePath();

        var notMarkedIcon = L.icon({
            /*
            iconUrl: L.Icon.Default.imagePath + '/not-marked-icon.png',
            iconRetinaUrl: L.Icon.Default.imagePath + '/not-marked-icon-2x.png',
            */
            iconUrl: imagePath + 'not-marked-icon.png',
            iconRetinaUrl: imagePath + 'not-marked-icon-2x.png',
            iconSize:    [25, 41],
            iconAnchor:  [12, 41],
            popupAnchor: [1, -34],
            tooltipAnchor: [16, -28],
            /*
            shadowUrl: L.Icon.Default.imagePath + '/marker-shadow.png',
            shadowRetinaUrl: L.Icon.Default.imagePath + '/marker-shadow.png',
            */
            shadowUrl: imagePath + 'marker-shadow.png',
            shadowRetinaUrl: imagePath + 'marker-shadow.png',
            shadowSize:  [41, 41]
        });

        var markers = state.markers;
        var coordinates = [];
        var markerArray = [];
        for (var i = 0; i < markers.length; i++) {
            var latitude = markers[i].coordinates.latitude;
            var longitude = markers[i].coordinates.longitude;
            var marker = L.marker(L.latLng(latitude, longitude));

            if (markers[i].tooltip !== undefined) {
                marker.bindPopup(markers[i].tooltip);
            }
            if (!markers[i].marked) {
                marker.setIcon(notMarkedIcon);
            }
            marker.addTo(theMap);
            coordinates.push([latitude, longitude]);
            markerArray.push(marker);
        }

        // show all markers
        var markerGroup = new L.featureGroup(markerArray);
        theMap.fitBounds(markerGroup.getBounds().pad(0.2));

        // If we have only one vertex, center it
        if (markerArray.length === 1) {
            // Center the view
            var center = coordinates.reduce(function getCenter(x,y) {
                return [x[0] + y[0]/coordinates.length, x[1] + y[1]/coordinates.length];
            }, [0,0]);

            // Collect coordinates
            theMap.setView([center[0], center[1]], state.initialZoom);
        }
    };

    console.log('init: location-component');
}

module.exports = window.org_opennms_features_topology_app_internal_ui_geographical_LocationComponent;
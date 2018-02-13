'use strict';

/* eslint no-console: 0 */

const jquery = require('vendor/jquery-js');
const L = require('vendor/leaflet-js');

require('geomap.scss');

const retries = 3; // the number of retries for each delay
const retryDelay = [5, 10, 30, 60, 300]; // seconds

const severities = ['Normal', 'Warning', 'Minor', 'Major', 'Critical'];
const severityIcons = {
    'Normal': 'ion ion-ios-medical',
    'Warning': 'ion ion-alert-circled',
    'Minor': 'ion ion-flash',
    'Major': 'ion ion-flame',
    'Critical': 'ion ion-nuclear'
};
const severityImages = severities.map((severity) => {
    return require('./images/' + severity + '.png');
});
const severityImagesRetina = severities.map((severity) => {
    return require('./images/' + severity + '@2x.png');
});

let retryCount = 0;
let timer = undefined;

const isUndefinedOrNull = function(input) {
    return input === undefined || input === 'null' || input === null;
};

const determineRetryDelay = function() {
    let index = parseInt(retryCount / retries, 10);
    if (index >= retryDelay.length) {
        index = retryDelay.length - 1;
    }
    return retryDelay[index]
};

const render = function(options) {
    // Set variables bases on options
    const baseHref = isUndefinedOrNull(options.baseHref) ? '/opennms/' : options.baseHref;
    const hideControlsOnStartup = isUndefinedOrNull(options.hideControlsOnStartup) ? false : options.hideControlsOnStartup;
    const mapId = isUndefinedOrNull(options.mapId) ? 'map' : options.mapId;

    let query = {
        strategy: isUndefinedOrNull(options.strategy) ? 'Alarms' : options.strategy,
        severityFilter: isUndefinedOrNull(options.severity) ?  'Normal' : options.severity,
        includeAcknowledgedAlarms: isUndefinedOrNull(options.includeAcknowledgedAlarms) ? false : options.includeAcknowledgedAlarms
    };

    // Define other variables
    const restEndpoint = baseHref + 'api/v2/geolocation';
    let theMap = undefined;
    let markersGroup = undefined;

    let triggerRetry = function(fn) {
        if (timer !== undefined) {
            clearTimeout(timer);
            retryCount = 0;
        }
        const delay = determineRetryDelay();
        console.error('Retry in', delay, 'seconds');
        timer = setTimeout(function () {
            console.log('retrying...');
            timer = undefined;
            fn();
        }, delay * 1000);
    };

    const getIcons = function () {
        let icons = {};
        for (let i = 0; i < severities.length; i++) {
            icons[severities[i]] = L.icon({
                iconUrl: baseHref + '/assets/' + severityImages[i],
                iconRetinaUrl: baseHref + '/assets/' + severityImagesRetina[i],
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            });
        }
        return icons;
    };

    var loadConfig = function() {
        $.ajax({
            method: 'GET',
            url: restEndpoint + '/config',
            contentType: 'application/json',
            dataType: 'json',
            success: function(config) {
                retryCount = 0;
                initMap(config);
                loadGeolocations(query, centerOnMap);
            },
            error: function(xhr, status, error) {
                console.error('Error receiving configuration from rest endpoint. Status: ' + status + ' Error: ' + error);
                triggerRetry(loadConfig);
                retryCount++;
            }
        })
    };

    var loadGeolocations = function(query, fn) {
        $.ajax({
            method: 'POST',
            url: restEndpoint,
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify(query),
            success: function (data) {
                retryCount = 0;
                if (data !== undefined) {
                    resetMap(data);
                } else {
                    resetMap([]);
                }
                // Invoke Callback function if defined
                if (fn) {
                    fn();
                }
            },
            error: function (xhr, status, error) {
                console.error('Error talking to rest endpoint. Status: ' + status + ' Error: ' + error);
                triggerRetry(function() {
                    loadGeolocations(query);
                });
                retryCount++;
            }
        });
    };

    var resetMap = function(theMarkers) {
        markersGroup.clearLayers();
        var icons = getIcons();
        for (var i = 0; i < theMarkers.length; i++) {
            var markerData = theMarkers[i];
            if (markerData.coordinates !== undefined) {
                var latitude = markerData.coordinates.latitude;
                var longitude = markerData.coordinates.longitude;

                var icon = icons['Normal'];
                if (markerData.severityInfo !== undefined
                    && markerData.severityInfo.label !== undefined) {
                    icon = icons[markerData.severityInfo.label];
                }
                var marker = L.marker(L.latLng(latitude, longitude), {
                    icon: icon
                });

                var popup = buildMarkerPopup(markerData);
                marker.bindPopup(popup);
                marker.data = markerData;
                markersGroup.addLayer(marker);
            }
        }
    };

    var buildMarkerPopup = function (marker) {
        var template = L.DomUtil.get('single-popup');
        var popup = template.cloneNode(true);
        var popupContent = L.Util.template(popup.innerHTML, {
            'NODE_ID': emptyStringIfNull(marker.nodeInfo.nodeId),
            'NODE_LABEL': emptyStringIfNull(marker.nodeInfo.nodeLabel),
            'DESCRIPTION': emptyStringIfNull(marker.nodeInfo.description),
            'MAINT_CONTRACT': emptyStringIfNull(marker.nodeInfo.maintcontract),
            'SEVERITY_LABEL': marker.severityInfo.label,
            'IP_ADDRESS': emptyStringIfNull(marker.nodeInfo.ipAddress),
            'CATEGORIES': marker.nodeInfo.categories.join(', ')
        });
        return popupContent;
    };

    var emptyStringIfNull = function(input) {
        if (input === null || input === 'null') {
            return '';
        }
        return input;
    };

    var createSvgElement = function (dataArray, classArray, total) {
        var cx = 20;
        var cy = 20;
        var r = 20;
        var innerR = 13;

        var startangle = 0;
        var svg = '<svg class="svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="40px" height="40px">';

        for (var i = 0; i < dataArray.length; i++) {
            // Only consider severity if actually available
            if (dataArray[i] > 0) {
                var endangle = startangle + dataArray[i] / total * Math.PI * 2.0;

                // Calculate inner and outer circle
                var x1 = cx + (r * Math.sin(startangle));
                var y1 = cy - (r * Math.cos(startangle));
                var X1 = cx + (innerR * Math.sin(startangle));
                var Y1 = cy - (innerR * Math.cos(startangle));
                var x2 = cx + (r * Math.sin(endangle));
                var y2 = cy - (r * Math.cos(endangle));
                var X2 = cx + (innerR * Math.sin(endangle));
                var Y2 = cy - (innerR * Math.cos(endangle));
                var big = endangle - startangle > Math.PI ? 1 : 0;

                // this branch is if one data value comprises 100% of the data
                if (dataArray[i] >= total) {
                    // path string
                    const d = 'M ' + X1 + ',' + Y1 + ' A ' + innerR + ',' + innerR
                        + ' 0 1 0 ' + X1 + ','
                        + (Y1 + (2 * innerR)) + ' A ' + innerR + ','
                        + innerR + ' 0 ' + big + ' 0 ' + X1 + ',' + Y1
                        + ' M ' + x1 + ',' + y1 + ' A ' + r + ',' + r
                        + ' 0 ' + big + ' 1 ' + x1 + ',' + (y1 + (2 * r))
                        + ' A ' + r + ',' + r + ' 0 ' + big + ' 1 ' + x1
                        + ',' + y1;
                    svg += '<path d="' + d + '" class="' + classArray[i] + '"/>';
                } else {
                    // path string
                    const d = 'M ' + X1 + ',' + Y1 + ' A ' + innerR + ',' + innerR
                        + ' 0 ' + big + ' 1 ' + X2 + ',' + Y2 + ' L ' + x2
                        + ',' + y2 + ' A ' + r + ',' + r + ' 0 ' + big
                        + ' 0 ' + x1 + ',' + y1 + ' Z';
                    svg += '<path d="' + d + '" class="' + classArray[i] + '"/>';
                }
                startangle = endangle;
            }
        }
        svg = svg + '</svg>';
        return svg;
    };

    var centerOnMap = function() {
        if (markersGroup.getBounds().isValid()) {
            theMap.fitBounds(markersGroup.getBounds(), {padding: [15, 15]});
        } else {
            theMap.setZoom(1);
            theMap.setView([34.5133, -94.1629]); // center of earth
        }
    };

    var createButton = function(title, className, container, fn) {
        var link = L.DomUtil.create('a', className, container);
        link.href = '#';
        link.title = title;
        link.style.fontSize = '120%';

        L.DomEvent
            .on(link, 'mousedown dblclick', L.DomEvent.stopPropagation)
            .on(link, 'click', L.DomEvent.stop)
            // eslint-disable-next-line no-invalid-this
            .on(link, 'click', fn, this);
            //.on(link, 'click', fn, container);

        return link;
    };

    var CenterOnMarkersControl = L.Control.extend({
        options: {
            position: 'topright'
        },

        initialize: function (options) {
            L.Util.setOptions(this, options);
        },

        onAdd: function (map) {
            // create the control container with a particular class name
            var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            var refresh = createButton('Refresh', 'fa fa-refresh', container, function() {
                loadGeolocations(query);
            });
            var center = createButton('Center on marker', 'fa fa-location-arrow', container, function() {
                centerOnMap();
            });
            var includeAcknowledgedAlarmsButton = createButton('Include acknowledged alarms in status calculation', 'fa fa-square-o', container, function() {
                query.includeAcknowledgedAlarms = !query.includeAcknowledgedAlarms;
                L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, 'fa-check-square-o');
                L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, 'fa-square-o');
                if (query.includeAcknowledgedAlarms) {
                    L.DomUtil.addClass(includeAcknowledgedAlarmsButton, 'fa-check-square-o');
                } else {
                    L.DomUtil.addClass(includeAcknowledgedAlarmsButton, 'fa-square-o');
                }
                loadGeolocations(query);
            });
            includeAcknowledgedAlarmsButton.id = 'toolbarIncludeAcknowledgedAlarmsButton';
            return container;
        }
    });

    var SeverityFilterControl = L.Control.extend({
        onAdd: function (map) {
            var setSeverityLabel = function(severity) {
                filterLabel.title = 'Show markers with severity >= ' + severity;
                filterLabel.className = severityIcons[severity];
            };

            // Applies the severity
            var applySeverity = function(severity) {
                if (query.severityFilter !== severity) {
                    query.severityFilter = severity;
                    loadGeolocations(query)
                    setSeverityLabel(query.severityFilter);
                }
            };

            // create the control container with a particular class name
            var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            // Increase Severity button
            createButton('Increase severity filter', 'fa fa-angle-up', container, function() {
                var index = severities.indexOf(query.severityFilter);
                if (index < severities.length - 1) {
                    index++;
                }
                applySeverity(severities[index]);
            });

            var filterLabel = createButton('', '', container, function() {});

            // Decrase severity button
            createButton('Decrease severity filter', 'fa fa-angle-down', container, function() {
                var index = severities.indexOf(query.severityFilter);
                if (index > 0) {
                    index--;
                }
                applySeverity(severities[index]);
            });

            // Apply default selection
            setSeverityLabel(query.severityFilter);

            return container;
        }
    });

    var StatusCalculatorStrategyControl = L.Control.extend({
        onAdd: function (map) {
            // create the control container with a particular class name
            var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');

            var alarmButton = createButton('Calculate status based on alarms', 'fa fa-exclamation', container, function(e) {
                buttonClick(e.target, 'Alarms');
            });

            var outageButton = createButton('Calculate status based on outages', 'fa fa-flash', container, function(e) {
                buttonClick(e.target, 'Outages');
            });

            var setSelected = function(strategy) {
                if (strategy === 'Alarms') {
                    L.DomUtil.addClass(alarmButton, 'selected');
                    L.DomUtil.removeClass(outageButton, 'selected');
                    $('#toolbarIncludeAcknowledgedAlarmsButton').show();
                }
                if (strategy === 'Outages') {
                    L.DomUtil.removeClass(alarmButton, 'selected');
                    L.DomUtil.addClass(outageButton, 'selected');
                    $('#toolbarIncludeAcknowledgedAlarmsButton').hide();
                }
            }

            var buttonClick = function(button, strategy) {
                query.strategy = strategy;
                loadGeolocations(query);
                setSelected(strategy);
            };

            // Apply default
            setSelected(query.strategy);
            return container;
        }
    });

    var SeverityLegendControl = L.Control.extend({
        options: {
            position: 'bottomleft'
        },

        onAdd: function (map) {
            var container = L.DomUtil.create('div', 'leaflet-control-attribution leaflet-control');
            for (var i = 0; i < severities.length; i++) {
                container.innerHTML +=
                    '<div style="float:left;">' +
                    '<div style="float:left; margin-top: 3px; display:inline-block; height:10px; width: 10px;" class="marker-cluster-' + severities[i] + '" ></div><div style="float: left; margin-right: 4pt; margin-left: 2pt;">' + severities[i] + ' </div>' +
                    '</div>';
            }
            return container;
        }
    });

    var initMap = function(config) {
        // create map
        theMap = L.map(mapId, {
            zoom: 1,
            maxZoom: 15,
            zoomControl: false
        });

        // add tile layer
        L.tileLayer(config.tileServerUrl, config.options).addTo(theMap);

        // add marker layer
        markersGroup = L.markerClusterGroup({
                zoomToBoundsOnClick: false,
                iconCreateFunction: function (cluster) {
                    var severity = 0;
                    var severityLabel = 'Normal';
                    var severityArray = [0, 0, 0, 0, 0, 0, 0];
                    var classArray = severities;

                    for (var i = 0; i < cluster.getAllChildMarkers().length; i++) {
                        var markerData = cluster.getAllChildMarkers()[i].data;
                        severityArray[markerData.severityInfo.id - 1]++;
                        if (severity < markerData.severityInfo.id) {
                            severity = markerData.severityInfo.id;
                            severityLabel = markerData.severityInfo.label
                        }
                    }

                    var svg = createSvgElement(severityArray.slice(2, severityArray.length), classArray, cluster.getAllChildMarkers().length);
                    return L.divIcon({
                        iconSize: L.point(40, 40),
                        className: 'marker-cluster marker-cluster-' + severityLabel,
                        html: svg + '<div><span>' + cluster.getChildCount() + '</span></div>'
                    })

                }
            }
        );
        markersGroup.addTo(theMap);
        markersGroup.on('clusterclick', function (event) {
            if (theMap.getZoom() !== theMap.getMaxZoom()) {
                var markers = event.layer.getAllChildMarkers();
                var tableContent = '';
                var nodeIds = [];
                var unacknowledgedAlarms = 0;

                // Sort the markers based on the severity, starting with the worst
                markers.sort(function(a, b) {
                    return -1 * (a.data.severityInfo.id - b.data.severityInfo.id);
                });
                // Build table content
                for (var i = 0; i < markers.length; i++) {
                    var markerData = markers[i].data;
                    unacknowledgedAlarms += markerData.alarmUnackedCount;
                    nodeIds.push(markerData.nodeInfo.nodeId);
                    var rowTemplate = L.DomUtil.get('multi-popup-table-row')
                        .cloneNode(true)
                        .children[0].children[0]
                        .innerHTML;

                    tableContent += L.Util.template(rowTemplate, {
                        'NODE_ID': emptyStringIfNull(markerData.nodeInfo.nodeId),
                        'NODE_LABEL': emptyStringIfNull(markerData.nodeInfo.nodeLabel),
                        'SEVERITY_LABEL': markerData.severityInfo.label,
                        'IP_ADDRESS': emptyStringIfNull(markerData.nodeInfo.ipAddress)
                    });
                }

                var template = L.DomUtil.get('multi-popup');
                var popupContent = L.Util.template(template.cloneNode(true).innerHTML, {
                    'NUMBER_NODES': markers.length,
                    'NUMBER_UNACKED': unacknowledgedAlarms,
                    'NODE_IDS': nodeIds.join(','),
                    'TABLE_CONTENT': '<table class="node-marker-list">' + tableContent + '</table>'
                });

                var popup = L.popup({
                    'minWidth': 500,
                    'maxWidth': 500,
                    'maxHeight': 300,
                    'className': 'node-marker-popup'
                });
                popup.setContent(popupContent);
                popup.setLatLng(event.layer.getLatLng());

                popup.openOn(theMap);
            }
        });

        // Toolbar Controls
        L.control.zoom({position: 'topright'}).addTo(theMap);
        new SeverityFilterControl().addTo(theMap);
        new CenterOnMarkersControl().addTo(theMap);
        new StatusCalculatorStrategyControl().addTo(theMap);
        new SeverityLegendControl().addTo(theMap);

        if (hideControlsOnStartup) {
            var setControlVisibility = function (visible) {
                $('.leaflet-right.leaflet-top')[0].style.display =  visible ? 'block' : 'none';
            };
            theMap.on('mouseover', function () {
                setControlVisibility(true);
            });
            theMap.on('mouseout', function () {
                setControlVisibility(false);
            });
            setControlVisibility(false);
        }
    };
    loadConfig();
};

module.exports = render;
window.geomap = {
    render: render
};
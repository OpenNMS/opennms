/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
'use strict';

/* eslint no-console: 0 */
/* eslint @typescript-eslint/no-use-before-define: 0 */

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
    return require('./images/' + severity + '.png').default;
});
const severityImagesRetina = severities.map((severity) => {
    return require('./images/' + severity + '@2x.png').default;
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

    const query = {
        strategy: isUndefinedOrNull(options.strategy) ? 'Alarms' : options.strategy,
        severityFilter: isUndefinedOrNull(options.severity) ?  'Normal' : options.severity,
        includeAcknowledgedAlarms: isUndefinedOrNull(options.includeAcknowledgedAlarms) ? false : options.includeAcknowledgedAlarms
    };

    // Define other variables
    const restEndpoint = baseHref + 'api/v2/geolocation';
    let theMap = undefined;
    let markersGroup = undefined;

    const triggerRetry = function(fn) {
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
        const icons = {};
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

    const loadConfig = function() {
        $.ajax({
            method: 'GET',
            url: restEndpoint + '/config',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
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

    const loadGeolocations = function(query, fn) {
        $.ajax({
            method: 'POST',
            url: restEndpoint,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
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

    const resetMap = function(theMarkers) {
        markersGroup.clearLayers();
        const icons = getIcons();
        for (let i = 0; i < theMarkers.length; i++) {
            const markerData = theMarkers[i];
            if (markerData.coordinates !== undefined) {
                const latitude = markerData.coordinates.latitude;
                const longitude = markerData.coordinates.longitude;

                let icon = icons['Normal'];
                if (markerData.severityInfo !== undefined
                    && markerData.severityInfo.label !== undefined) {
                    icon = icons[markerData.severityInfo.label];
                }
                const marker = L.marker(L.latLng(latitude, longitude), {
                    icon: icon
                });

                const popup = buildMarkerPopup(markerData);
                marker.bindPopup(popup);
                marker.data = markerData;
                markersGroup.addLayer(marker);
            }
        }
    };

    const buildMarkerPopup = function (marker) {
        const template = L.DomUtil.get('single-popup');
        const popup = template.cloneNode(true);
        const popupContent = L.Util.template(popup.innerHTML, {
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

    const emptyStringIfNull = function(input) {
        if (input === null || input === 'null') {
            return '';
        }
        return input;
    };

    const createSvgElement = function (dataArray, classArray, total) {
        const cx = 20;
        const cy = 20;
        const r = 20;
        const innerR = 13;

        let startangle = 0;
        let svg = '<svg class="svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="40px" height="40px">';

        for (let i = 0; i < dataArray.length; i++) {
            // Only consider severity if actually available
            if (dataArray[i] > 0) {
                const endangle = startangle + dataArray[i] / total * Math.PI * 2.0;

                // Calculate inner and outer circle
                const x1 = cx + (r * Math.sin(startangle));
                const y1 = cy - (r * Math.cos(startangle));
                const X1 = cx + (innerR * Math.sin(startangle));
                const Y1 = cy - (innerR * Math.cos(startangle));
                const x2 = cx + (r * Math.sin(endangle));
                const y2 = cy - (r * Math.cos(endangle));
                const X2 = cx + (innerR * Math.sin(endangle));
                const Y2 = cy - (innerR * Math.cos(endangle));
                const big = endangle - startangle > Math.PI ? 1 : 0;

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

    const centerOnMap = function() {
        if (markersGroup.getBounds().isValid()) {
            theMap.fitBounds(markersGroup.getBounds(), {padding: [15, 15]});
        } else {
            theMap.setZoom(1);
            theMap.setView([34.5133, -94.1629]); // center of earth
        }
    };

    const createButton = function(title, className, container, fn) {
        const link = L.DomUtil.create('a', className, container);
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

    const CenterOnMarkersControl = L.Control.extend({
        options: {
            position: 'topright'
        },

        initialize: function (options) {
            L.Util.setOptions(this, options);
        },

        onAdd: function (map) {
            // create the control container with a particular class name
            const container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            const refresh = createButton('Refresh', 'fa fa-refresh', container, function() {
                loadGeolocations(query);
            });
            const center = createButton('Center on marker', 'fa fa-location-arrow', container, function() {
                centerOnMap();
            });
            const includeAcknowledgedAlarmsButton = createButton('Include acknowledged alarms in status calculation', 'fa fa-square-o', container, function() {
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

    const SeverityFilterControl = L.Control.extend({
        onAdd: function (map) {
            const setSeverityLabel = function(severity) {
                filterLabel.title = 'Show markers with severity >= ' + severity;
                filterLabel.className = severityIcons[severity];
            };

            // Applies the severity
            const applySeverity = function(severity) {
                if (query.severityFilter !== severity) {
                    query.severityFilter = severity;
                    loadGeolocations(query)
                    setSeverityLabel(query.severityFilter);
                }
            };

            // create the control container with a particular class name
            const container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
            // Increase Severity button
            createButton('Increase severity filter', 'fa fa-angle-up', container, function() {
                let index = severities.indexOf(query.severityFilter);
                if (index < severities.length - 1) {
                    index++;
                }
                applySeverity(severities[index]);
            });

            const filterLabel = createButton('', '', container, function() {}); // eslint-disable-line @typescript-eslint/no-empty-function

            // Decrase severity button
            createButton('Decrease severity filter', 'fa fa-angle-down', container, function() {
                let index = severities.indexOf(query.severityFilter);
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

    const StatusCalculatorStrategyControl = L.Control.extend({
        onAdd: function (map) {
            // create the control container with a particular class name
            const container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');

            const alarmButton = createButton('Calculate status based on alarms', 'fa fa-exclamation', container, function(e) {
                buttonClick(e.target, 'Alarms');
            });

            const outageButton = createButton('Calculate status based on outages', 'fa fa-flash', container, function(e) {
                buttonClick(e.target, 'Outages');
            });

            const setSelected = function(strategy) {
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

            const buttonClick = function(button, strategy) {
                query.strategy = strategy;
                loadGeolocations(query);
                setSelected(strategy);
            };

            // Apply default
            setSelected(query.strategy);
            return container;
        }
    });

    const SeverityLegendControl = L.Control.extend({
        options: {
            position: 'bottomleft'
        },

        onAdd: function (map) {
            const container = L.DomUtil.create('div', 'leaflet-control-attribution leaflet-control');
            for (let i = 0; i < severities.length; i++) {
                container.innerHTML +=
                    '<div style="float:left;">' +
                    '<div style="float:left; margin-top: 3px; display:inline-block; height:10px; width: 10px;" class="marker-cluster-' + severities[i] + '" ></div><div style="float: left; margin-right: 4pt; margin-left: 2pt;">' + severities[i] + ' </div>' +
                    '</div>';
            }
            return container;
        }
    });

    const initMap = function(config) {
        // create map
        theMap = L.map(mapId, {
            zoom: 1,
            maxZoom: 18,
            zoomControl: false
        });

        // add tile layer
        L.tileLayer(config.tileServerUrl, config.options).addTo(theMap);

        // add marker layer
        markersGroup = L.markerClusterGroup({
                zoomToBoundsOnClick: false,
                iconCreateFunction: function (cluster) {
                    let severity = 0;
                    let severityLabel = 'Normal';
                    const severityArray = [0, 0, 0, 0, 0, 0, 0];
                    const classArray = severities;

                    for (let i = 0; i < cluster.getAllChildMarkers().length; i++) {
                        const markerData = cluster.getAllChildMarkers()[i].data;
                        severityArray[markerData.severityInfo.id - 1]++;
                        if (severity < markerData.severityInfo.id) {
                            severity = markerData.severityInfo.id;
                            severityLabel = markerData.severityInfo.label
                        }
                    }

                    const svg = createSvgElement(severityArray.slice(2, severityArray.length), classArray, cluster.getAllChildMarkers().length);
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
                const markers = event.layer.getAllChildMarkers();
                let tableContent = '';
                const nodeIds = [];
                let unacknowledgedAlarms = 0;

                // Sort the markers based on the severity, starting with the worst
                markers.sort(function(a, b) {
                    return -1 * (a.data.severityInfo.id - b.data.severityInfo.id);
                });
                // Build table content
                for (let i = 0; i < markers.length; i++) {
                    const markerData = markers[i].data;
                    unacknowledgedAlarms += markerData.alarmUnackedCount;
                    nodeIds.push(markerData.nodeInfo.nodeId);
                    const rowTemplate = L.DomUtil.get('multi-popup-table-row')
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

                const template = L.DomUtil.get('multi-popup');
                const popupContent = L.Util.template(template.cloneNode(true).innerHTML, {
                    'NUMBER_NODES': markers.length,
                    'NUMBER_UNACKED': unacknowledgedAlarms,
                    'NODE_IDS': nodeIds.join(','),
                    'TABLE_CONTENT': '<table class="node-marker-list">' + tableContent + '</table>'
                });

                const popup = L.popup({
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
            const setControlVisibility = function (visible) {
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

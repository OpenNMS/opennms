Geomap = function() {
    "use strict";

    var isUndefinedOrNull = function(input) {
        return input == undefined || "null" === input || null === input;
    };

    var render = function(options) {
        // Set variables bases on options
        var baseHref = options.baseHref == undefined ? "/opennms/" : options.baseHref;
        var hideControlsOnStartup = isUndefinedOrNull(options.hideControlsOnStartup) ? false : options.hideControlsOnStartup;
        var mapId = isUndefinedOrNull(options.mapId) ? "map" : options.mapId;
        var query = {
            resolveMissingCoordinatesFromAddressString: isUndefinedOrNull(options.resolveCoordinatesFromAddressString) ? false : options.resolveCoordinatesFromAddressString,
            strategy: isUndefinedOrNull(options.strategy) ? "Alarms" : options.strategy,
            severityFilter: isUndefinedOrNull(options.severity) ?  "Normal" : options.severity,
            includeAcknowledgedAlarms: isUndefinedOrNull(options.includeAcknowledgedAlarms) ? false : options.includeAcknowledgedAlarms
        };

        // Define other variables
        var restEndpoint = baseHref + "api/v2/geolocation";
        var severities = ["Normal", "Warning", "Minor", "Major", "Critical"];
        var theMap = undefined;
        var markersGroup = undefined;
        var markersData = [];
        var controls = [];

        var getIcons = function () {
            var icons = {};
            for (var i = 0; i < severities.length; i++) {
                var icon = L.icon({
                    iconUrl: baseHref + 'geolocation/images/' + severities[i] + '.png',
                    iconRetinaUrl: baseHref + 'geolocation/images/' + severities[i] + '@2x.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41],
                });
                icons[severities[i]] = icon;
            }
            return icons;
        };

        var loadConfig = function() {
            $.ajax({
                method: "GET",
                url: restEndpoint + "/config",
                contentType: 'application/json',
                dataType: 'json',
                success: function(config) {
                    initMap(config);
                    loadGeolocations(query, function() { centerOnMap(); });
                },
                error: function(xhr, status, error) {
                    console.error("Error receiving configuration from rest endpoint. Status: " + status + " Error: " + error);
                }
            })
        };

        var loadGeolocations = function(query, fn) {
            $.ajax({
                method: "POST",
                url: restEndpoint,
                contentType: 'application/json',
                dataType: 'json',
                data: JSON.stringify(query),
                success: function (data) {
                    if (data != undefined) {
                        resetMap(data, fn);
                    } else {
                        resetMap([], fn);
                    }
                },
                error: function (xhr, status, error) {
                    console.error("Error talking to rest endpoint. Status: " + status + " Error: " + error);
                }
            });
        };

        var resetMap = function(theMarkers, fn) {
            markersGroup.clearLayers();
            markersData = [];
            var icons = getIcons();
            for (var i = 0; i < theMarkers.length; i++) {
                var markerData = theMarkers[i];
                if (markerData.coordinates != undefined) {
                    var latitude = markerData.coordinates.latitude;
                    var longitude = markerData.coordinates.longitude;

                    var icon = icons["Normal"];
                    if (markerData.severityInfo != undefined
                        && markerData.severityInfo.label != undefined) {
                        icon = icons[markerData.severityInfo.label];
                    }
                    var marker = L.marker(L.latLng(latitude, longitude), {
                        icon: icon
                    });

                    var popup = buildMarkerPopup(markerData);
                    marker.bindPopup(popup);
                    marker.data = markerData;
                    markersGroup.addLayer(marker);
                    markersData.push(markerData);
                }
            }
            // Invoke Callback function if defined
            if (fn) {
                fn();
            }
        };

        var buildMarkerPopup = function (marker) {
            var template = L.DomUtil.get("single-popup");
            var popup = template.cloneNode(true);
            var popupContent = L.Util.template(popup.innerHTML, {
                "NODE_ID": emptyStringIfNull(marker.nodeInfo.nodeId),
                "NODE_LABEL": emptyStringIfNull(marker.nodeInfo.nodeLabel),
                "DESCRIPTION": emptyStringIfNull(marker.nodeInfo.description),
                "MAINT_CONTRACT": emptyStringIfNull(marker.nodeInfo.maintcontract),
                "SEVERITY_LABEL": marker.severityInfo.label,
                "IP_ADDRESS": emptyStringIfNull(marker.nodeInfo.ipAddress),
                "CATEGORIES": marker.nodeInfo.categories.join(", ")
            });
            return popupContent;
        };

        var emptyStringIfNull = function(input) {
            if (input === null || input === "null") {
                return "";
            }
            return input;
        };

        var createSvgElement = function (dataArray, classArray, total) {
            var cx = 20;
            var cy = 20;
            var r = 18;
            var innerR = 12;

            var startangle = 0;
            var svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"40px\" height=\"40px\">";

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
                        var d = "M " + X1 + "," + Y1 + " A " + innerR + "," + innerR
                            + " 0 " + "1" + " 0 " + X1 + ","
                            + (Y1 + (2 * innerR)) + " A " + innerR + ","
                            + innerR + " 0 " + big + " 0 " + X1 + "," + Y1
                            + " M " + x1 + "," + y1 + " A " + r + "," + r
                            + " 0 " + big + " 1 " + x1 + "," + (y1 + (2 * r))
                            + " A " + r + "," + r + " 0 " + big + " 1 " + x1
                            + "," + y1;
                        svg += "<path d=\"" + d + "\" class=\"" + classArray[i] + "\"/>";
                    } else {
                        // path string
                        var d = "M " + X1 + "," + Y1 + " A " + innerR + "," + innerR
                            + " 0 " + big + " 1 " + X2 + "," + Y2 + " L " + x2
                            + "," + y2 + " A " + r + "," + r + " 0 " + big
                            + " 0 " + x1 + "," + y1 + " Z";
                        svg += "<path d=\"" + d + "\" class=\"" + classArray[i] + "\"/>";
                    }
                    startangle = endangle;
                }
            }
            svg = svg + "</svg>";
            return svg;
        };

        var centerOnMap = function() {
            if (markersGroup.getBounds().isValid()) {
                theMap.fitBounds(markersGroup.getBounds(), [100, 100]);
            } else {
                theMap.setZoom(1);
                theMap.setView([34.5133, -94.1629]); // center of earth
            }
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
                var refresh = this.createButton("Refresh", "fa fa-refresh", container, function() {
                    loadGeolocations(query);
                });
                var center = this.createButton("Center on marker", "fa fa-location-arrow", container, function() {
                    centerOnMap();
                });
                var includeAcknowledgedAlarmsButton = this.createButton("Include acknowledged alarms in status calculation", "fa fa-square-o", container, function() {
                    query.includeAcknowledgedAlarms = !query.includeAcknowledgedAlarms;
                    L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, "fa-check-square-o");
                    L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, "fa-square-o");
                    if (query.includeAcknowledgedAlarms) {
                        L.DomUtil.addClass(includeAcknowledgedAlarmsButton, "fa-check-square-o");
                    } else {
                        L.DomUtil.addClass(includeAcknowledgedAlarmsButton, "fa-square-o");
                    }
                    loadGeolocations(query);
                });
                controls.push(container);
                return container;
            },

            createButton: function (title, className, container, fn) {
                var link = L.DomUtil.create('a', className, container);
                link.href = '#';
                link.title = title;
                link.style.fontSize = "120%";

                L.DomEvent
                    .on(link, 'mousedown dblclick', L.DomEvent.stopPropagation)
                    .on(link, 'click', L.DomEvent.stop)
                    .on(link, 'click', fn, this)

                return link;
            }
        });

        var SeverityFilterControl = L.Control.extend({
            options: {
                position: 'topright'
            },

            initialize: function (options) {
                L.Util.setOptions(this, options);
            },

            onAdd: function (map) {
                // create the control container with a particular class name
                var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-dropdown');
                container.style = "z-index: 1000;";

                // Selection
                var severityList = L.DomUtil.create("select", '', container);
                severityList.id = 'severityControl';
                severityList.title = "Show markers with severity >=";
                severityList.onmousedown = L.DomEvent.stopPropagation;
                severityList.onchange = function (event) {
                    query.severityFilter = event.target.value;
                    loadGeolocations(query);
                };

                for (var i = 0; i < severities.length; i++) {
                    var option = L.DomUtil.create('option', '', severityList);
                    option.innerHTML = severities[i];
                    option.value = severities[i];
                    if (option.value == query.severityFilter) {
                        option.selected = "selected";
                    }
                }

                controls.push(container);
                return container;
            }
        });

        var StatusCalculatorControl = L.Control.extend({
            options: {
                position: 'topright'
            },

            initialize: function (options) {
                L.Util.setOptions(this, options);
            },

            onAdd: function (map) {
                // create the control container with a particular class name
                var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control leaflet-control-dropdown');
                container.style = "z-index: 1000;";

                // Selection
                var strategyList = L.DomUtil.create("select", '', container);
                strategyList.id = 'statusStrategyControl';
                strategyList.title = "Calculate status by";
                strategyList.onmousedown = L.DomEvent.stopPropagation;
                strategyList.onchange = function (event) {
                    query.strategy = event.target.value;
                    loadGeolocations(query);
                };

                var alarmOption = L.DomUtil.create('option', '', strategyList);
                alarmOption.innerHTML = "Alarms";
                alarmOption.value = "Alarms";
                if (alarmOption.value == query.strategy) {
                    alarmOption.selected = "selected";
                }

                var outageOption = L.DomUtil.create('option', '', strategyList);
                outageOption.innerHTML = "Outages";
                outageOption.value = "Outages";
                if (outageOption.value == query.strategy) {
                    outageOption.selected = "selected";
                }

                controls.push(container);
                return container;
            }

        });

        var SeverityLegendControl = L.Control.extend({
            options: {
                position: 'bottomleft'
            },

            initialize: function (options) {
                L.Util.setOptions(this, options);
            },

            onAdd: function (map) {
                var container = L.DomUtil.create("div", "leaflet-control-attribution leaflet-control");
                for (var i = 0; i < severities.length; i++) {
                    container.innerHTML += "<span class=\"fa fa-square marker-cluster-" + severities[i] + "\"> </span> <span>" + severities[i] + " </span> "
                }
                return container;
            }
        });

        var setControlVisibility = function (visible) {
            for (var i = 0; i < controls.length; i++) {
                controls[i].style.display = visible ? "block" : "none";
            }
        };

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
                        var severityLabel = "Normal";
                        var severityArray = [0, 0, 0, 0, 0, 0, 0];
                        var classArray = severities;

                        for (var i = 0; i < markersData.length; i++) {
                            severityArray[markersData[i].severityInfo.id - 1]++;
                            if (severity < markersData[i].severityInfo.id) {
                                severity = markersData[i].severityInfo.id;
                                severityLabel = markersData[i].severityInfo.label
                            }
                        }

                        var svg = createSvgElement(severityArray.slice(2, severityArray.length), classArray, markersData.length);
                        return L.divIcon({
                            iconSize: L.point(40, 40),
                            className: "marker-cluster marker-cluster-" + severityLabel,
                            html: svg + "<div><span>" + cluster.getChildCount() + "</span></div>"
                        })

                    }
                }
            );
            markersGroup.addTo(theMap);
            markersGroup.on("clusterclick", function (event) {
                if (theMap.getZoom() != theMap.getMaxZoom()) {
                    var markers = event.layer.getAllChildMarkers();
                    var tableContent = "";
                    var nodeIds = [];
                    var unacknowledgedAlarms = 0;
                    // Build table content
                    for (var i = 0; i < markers.length; i++) {
                        var markerData = markers[i].data;
                        unacknowledgedAlarms += markerData.alarmUnackedCount;
                        nodeIds.push(markerData.nodeInfo.nodeId);
                        var rowTemplate = L.DomUtil.get("multi-popup-table-row")
                            .cloneNode(true)
                            .children[0].children[0]
                            .innerHTML;

                        tableContent += L.Util.template(rowTemplate, {
                            "NODE_ID": emptyStringIfNull(markerData.nodeInfo.nodeId),
                            "NODE_LABEL": emptyStringIfNull(markerData.nodeInfo.nodeLabel),
                            "SEVERITY_LABEL": markerData.severityInfo.label,
                            "IP_ADDRESS": emptyStringIfNull(markerData.nodeInfo.ipAddress),
                        });
                    }

                    var template = L.DomUtil.get("multi-popup");
                    var popup = template.cloneNode(true);
                    var popupContent = L.Util.template(popup.innerHTML, {
                        "NUMBER_NODES": markers.length,
                        "NUMBER_UNACKED": unacknowledgedAlarms,
                        "NODE_IDS": nodeIds.join(","),
                        "TABLE_CONTENT": '<table class="node-marker-list">' + tableContent + '</table>'
                    });

                    var popup = L.popup({
                        'minWidth': 500,
                        'maxWidth': 500,
                        'maxHeight': 300,
                        'className': "node-marker-popup"
                    });
                    popup.setContent(popupContent);
                    popup.setLatLng(event.layer.getLatLng());

                    popup.openOn(theMap);
                }
            });

            // zoom control
            new SeverityFilterControl().addTo(theMap);
            new StatusCalculatorControl().addTo(theMap);
            var zoomControl = L.control.zoom({position: 'topright'});
            zoomControl.addTo(theMap);
            controls.push(zoomControl.getContainer());
            new CenterOnMarkersControl().addTo(theMap);
            new SeverityLegendControl().addTo(theMap);

            if (hideControlsOnStartup) {
                theMap.on("mouseover", function (event) {
                    setControlVisibility(true);
                });
                theMap.on("mouseout", function (event) {
                    setControlVisibility(false);
                });
                setControlVisibility(false);
            }
        };
        loadConfig();
    };

    return {
        render: render
    }
}();
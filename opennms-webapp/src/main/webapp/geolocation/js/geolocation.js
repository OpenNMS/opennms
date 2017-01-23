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
            resolveCoordinatesFromAddressString: isUndefinedOrNull(options.resolveCoordinatesFromAddressString) ? false : options.resolveCoordinatesFromAddressString,
            statusCalculationStrategy: isUndefinedOrNull(options.strategy) ? "Alarms" : options.strategy,
            severity: isUndefinedOrNull(options.severity) ?  "Normal" : options.severity,
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

        var load = function (strategy) {
            var url = restEndpoint;
            if (strategy != undefined) {
                query.statusCalculationStrategy = strategy;
            }
            $.ajax({
                method: "POST",
                url: url,
                contentType: 'application/json',
                dataType: 'json',
                async: false,
                data: JSON.stringify(query),
                success: function (data) {
                    if (data != undefined) {
                        resetMap(data);
                    } else {
                        resetMap([]);
                    }
                },
                error: function (xhr, status, error) {
                    console.error("Error talking to geolocation endpoint. Status: " + status + " Error: " + error);
                }
            });
        };

        var resetMap = function (theMarkers) {
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
            if (markersGroup.getBounds().isValid()) {
                theMap.fitBounds(markersGroup.getBounds(), [100, 100]);
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
        }

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

                var refresh = L.DomUtil.create("a", "fa fa-refresh", container);
                // TODO MVR apply this to all buttons
                refresh.onclick = refresh.ondblclick = refresh.onmousedown = L.DomEvent.stopPropagation;
                refresh.title = "Refresh";
                refresh.style.fontSize = "120%";
                refresh.role = "button";
                refresh.href = "#";
                refresh.onclick = function () {
                    load(query.statusCalculationStrategy);
                };

                var center = L.DomUtil.create("a", "fa fa-location-arrow", container);
                center.title = "Center on marker";
                center.style.fontSize = "120%";
                center.role = "button";
                center.href = "#";
                center.onclick = function () {
                    if (markersGroup.getBounds().isValid()) {
                        theMap.fitBounds(markersGroup.getBounds(), [100, 100]);
                    }
                };

                var includeAcknowledgedAlarmsButton = L.DomUtil.create("a", "fa fa-square-o", container);
                includeAcknowledgedAlarmsButton.title = "Include acknowledged alarms in status calculation";
                includeAcknowledgedAlarmsButton.style.fontSize = "120%";
                includeAcknowledgedAlarmsButton.role = "button";
                includeAcknowledgedAlarmsButton.href = "#";
                includeAcknowledgedAlarmsButton.onclick = function () {
                    query.includeAcknowledgedAlarms = !query.includeAcknowledgedAlarms;
                    L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, "fa-check-square-o");
                    L.DomUtil.removeClass(includeAcknowledgedAlarmsButton, "fa-square-o");
                    if (query.includeAcknowledgedAlarms) {
                        L.DomUtil.addClass(includeAcknowledgedAlarmsButton, "fa-check-square-o");
                    } else {
                        L.DomUtil.addClass(includeAcknowledgedAlarmsButton, "fa-square-o");
                    }
                    load(query.statusCalculationStrategy);
                };
                controls.push(container);
                return container;
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
                    query.severity = event.target.value;
                    load(query.strategy);
                };

                // TODO MVR automatically pre select the option from query.severity
                for (var i = 0; i < severities.length; i++) {
                    var option = L.DomUtil.create('option', '', severityList);
                    option.innerHTML = severities[i];
                    option.value = severities[i];
                    if (option.value == query.severity) {
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
                    query.statusCalculationStrategy = event.target.value;
                    load(query.strategy);
                };

                var alarmOption = L.DomUtil.create('option', '', strategyList);
                alarmOption.innerHTML = "Alarms";
                alarmOption.value = "Alarms";
                if (alarmOption.value == query.statusCalculationStrategy) {
                    alarmOption.selected = "selected";
                }

                var outageOption = L.DomUtil.create('option', '', strategyList);
                outageOption.innerHTML = "Outages";
                outageOption.value = "Outages";
                if (outageOption.value == query.statusCalculationStrategy) {
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
        }

        // create map
        theMap = L.map(mapId, {
            zoom: 1,
            maxZoom: 15,
            zoomControl: false
        });
        theMap.setView([34.5133, -94.1629]); // center of earth

        // TODO MVR load dynamically from server
        // add tile layer
        L.tileLayer('http://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: 'Map data &copy; <a tabindex="-1" target="_blank" href="http://openstreetmap.org/copyright">OpenStreetMap</a> contributors under <a tabindex="-1" target="_blank" href="http://opendatacommons.org/licenses/odbl/">ODbL</a>, <a tabindex="-1" target="_blank" href="http://creativecommons.org/licenses/by-sa/2.0/">CC BY-SA 2.0</a>',
            // TODO MVR add dynamic
        }).addTo(theMap);

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

        load();
    };

    return {
        render: render
    }
}();
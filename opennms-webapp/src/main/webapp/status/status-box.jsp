<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page language="java" contentType="text/html" session="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    final String graphs = System.getProperty("org.opennms.statusbox.elements", "business-services,nodes-by-alarms,nodes-by-outages");
    final String[] graphKeys = graphs.split(",");
    final List<String> graphKeyList = new ArrayList<>();
    for (String eachGraphKey : graphKeys) {
        if (eachGraphKey != null && !eachGraphKey.isEmpty()) {
            graphKeyList.add("'" + eachGraphKey + "'");
        }
    }
    String javaScriptArrayContent = String.join(", ", graphKeyList);
%>

<link href="lib/c3/c3.css" rel="stylesheet" type="text/css">
<style>

    #chart-content {
    }

    .c3 .c3-chart-arc path {
        stroke-width: 0px;
    }

    .row-centered {
        text-align:center;
    }
    .col-centered {
        display:inline-block;
        float:none;
        /* reset the text-align */
        text-align:left;
        /* inline-block space fix */
        margin-right:-4px;
    }

</style>


<div id="status-overview-box" class="panel panel-default fix-subpixel" style="display: none;">
    <div class="panel-heading">
        <h3 class="panel-title">Status Overview</h3>
    </div>
    <div class="panel-body">
        <div id="chart-content" class="row row-centered">
        </div>
    </div>
</div>

<script type="application/javascript">

    require(["d3", "c3", "jquery"], function(d3, c3, $) {

        // the color palette to map each severity to
        var colorPalette = {
            'Normal': '#336600',
            'Warning': '#ffcc00',
            'Minor': '#ff9900',
            'Major': '#ff3300',
            'Critical': '#cc0000'
        };

        // the size of each donut
        var donutSize = {
            // Don't set size to allow resizing automatically based on space available
            width: 0,
            height: 0
        };

        var loadChartData = function(graph) {
            $.getJSON(graph.url, function (data) {
                var columns = [];

                // Only include values > 0
                for (var i=0; i<data.length; i++) {
                    if (data[i].length >= 2) {
                        if (data[i][1] > 0) {
                            columns.push(data[i]);
                        }
                    }
                }

                // Decide to show or hide the graph
                var sum = 0;
                for (var i=0; i<data.length; i++) {
                    sum += data[i][1];
                }
                if (sum > 0) {
                    // The first chart with data shows the box
                    $("#status-overview-box").show();
                    $("#" + graph.id).parent().show();

                    // Generate graph
                    var chart = c3.generate({
                        bindto: "#" + graph.id,
                        size: donutSize,
                        data: {
                            order: null,
                            columns: [],
                            colors: colorPalette,
                            type : 'donut',
                            onclick: graph.onclick
                        },
                        donut: {
                            title: graph.title,
                            label: {
                                format: function(value, id, ratio) {
                                    return value;
                                }
                            }
                        }
                    });
                    chart.load({columns: columns});

                    // Add graph tooltip
                    var description = graph.description || graph.title || '';
                    if (description !== '') {
                        d3.select("#" + graph.id)
                            .select(".c3-chart")
                            .append("svg:title")
                            .text(description);
                    }
                }
            });
        };

        var render = function(options) {
            var graphs = options.graphs;
            if (graphs === undefined || graphs === null || graphs.length == 0) {
                return;
            }
            if (options.parentContainer === undefined || graphs.parentContainer === null) {
                return;
            }
            if ($(options.parentContainer) === undefined) {
                return;
            }

            for (var i=0; i < graphs.length; i++) {
                // Gather options to draw graph
                var graph = graphs[i];

                // Skip the entry when any of the required fields are missing
                if (graph.id === undefined || graph.id === null || graph.id === "") {
                    return;
                }
                if (graph.url === undefined || graph.url === null || graph.url === "") {
                    return;
                }

                // create container for graph if it does not exist yet
                if ($("#" + graph.id).length == 0) {
                    var graphContainer = $('<div/>', {
                        class: 'col-centered col-xs-12 col-sm-6 col-md-6 col-lg-4'
                    });
                    graphContainer.append($("<div></div>", {
                        id: graph.id
                    }));
                    graphContainer.hide();
                    $(options.parentContainer).append(graphContainer);
                }

                // load data and populate graph
                loadChartData(graph);
            };
        };

        $("document").ready(function() {
            // all supported graphs
            var graphDefinitions = {
                'business-services': {
                    id: "businessServiceProblemChart",
                    title: "Business Services",
                    description: "Business Services Status Overview",
                    url: "api/v2/status/summary/business-services",
                    onclick: function (e) {
                        window.location = "status/index.jsp?title=Business Service List&type=business-services&severityFilter=" + e.id;
                    }
                },

                'applications': {
                    id: "applicationProblemChart",
                    title: "Applications",
                    description: "Applications Status Overview",
                    url: "api/v2/status/summary/applications",
                    onclick: function (e) {
                        window.location = "status/index.jsp?title=Application List&type=applications&severityFilter=" + e.id;
                    }
                },

                'nodes-by-alarms': {
                    id: "nodeProblemChartsByAlarms",
                    title: "Alarms",
                    description: "Nodes grouped by unacknowledged Alarms",
                    url: "api/v2/status/summary/nodes/alarms",
                    onclick: function (e) {
                        window.location = "status/index.jsp?title=Node List&type=nodes&strategy=alarms&severityFilter=" + e.id;
                    }
                },
                'nodes-by-outages': {
                    id: "nodeProblemChartByOutages",
                    title: "Outages",
                    description: "Nodes grouped by current Outages",
                    url: "api/v2/status/summary/nodes/outages",
                    onclick: function (e) {
                        window.location = "status/index.jsp?title=Node List&type=nodes&strategy=outages&severityFilter=" + e.id;
                    }
                }
            };

            // the graphs to render
            var graphKeys = [ <%= javaScriptArrayContent %> ];
            var graphs = [];
            for (var i=0; i<graphKeys.length; i++) {
                var graphKey = graphKeys[i];
                if (graphKey != undefined && graphKey in graphDefinitions) {
                    graphs.push(graphDefinitions[graphKey]);
                }
            }

            // only render if something is configured to show
            if (graphs.length != 0) {
                render({
                    parentContainer: '#chart-content',
                    graphs: graphs
                })
            }
        });
    });

</script>
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

<%@page language="java" contentType="text/html" session="true" %>

<link href="/opennms/lib/c3/c3.css" rel="stylesheet" type="text/css">
<style>

    #chart-content {
    }

    .no-padding {
        padding: 0px;
    }

    .c3 .c3-chart-arc path {
        stroke-width: 0px;
    }
</style>


<div class="panel panel-default fix-subpixel">
    <div class="panel-heading">
        <h3 class="panel-title">Status Overview</h3>
    </div>
    <div class="panel-body">
        <div>
            <div id="chart-content" class="row" stlye="display:flex; justify-content: space-around">
            </div>
        </div>
    </div>
</div>

<script type="application/javascript">

    require(["d3", "c3", "jquery"], function(d3, c3, $) {

        var chartMapping = [];

        var severityIds = {
            'Normal': 3,
            'Warning': 4,
            'Minor': 5,
            'Major': 6,
            'Critical': 7
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
                console.log(columns);

                // Reload graph
                chartMapping[graph.id].unload();
                chartMapping[graph.id].load({columns: columns});

                // Decide wether to show or hide the graph
                var sum = 0;
                for (var i=0; i<data.length; i++) {
                    sum += data[i][1];
                }
                if (sum == 0) {
                    $("#" + graph.id).parent().hide();
                } else {
                    $("#" + graph.id).parent().show();
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

                // create container for graphif it does not exist yet
                if ($("#" + graph.id).length == 0) {
                    var graphContainer = $('<div/>', {
                        class: 'no-padding col-lg-3 col-md-4 col-xs-6',
                    });
                    graphContainer.append($("<div></div>", {
                        id: graph.id
                    }));
                    $(options.parentContainer).append(graphContainer);
                    $("#" + graph.id).parent().hide();

                    // Generate graph
                    var chart = c3.generate({
                        bindto: "#" + graph.id,
                        size: {
                            width: 250,
                            height: 250
                        },
                        data: {
                            order: null,
                            columns: [],
                            colors: {
                                'Normal': '#336600',
                                'Warning': '#ffcc00',
                                'Minor': '#ff9900',
                                'Major': '#ff3300',
                                'Critical': '#cc0000'
                            },
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
                    chartMapping[graph.id] = chart;
                }

                // load data and populate graph
                loadChartData(graph);
            };
        };

        $("document").ready(function() {
            render({
                parentContainer: '#chart-content',
                graphs: [
                    {
                        id: "nodeProblemChartsByAlarms",
                        title: "Alarms",
                        url: "/opennms/api/v2/status/summary/nodes/alarms",
                        onclick: function(e) {
                            window.location = "node/index.jsp?type=alarms&_s=severity%3D%3D" + e.id;
                        }
                    },
                    {
                        id: "nodeProblemChartByOutages",
                        title: "Outages",
                        url: "/opennms/api/v2/status/summary/nodes/outages",
                        onclick: function(e) {
                            window.location = "node/index.jsp?type=outages&_s=severity%3D%3D" + e.id;
                        }
                    },
                    {
                        id: "applicationProblemChart",
                        title: "Applications",
                        url: "/opennms/api/v2/status/summary/applications",
                        onclick: function(e) {
                            window.location = "application/index.jsp?_s=severity%3D%3D" + e.id;
                        }
                    },
                    {
                        id: "businessServiceProblemChart",
                        title: "Business Services",
                        url: "/opennms/api/v2/status/summary/business-services",
                        onclick: function(e) {
                            window.location = "bsm/index.jsp?_s=severity%3D%3D" + e.id;
                        }
                    }
                ]
            })
        });

    });

</script>
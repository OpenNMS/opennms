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
    .c3 {
        float: left;
    }
    .c3-chart-arc path {
        stroke-width: 0px;
    }
</style>

<div class="panel panel-default fix-subpixel">
    <div class="panel-heading">
        <h3 class="panel-title">Status Overview</h3>
    </div>
    <div class="panel-body">
        <div id="nodeProblemChart" class="status-box" data-url="/opennms/rest/status-box/nodes" data-title="Alarms">
        </div>

        <div id="outageProblemChart" class="status-box" data-url="/opennms/rest/status-box/outages" data-title="Outages">
        </div>

        <div id="applicationProblemChart" class="status-box" data-url="/opennms/rest/status-box/applications" data-title="Applications">
        </div>

        <div id="businessServiceProblemChart" class="status-box" data-url="/opennms/rest/status-box/business-services" data-title="Business Services">
        </div>
    </div>
</div>

<script type="application/javascript">

    require(["d3", "c3", "jquery"], function(d3, c3, $) {
        $("document").ready(function() {
            $(".status-box").each(function() {
                // Gather options to draw graph
                var el = $(this);
                var def = {
                    'id': el.attr("id"),
                    'url': el.data("url"),
                    'title': el.data("title"),
                };

                // Skip the entry when any of the required fields are missing
                if (def.id === undefined || def.id === null || def.id === "") {
                    return;
                }
                if (def.url === undefined || def.url === null || def.url === "") {
                    return;
                }

                // Generate graph
                var chart = c3.generate({
                    bindto: "#" + def.id,
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
                    },
                    donut: {
                        title: def.title,
                        label: {
                            format: function (value, id, ratio) {
                                return value;
                            }
                        }
                    }
                });

                // when ready load data and populate graph
                $("#" + def.id).ready(function () {
                    var url = def.url;
                    $.getJSON(url, function (data) {
                        chart.unload();
                        chart.load({columns: data});
                    });
                });
            });
        });
    });

</script>
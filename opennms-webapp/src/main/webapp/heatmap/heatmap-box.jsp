<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%--
  This page is included by other JSPs to create a box containing a treemap/heatmap of
  outages grouped by nodes, foreignSources or categories.

  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    String mode = System.getProperty("org.opennms.heatmap.defaultMode", "alarms");
    String title = "";
    String heatmap = System.getProperty("org.opennms.heatmap.defaultHeatmap", "categories");
    String foreignSource = null;
    String category = null;

    String url = "/opennms/rest/heatmap/";

    if (request.getParameterMap().containsKey("mode")) {
        mode = request.getParameter("mode");
    }

    if (request.getParameterMap().containsKey("heatmap")) {
        heatmap = request.getParameter("heatmap");
    }

    if ("alarms".equals(mode)) {
        title = "Alarm Heatmap ";
    } else {
        title = "Outage Heatmap ";
    }

    url += mode + "/" + heatmap + "/";

    if ("nodesByForeignSource".equals(heatmap)) {
        foreignSource = request.getParameter("foreignSource");
        url += foreignSource;
        title += " (Nodes by ForeignSource '" + foreignSource + "')";
    }

    if ("nodesByCategory".equals(heatmap)) {
        category = request.getParameter("category");
        url += category;
        title += " (Nodes by Category '" + category + "')";
    }

    if ("foreignSources".equals(heatmap)) {
        title += " (by ForeignSources)";
    }

    if ("categories".equals(heatmap)) {
        title += " (by Categories)";
    }
%>

<div id="heatmap-box" class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title"><a href="heatmap/index.jsp?mode=<%=mode%>&heatmap=<%=heatmap%>&foreignSource=<%=foreignSource%>&category=<%=category%>"><%=title%>
        </a></h3>
    </div>

    <script type="text/javascript" src="/opennms/js/jquery/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" src="/opennms/js/jquery/ui/jquery-ui-1.8.2.custom.js"></script>
    <script type="text/javascript" src="/opennms/js/jquery.ui.treemap.js"></script>

    <div id="treemap"></div>

    <script type="text/javascript">
        var mouseclickHandler = function (e, data) {
            var nodes = data.nodes;
            var ids = data.ids;
            <%
              if ("foreignSources".equals(heatmap)) {
            %>
            location.href = "<%=request.getRequestURI()%>?mode=<%=mode%>&heatmap=nodesByForeignSource&foreignSource=" + nodes[0].id;
            <%
              }

              if ("categories".equals(heatmap)) {
            %>
            location.href = "<%=request.getRequestURI()%>?mode=<%=mode%>&heatmap=nodesByCategory&category=" + nodes[0].id;
            <%
              }

              if ("nodesByCategory".equals(heatmap) || "nodesByForeignSource".equals(heatmap)) {
            %>
            location.href = "/opennms/element/node.jsp?node=" + nodes[0].elementId
            <%
              }
            %>
        };

        var url = "<%=url%>";
        var children;

        function refresh() {
            $("#treemap").treemap({
                "dimensions": [
                    $("#treemap").width(),
                    Math.min($(window).height() - 230, $("#treemap").width())
                ],
                "colorStops": [
                    {"val": 1.0, "color": "#CC0000"},
                    {"val": 0.4, "color": "#FF3300"},
                    {"val": 0.2, "color": "#FF9900"},
                    {"val": 0.1, "color": "#FFCC00"},
                    {"val": 0.0, "color": "#336600"}
                ],
                "labelsEnabled": true,
                "nodeData": {
                    "id": "<%=heatmap%>",
                    "children": children
                }
            }).bind('treemapclick', mouseclickHandler);
        }

        $( window ).resize(function() {
            refresh();
        });

        $(document).ready(function () {
            $.getJSON(url, function (data) {
                children = data.children;
                refresh();
            });
        });
    </script>
    <div class="panel-footer">
        <span class="glyphicon glyphicon-retweet" aria-hidden="true"></span>&nbsp;
        <%
            if ("outages".equals(mode)) {
        %>
        <a href="<%=request.getRequestURI()%>?mode=alarms&heatmap=<%=heatmap%>&category=<%=category%>&foreignSource=<%=foreignSource%>">Alarms</a> / <b>Outages</b>
        <%
            } else {
        %>
        <b>Alarms</b> / <a href="<%=request.getRequestURI()%>?mode=outages&heatmap=<%=heatmap%>&category=<%=category%>&foreignSource=<%=foreignSource%>">Outages</a>
        <%
            }
        %>
        &nbsp;<span class="glyphicon glyphicon-retweet" aria-hidden="true"></span>&nbsp;
        <%
            if ("foreignSources".equals(heatmap) || "nodesByForeignSource".equals(heatmap)) {
        %>
        <a href="<%=request.getRequestURI()%>?mode=<%=mode%>&heatmap=categories">Categories</a> / <b>Foreign Sources</b>
        <%
            } else {
        %>
        <b>Categories</b> / <a href="<%=request.getRequestURI()%>?mode=<%=mode%>&heatmap=foreignSources">Foreign Sources</a>
        <%
            }
        %>
        <div style="float:right;">
            <%
                if ("outages".equals(mode)) {
            %>
            <font color="#336600"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;0% down
            <font color="#FFCC00"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;10% down
            <font color="#FF9900"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;20% down
            <font color="#FF3300"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;40% down
            <font color="#CC0000"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;100% down
            <%
                } else {
            %>
            <font color="#336600"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;Normal
            <font color="#FFCC00"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;Warning
            <font color="#FF9900"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;Minor
            <font color="#FF3300"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;Major
            <font color="#CC0000"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span></font>&nbsp;Critical
            <%
                }
            %>
        </div>
    </div>
</div>

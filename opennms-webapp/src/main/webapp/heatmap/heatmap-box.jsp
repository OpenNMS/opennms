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

<div id="heatmap-box" class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Availability</h3>
    </div>

    <script type="text/javascript" src="/opennms/js/jquery/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" src="/opennms/js/jquery/ui/jquery-ui-1.8.2.custom.js"></script>
    <script type="text/javascript" src="/opennms/js/jquery.ui.treemap.js"></script>

    <div id="treemap"></div>

    <script type="text/javascript">
        <%
          String heatmap = "foreignSources";
          String foreignSource = null;
          String category = null;

          String url = "/opennms/rest/heatmap/";

          if (request.getParameterMap().containsKey("heatmap")) {
            heatmap = request.getParameter("heatmap");
            url += heatmap + "/";
          }

          if ("nodesByForeignSource".equals(heatmap)) {
            foreignSource = request.getParameter("foreignSource");
            url += foreignSource;
          }

          if ("nodesByCategory".equals(heatmap)) {
            category = request.getParameter("category");
            url += category;
          }

        %>

        var mouseclickHandler = function (e, data) {
            var nodes = data.nodes;
            var ids = data.ids;
            <%
              if ("foreignSources".equals(heatmap)) {
            %>
            location.href = "<%=request.getRequestURI()%>?heatmap=nodesByForeignSource&foreignSource=" + nodes[0].id;
            <%
              }

              if ("categories".equals(heatmap)) {
            %>
            location.href = "<%=request.getRequestURI()%>?heatmap=nodesByCategory&category=" + nodes[0].id;
            <%
              }
              
              if ("nodesByCategory".equals(heatmap) || "nodesByForeignSource".equals(heatmap)) {
            %>
            location.href = "/opennms/element/node.jsp?node="+nodes[0].nodeId
            <%
              }
            %>
        };

        var url = "<%=url%>";

        $.getJSON(url, function (data) {
            $(document).ready(function () {
                $("#treemap").treemap({
                    "dimensions": [
                        $("#treemap").width(),
                        $(document).height() - 220
                    ],
                    "colorStops": [
                        {"val": 1.0, "color": "#CC0000"},
                        {"val": 0.5, "color": "#FFCC00"},
                        {"val": 0.0, "color": "#336600"}
                    ],
                    "labelsEnabled": true,
                    "nodeData": {
                        "id": "<%=heatmap%>",
                        "children": data.children
                    }

                }).bind('treemapclick', mouseclickHandler);
            });
        });
    </script>
</div>
[<a href="<%=request.getRequestURI()%>?heatmap=foreignSources">foreingSources</a>] &nbsp; [<a href="<%=request.getRequestURI()%>?heatmap=categories">categories</a>]

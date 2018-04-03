<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@ page import="org.opennms.web.api.Util" %><%--
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
    String monitoredService = null;

    String url = "/opennms/rest/heatmap/";

    if (request.getParameterMap().containsKey("mode")) {
        mode = request.getParameter("mode");
    }

    if (request.getParameterMap().containsKey("heatmap")) {
        heatmap = request.getParameter("heatmap");
    }

    if ("services".equals(heatmap)) {
        heatmap = "monitoredServices";
    }

    if ("alarms".equals(mode)) {
        title = "Alarm Heatmap ";
    } else {
        if ("outages".equals(mode)) {
            title = "Outage Heatmap ";
        } else {
            title = "Service Heatmap ";
        }
    }

    url += Util.encode(mode) + "/" + Util.encode(heatmap) + "/";

    if ("nodesByForeignSource".equals(heatmap)) {
        foreignSource = request.getParameter("foreignSource");
        url += Util.encode(foreignSource);
        title += " (Nodes by ForeignSource '" + foreignSource + "')";
    }

    if ("nodesByCategory".equals(heatmap)) {
        category = request.getParameter("category");
        url += Util.encode(category);
        title += " (Nodes by Category '" + category + "')";
    }

    if ("nodesByMonitoredService".equals(heatmap)) {
        monitoredService = request.getParameter("monitoredService");
        url += Util.encode(monitoredService);
        title += " (Nodes by Service '" + monitoredService + "')";
    }

    if ("foreignSources".equals(heatmap)) {
        title += " (by ForeignSources)";
    }

    if ("categories".equals(heatmap)) {
        title += " (by Categories)";
    }

    if ("monitoredServices".equals(heatmap)) {
        title += " (by Services)";
    }
%>

<style type="text/css">
    .tooltipbox {
        border: 1px solid #aaa;
        position: absolute;
        background-color: rgba(255,255,255,0.85);
        pointer-events: none;
        display: none;
    }
    .tooltipbox p {
        margin: 0px 2px 0px 2px;
        padding: 0px 2px 0px 2px;
        font-size: 0.8em;
        color: #444;
        white-space: nowrap;
    }
</style>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="jquery-ui-js" />
</jsp:include>

<div id="heatmap-box" class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title"><a href="heatmap/index.jsp?mode=<%=Util.encode(mode)%>&amp;heatmap=<%=Util.encode(heatmap)%>&amp;foreignSource=<%=foreignSource==null?"":Util.encode(foreignSource)%>&amp;category=<%=category==null?"":Util.encode(category)%>&amp;monitoredService=<%=monitoredService==null?"":Util.encode(monitoredService)%>"><%=WebSecurityUtils.sanitizeString(title)%>
        </a></h3>
    </div>

    <div id="treemap" style="position: relative;">
        <div id="tooltipbox" class="tooltipbox"></div>
    </div>

    <script type="text/javascript">

        function addOnLoadEvent(funcToAdd) {
            var oldOnLoadFunction = window.onload;
            if (typeof window.onload != 'function') {
                window.onload = funcToAdd;
            } else {
                window.onload = function() {
                    if (oldOnLoadFunction) {
                        oldOnLoadFunction();
                    }
                    funcToAdd();
                }
            }
        }

        addOnLoadEvent(function() {
            var tooltipDelay = 500;
            var tooltipFadeIn = 500;
            var tooltipTimeout;

            function clearAndHideTooltip() {
                clearTimeout(tooltipTimeout);
                $(".tooltipbox").hide();
            }

            var mousemoveHandler = function(e, data) {
                clearAndHideTooltip();

                $("#tooltipbox").html("<span><p><b>" + data.nodes[0].id + "</b></p></span>");

                var tooltipWidth = $("#tooltipbox").width();
                var tooltipHeight = $("#tooltipbox").height();
                var treemapWidth = $("#treemap").width();
                var treemapHeight = $("#treemap").height();

                if (e.offsetX + tooltipWidth + 16 > treemapWidth) {
                  $("#tooltipbox").css({ "left" : e.offsetX - tooltipWidth - 8 });
                } else {
                  $("#tooltipbox").css({ "left" : e.offsetX + 16 });
                }

                if (e.offsetY + tooltipHeight > treemapHeight) {
                  $("#tooltipbox").css({ "top" : e.offsetY - tooltipHeight });
                } else {
                  $("#tooltipbox").css({ "top" : e.offsetY });
                }

                tooltipTimeout = setTimeout(function(event){
                    $("#tooltipbox").fadeIn(tooltipFadeIn);
                }, tooltipDelay);
            };

            var mouseleaveHandler = function (e, data) {
                clearAndHideTooltip();
            }


            var mouseclickHandler = function (e, data) {
                var nodes = data.nodes;
                var ids = data.ids;
                <%
                  if ("foreignSources".equals(heatmap)) {
                %>
                location.href = "<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&heatmap=nodesByForeignSource&foreignSource=" + encodeURIComponent(nodes[0].id);
                <%
                  }

                  if ("categories".equals(heatmap)) {
                %>
                location.href = "<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&heatmap=nodesByCategory&category=" + encodeURIComponent(nodes[0].id);
                <%
                  }

                  if ("monitoredServices".equals(heatmap)) {
                %>
                location.href = "<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&heatmap=nodesByMonitoredService&monitoredService=" + encodeURIComponent(nodes[0].id);
                <%
                  }

                  if ("nodesByCategory".equals(heatmap) || "nodesByForeignSource".equals(heatmap) || "nodesByMonitoredService".equals(heatmap)) {
                %>
                location.href = "/opennms/element/node.jsp?node=" + encodeURIComponent(nodes[0].elementId);
                <%
                  }
                %>
            };

            var url = "<%=url%>";
            var children;

            function refresh() {
                clearAndHideTooltip();

                var height = $(window).height() - 105 - $("#treemap").offset().top;

                if (height < 0) {
                    height = $(window).width();
                }

                height = Math.min(height, $(window).width());

                $("#treemap").treemap({
                    "dimensions": [
                        $("#treemap").width(),
                        height
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
                        "id": "<%=Util.encode(heatmap)%>",
                        "children": children
                    }
                }).bind('treemapmousemove', mousemoveHandler)
                  .bind('treemapclick', mouseclickHandler)
                  .mouseleave(function(){mouseleaveHandler()});
            }

            $(window).resize(function() {
                refresh();
            });

            $(document).ready(function () {
                $.getJSON(url, function (data) {
                    children = data.children;
                    refresh();
                });
            });
        });
    </script>
    <div class="panel-footer">
        <div class="row">
            <div class="col-sm-7 col-md-7" style="padding-right: 0 !important">
                <span class="text-nowrap">
                    <span class="glyphicon glyphicon-retweet" aria-hidden="true"></span>&nbsp;
                    <%
                        if ("outages".equals(mode)) {
                    %>
                    <a href="<%=request.getRequestURI()%>?mode=alarms&amp;heatmap=<%=Util.encode(heatmap)%>&amp;category=<%=category==null?"":Util.encode(category)%>&amp;foreignSource=<%=foreignSource==null?"":Util.encode(foreignSource)%>&amp;monitoredService=<%=monitoredService==null?"":Util.encode(monitoredService)%>">Alarms</a> / <b>Outages</b>
                    <%
                    } else {
                    %>
                    <b>Alarms</b> / <a href="<%=request.getRequestURI()%>?mode=outages&amp;heatmap=<%=Util.encode(heatmap)%>&amp;category=<%=category==null?"":Util.encode(category)%>&amp;foreignSource=<%=foreignSource==null?"":Util.encode(foreignSource)%>&amp;monitoredService=<%=monitoredService==null?"":Util.encode(monitoredService)%>">Outages</a>
                    <%
                        }
                    %>
                </span>
                &nbsp;
                <span class="text-nowrap">
                    <span class="glyphicon glyphicon-retweet" aria-hidden="true"></span>&nbsp;
                    <%
                        if ("foreignSources".equals(heatmap) || "nodesByForeignSource".equals(heatmap)) {
                    %>
                    <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=categories">Categories</a> / <b>Foreign Sources</b> / <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=monitoredServices">Services</a>
                    <%
                    } else {
                        if ("categories".equals(heatmap) ||"nodesByCategory".equals(heatmap)) {
                    %>
                    <b>Categories</b> / <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=foreignSources">Foreign Sources</a> / <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=monitoredServices">Services</a>
                    <%
                    } else {
                    %>
                    <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=categories">Categories</a> / <a href="<%=request.getRequestURI()%>?mode=<%=Util.encode(mode)%>&amp;heatmap=foreignSources">Foreign Sources</a> / <b>Services</b>
                    <%
                            }
                        }
                    %>
                </span>
            </div>
            <div class="col-sm-5 col-md-5 text-right" style="padding-left: 0 !important">
                <%
                    if ("outages".equals(mode)) {
                %>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#336600"></span>&nbsp;0% down</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FFCC00"></span>&nbsp;10% down</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FF9900"></span>&nbsp;20% down</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FF3300"></span>&nbsp;40% down</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#CC0000"></span>&nbsp;100% down</span>
                <%
                } else {
                %>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#336600"></span>&nbsp;Normal</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FFCC00"></span>&nbsp;Warning</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FF9900"></span>&nbsp;Minor</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#FF3300"></span>&nbsp;Major</span>
                <span class="text-nowrap"><span class="glyphicon glyphicon-th-large" aria-hidden="true" style="color:#CC0000"></span>&nbsp;Critical</span>
                <%
                    }
                %>
            </div>
        </div>
    </div>
</div>

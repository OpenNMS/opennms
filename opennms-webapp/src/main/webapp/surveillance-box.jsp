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

<%@page language="java" contentType="text/html" session="true" %>

<%--
/*******************************************************************************
 * Check org.opennms.dashboard.implementation for selected implementation      *
 *******************************************************************************/
--%>

<%
    String dashboardImplementation = System.getProperty("org.opennms.dashboard.implementation", "vaadin").trim();

    if (!"gwt".equals(dashboardImplementation)) {
%>

    <%--
    /*******************************************************************************
     * Include VAADIN implementation                                               *
     *******************************************************************************/
    --%>

    <%
        String viewName = "";

        if (request.getParameterMap().containsKey("viewName")) {
            viewName = "&viewName=" + request.getParameter("viewName");
        }
    %>

    <script type="text/javascript">

        var isInitialized = false;
        var checkInterval = setInterval(checkIframe, 1000);

        function checkIframe(){

            var iframe = document.getElementById("surveillance-iframe");

            iframe.contentWindow.postMessage("test", window.location.origin);
            if(isInitialized){
                clearInterval(checkInterval);
            }
        }

        function receiveMessage(event){
            isInitialized = true;
            if(event.origin !== window.location.origin)
                return;

            var elem = document.getElementById("surveillance-view");
            elem.style.height = event.data;
        }

        window.addEventListener("message", receiveMessage, false);

    </script>

    <div id="surveillance-view">
    <iframe id="surveillance-iframe" src="osgi/vaadin-surveillance-views?dashboard=false<%= viewName %>" frameborder="0" style="min-height:100%; min-width:100%;"></iframe>
    </div>

<% } else { %>

    <%--
    /*******************************************************************************
     * Include GWT implementation                                                  *
     *******************************************************************************/
    --%>

    <meta name='gwt:module' content='org.opennms.dashboard.Dashboard' />
    <link media="screen" href="css/dashboard.css" type="text/css" rel="stylesheet">
    <script type="text/javascript" src='dashboard/dashboard.nocache.js'></script>
    <table class="dashboard" cellspacing="5" width="100%">
        <tbody>
        <tr>
            <td class="dashletCell"id="surveillanceView"></td>
        </tr>
        </tbody>
    </table>

<% } %>
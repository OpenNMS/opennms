<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
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

<%--
  This page is included by other JSPs to create a box containing a list of grafana dashboards.

  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.net.URI,
                org.apache.commons.io.IOUtils,
                org.apache.http.client.config.RequestConfig,
                org.apache.http.client.utils.URIBuilder,
                org.apache.http.client.methods.HttpGet,
                org.apache.http.client.methods.CloseableHttpResponse,
                org.apache.http.HttpEntity,
                org.apache.http.impl.client.CloseableHttpClient,
                org.apache.http.impl.client.HttpClients" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    int grafanaDashboadLimit = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.dashboardLimit", "0"));
%>

<c:if test="${param.useLimit != 'true'}">
<% grafanaDashboadLimit = 0; %>
</c:if>
<%
    String grafanaApiKey = System.getProperty("org.opennms.grafanaBox.apiKey", "");
    String grafanaProtocol = System.getProperty("org.opennms.grafanaBox.protocol", "http");
    String grafanaHostname = System.getProperty("org.opennms.grafanaBox.hostname", "localhost");
    String grafanaTag = System.getProperty("org.opennms.grafanaBox.tag", "");
    String grafanaBasePath = System.getProperty("org.opennms.grafanaBox.basePath", "");
    int grafanaPort = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.port", "3000"));
    int grafanaConnectionTimeout = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.connectionTimeout", "500"));
    int grafanaSoTimeout = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.soTimeout", "500"));
    String errorMessage = null;
    String responseString = null;

    if (!grafanaBasePath.startsWith("/")) {
        grafanaBasePath = "/" + grafanaBasePath;
    }

    if (!"".equals(grafanaApiKey)
            && !"".equals(grafanaHostname)
            && !"".equals(grafanaProtocol)
            && ("http".equals(grafanaProtocol) || "https".equals(grafanaProtocol))) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(grafanaConnectionTimeout)
                    .setSocketTimeout(grafanaSoTimeout)
                    .build();
            URI uri = new URIBuilder()
                    .setScheme(grafanaProtocol)
                    .setHost(grafanaHostname)
                    .setPort(grafanaPort)
                    .setPath(grafanaBasePath + "/api/search/")
                    .build();

            /**
             * Setting the timeouts to assure that the landing page will be loaded. Making the
             * call via JS isn't possible due to CORS-related problems with the Grafana server.
             */
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("Authorization", "Bearer " + grafanaApiKey);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                HttpEntity httpEntity = httpResponse.getEntity();
                if (httpEntity != null) {
                    responseString = IOUtils.toString(httpEntity.getContent(), "UTF-8");
                }
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
    } else {
        errorMessage = "Invalid configuration";
    }
%>

<div id="grafana-box" class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Grafana Dashboards</h3>
    </div>

    <div id="dashboardlist" class="panel-body">
        <%
            if (responseString != null) {
        %>
        <script type="text/javascript">
            var grafanaTag = '<%=grafanaTag%>';
            var obj = <%=responseString%>;
            var limit = <%=grafanaDashboadLimit%>;
            var count = 0;

            for (var val of obj) {
                var showDashboard = true;

                if (grafanaTag != '') {
                    showDashboard = false;

                    for (var tag of val['tags']) {
                        if (grafanaTag == tag) {
                            showDashboard = true;
                            break;
                        }
                    }
                }
                if (showDashboard) {
                    if (limit < 1 || count++ < limit) {
                        $('#dashboardlist').append('<a href="<%=grafanaProtocol%>://<%=grafanaHostname%>:<%=grafanaPort%><%=grafanaBasePath%>/dashboard/' + val['uri'] + '"><span class="glyphicon glyphicon-signal" aria-hidden="true"></span>&nbsp;' + val['title'] + "</a><br/>");
                    }
                }
            };
            if (limit > 0 && count > limit) {
                $('#dashboardlist').append('<a href="graph/grafana.jsp"><span class="glyphicon glyphicon-th-list" aria-hidden="true"></span>&nbsp;View list of all Dashboards</a><br/>');
            }
        </script>
        <%
            } else {
        %>
        <span class="glyphicon glyphicon-wrench" aria-hidden="true"></span>&nbsp;<%=errorMessage%><br/>
        <%
            }
        %>
    </div>
</div>

<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

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
    String grafanaLinkProtocol = System.getProperty("org.opennms.grafanaBox.link.protocol", grafanaProtocol);
    String grafanaLinkHostname = System.getProperty("org.opennms.grafanaBox.link.hostname", grafanaHostname);
    String grafanaLinkBasePath = System.getProperty("org.opennms.grafanaBox.link.basePath", "");
    int grafanaLinkPort = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.link.port", Integer.toString(grafanaPort)));

    String errorMessage = null;
    String responseString = null;

    if (!grafanaBasePath.startsWith("/")  && !grafanaBasePath.trim().isEmpty()) {
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

<div id="grafana-box" class="card">
    <div class="card-header">
        <span>Grafana Dashboards</span>
    </div>

    <div id="dashboardlist" class="card-body">
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
                        $('#dashboardlist').append('<a href="<%=grafanaLinkProtocol%>://<%=grafanaLinkHostname%>:<%=grafanaLinkPort%><%=grafanaLinkBasePath%>' + val['url'] + '"><span class="fas fa-signal" aria-hidden="true"></span>&nbsp;' + val['title'] + "</a><br/>");
                    }
                }
            };
            if (limit > 0 && count > limit) {
                $('#dashboardlist').append('<a href="graph/grafana.jsp"><span class="fas fa-table-list" aria-hidden="true"></span>&nbsp;View list of all Dashboards</a><br/>');
            }
        </script>
        <%
            } else {
        %>
        <span class="fas fa-wrench" aria-hidden="true"></span>&nbsp;<%=errorMessage%><br/>
        <%
            }
        %>
    </div>
</div>

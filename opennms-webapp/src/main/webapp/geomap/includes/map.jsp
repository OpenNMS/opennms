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
<%@page import="org.opennms.web.api.Util" %>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@page language="java" contentType="text/html" session="true" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
  public String getParameter(HttpServletRequest request, String name) {
    return getParameter(request, name, null);
  }

  // Returns request parameter or default if the parameter does not exist
  public String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.isEmpty() && defaultValue != null && !defaultValue.isEmpty()) {
      return defaultValue;
    }
    return value;
  }
%>
<%
  final String baseHref = Util.calculateUrlBase( request );
  final String mapId = getParameter(request, "mapId", "map");
%>

<%--
  geomap-js already contains leaflet-js.
  Including asset leaflet-js caused an issue where the global 'Map' object was being clobbered by
  'Leaflet.Map'.
--%>
<%--
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="leaflet-js" />
</jsp:include>
--%>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="geomap-js" />
</jsp:include>

<div class="geomap" style="height: <%= WebSecurityUtils.sanitizeString(getParameter(request, "height", "400px"))%>">
  <div style="width: 100%; height:100%" id="<%= mapId %>"></div>

  <!-- Template to build the popup for each single marker -->
  <div id="single-popup" class="node-marker-single" style="display:none">
    <h4>Node <a class="node" href="<%= baseHref %>element/node.jsp?node={NODE_ID}">{NODE_LABEL}</a></h4>
    <p>
      <a href="<%= baseHref %>topology?provider=Enhanced+Linkd&focus-vertices={NODE_ID}" target="_blank">View in Topology Map</a>
    </p>
    <p>
      Severity: <span class="severity {SEVERITY_LABEL}"><a href="<%= baseHref %>alarm/list.htm?sortby=id&acktype=unack&limit=20&filter=node%3D{NODE_ID}" target="_blank">{SEVERITY_LABEL}</a></span> <br/>
      Description: {DESCRIPTION} <br/>
      Maint.&nbsp;Contract: {MAINT_CONTRACT} <br/>
      IP Address: {IP_ADDRESS} <br/>
      Categories: {CATEGORIES}
    </p>
  </div>

  <!-- Template to build the popup for each group cluster -->
  <div id="multi-popup" class="node-marker-multiple" style="display:none">
    <h4># of nodes: {NUMBER_NODES} ({NUMBER_UNACKED} Unacknowledged Alarms)</h4>
    <p>
      <a href="<%= baseHref %>topology?provider=Enhanced+Linkd&focus-vertices={NODE_IDS}" target="_blank">View in Topology Map</a>
    </p>
    <p>
      {TABLE_CONTENT}
    </p>
  </div>

  <!-- Template to build the rows for the popup table -->
  <div id="multi-popup-table-row" style="display:none">
    <table class="node-marker-list">
      <tr class="node-marker-{SEVERITY_LABEL}">
        <td class="node-marker-label">
          <a class="node" href="<%= baseHref %>element/node.jsp?node={NODE_ID}">{NODE_LABEL}</a>
        </td>
        <td class="node-marker-ipaddress">
          {IP_ADDRESS}
        </td>
        <td class="node-marker-severity severity {SEVERITY_LABEL}">
          <a href="<%= baseHref %>alarm/list.htm?sortby=id&acktype=unack&limit=20&filter=node%3D{NODE_ID}" target="_blank">{SEVERITY_LABEL}</a>
        </td>
      </tr>
    </table>
  </div>
</div>

<script type="text/javascript">
$('<%= mapId %>').ready(function() {
    geomap.render({
        baseHref: "<%= baseHref %>",
        mapId: "<%= mapId %>",
        hideControlsOnStartup: <%= WebSecurityUtils.sanitizeString(getParameter(request, "hideControlsOnStartup")) %> ,
        strategy: "<%= WebSecurityUtils.sanitizeString(getParameter(request, "strategy")) %>" ,
        severity: "<%= WebSecurityUtils.sanitizeString(getParameter(request, "severity")) %>"
    })
});
</script>


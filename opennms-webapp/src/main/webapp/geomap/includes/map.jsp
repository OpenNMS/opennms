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
<%@page import="org.opennms.web.api.Util" %>
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

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="leaflet-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="geomap-js" />
</jsp:include>

<div class="geomap" style="height: <%= getParameter(request, "height", "400px")%>">
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
      IP Address:  <br/>
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
        hideControlsOnStartup: <%= getParameter(request, "hideControlsOnStartup") %> ,
        strategy: "<%= getParameter(request, "strategy") %>" ,
        severity: "<%= getParameter(request, "severity") %>"
    })
});
</script>


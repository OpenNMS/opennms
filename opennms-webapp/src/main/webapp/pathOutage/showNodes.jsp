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

<%@page language="java" contentType="text/html" session="true"
	import="java.sql.Connection,
			java.util.List,
			java.util.Set,
			org.opennms.core.db.DataSourceFactory,
			org.opennms.core.utils.DBUtils,
			org.opennms.netmgt.dao.api.PathOutageManager,
			org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Path Outage Nodes" />
  <jsp:param name="headTitle" value="Path Outage Nodes" />
  <jsp:param name="breadcrumb" value="<a href=&quot;pathOutage/index.jsp&quot;>Path Outages</a>" />
  <jsp:param name="breadcrumb" value="Nodes" />
</jsp:include>

<% 
      String critIp = request.getParameter("critIp");
      String critSvc = request.getParameter("critSvc");
      PathOutageManager pathOutageManager = PathOutageManagerDaoImpl.getInstance();
      String[] pthData = pathOutageManager.getCriticalPathData(critIp, critSvc);
      Set<Integer> nodeList = pathOutageManager.getNodesInPath(critIp, critSvc);
%>
  
<div class="panel panel-default fix-subpixel">
    <div class="panel-heading">
        <h3 class="panel-title">Path Outage Node List</h3>
    </div>
    <table class="table table-condensed severity">
          <thead class="dark">
          <tr>
          <th>Critical Path</th>
          <th>Status</th>
          </tr>
          </thead>

          <tr>
          <td><%= critIp %></td>
          <td class="bright severity-<%= pthData[3].toLowerCase() %>"><%= critSvc %></td>
          </tr>

          <thead class="dark">
          <tr>
          <th>Node</th>
          <th>Status</th>
          </tr>
          </thead>

          <% for (Integer nodeid : nodeList) {
              String labelColor[] = PathOutageManagerDaoImpl.getInstance().getLabelAndStatus(nodeid.toString(), null); %>
              <tr>
              <td><a href="element/node.jsp?node=<%= nodeid %>"><%= labelColor[0] %></a></td>
              <td class="bright severity-<%= labelColor[1].toLowerCase() %>"><%= labelColor[2] %></td>
              </tr>
          <% } %>
    </table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

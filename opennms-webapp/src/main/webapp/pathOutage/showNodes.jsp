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
			org.opennms.core.db.DataSourceFactory,
			org.opennms.core.utils.DBUtils,
			org.opennms.netmgt.poller.PathOutageManagerJdbcImpl
" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Show Path Outage Nodes" />
  <jsp:param name="headTitle" value="Show Path Outage Nodes" />
  <jsp:param name="breadcrumb" value="Show Path Outage Nodes" />

</jsp:include>

<% 
      String critIp = request.getParameter("critIp");
      String critSvc = request.getParameter("critSvc");
      String[] pthData = PathOutageManagerJdbcImpl.getInstance().getCriticalPathData(critIp, critSvc);
      List<String> nodeList = PathOutageManagerJdbcImpl.getInstance().getNodesInPath(critIp, critSvc); %>
  
      <h3>Path Outage Node List</h3>
      <table>
          <tr>
          <th>Critical Path</th>
          <th>Status</th>
          </tr>

          <tr class="CellStatus">
          <td><%= critIp %></td>
          <td class="<%= pthData[3] %>"><%= critSvc %></td>
          </tr>

          <tr>
          <th>Node</th>
          <th>Status</th>
          </tr>

<%        final Connection conn = DataSourceFactory.getInstance().getConnection();
          final DBUtils d = new DBUtils(PathOutageManagerJdbcImpl.class, conn);
          try {
              for (String nodeid : nodeList) {
                  String labelColor[] = PathOutageManagerJdbcImpl.getInstance().getLabelAndStatus(nodeid, conn); %>
                  <tr class="CellStatus">
                  <td><a href="element/node.jsp?node=<%= nodeid %>"><%= labelColor[0] %></a></td>
                  <td class="<%= labelColor[1] %>"><%= labelColor[2] %></td>
                  </tr>
              <% } %>
          <% } finally {
            d.cleanUp();
          } %>

      </table>

<jsp:include page="/includes/footer.jsp" flush="false" />



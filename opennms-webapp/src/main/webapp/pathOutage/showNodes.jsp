<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	import=" java.sql.Connection,
                java.util.Iterator,
		java.util.List,
                org.opennms.core.resource.Vault,
                org.opennms.web.pathOutage.*" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Show Path Outage Nodes" />
  <jsp:param name="headTitle" value="Show Path Outage Nodes" />
  <jsp:param name="breadcrumb" value="Show Path Outage Nodes" />

</jsp:include>

<% 
      String critIp = request.getParameter("critIp");
      String critSvc = request.getParameter("critSvc");
      String[] pthData = PathOutageFactory.getCriticalPathData(critIp, critSvc);
      List<String> nodeList = PathOutageFactory.getNodesInPath(critIp, critSvc); %>
  
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

<%        Iterator<String> iter = nodeList.iterator();
          Connection conn = Vault.getDbConnection();
          try {
              while( iter.hasNext() ) {
                  String nodeid = iter.next();
                  String labelColor[] = PathOutageFactory.getLabelAndStatus(nodeid, conn); %>
                  <tr class="CellStatus">
                  <td><a href="element/node.jsp?node=<%= nodeid %>"><%= labelColor[0] %></a></td>
                  <td class="<%= labelColor[1] %>"><%= labelColor[2] %></td>
                  </tr>
              <% } %>
          <% } finally {
            Vault.releaseDbConnection(conn);
          } %>

      </table>

<jsp:include page="/includes/footer.jsp" flush="false" />



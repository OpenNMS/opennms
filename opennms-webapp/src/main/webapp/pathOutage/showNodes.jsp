<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: HTML fix from bug #1558. - dj@opennms.org
// 2006 Apr 25: improved speed and appearance
// 2006 Apr 17: Created file
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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
      List nodeList = PathOutageFactory.getNodesInPath(critIp, critSvc); %>

      <br>
      <table class="wdth600">
          <tr>
          <td class="standardbold" style="background-color: <%= pthData[3]%>" colspan="4" align="center">Node List for Critical Path <%= critIp %> <%= critSvc %></td>
          </tr>
          <tr>
          <td class="standard" colspan="4">&nbsp;</td>
          </tr>

          <tr>
          <td class="standardheader" width="40%" align="center"><%= "Node" %></td>
          <td class="standardheader" width="40%" align="center">Status</td>
          </tr>

<%        Iterator iter = nodeList.iterator();
          Connection conn = Vault.getDbConnection();
          try {
              while( iter.hasNext() ) {
                  String nodeid = (String)iter.next();
                  String labelColor[] = PathOutageFactory.getLabelAndStatus(nodeid, conn); %>
                  <tr>
                  <td class="standard"><a href="element/node.jsp?node=<%= nodeid %>"><%= labelColor[0] %></a></td>
                  <td class="standard" style="background-color: <%= labelColor[1] %>"><%= labelColor[2] %></td>
                  </tr>
              <% } %>
          <% } finally {
            Vault.releaseDbConnection(conn);
          } %>

      </table>

<jsp:include page="/includes/footer.jsp" flush="false" />



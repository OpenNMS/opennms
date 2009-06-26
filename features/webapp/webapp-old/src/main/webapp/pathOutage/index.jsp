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
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
// 2006 Aug 25: Small HTML fix. - dj@opennms.org
// 2006 Aug 15: HTML fix from bug #1558. - dj@opennms.org
// 2006 Apr 17: File created
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
	import="java.util.Iterator,
		java.util.List,
                org.opennms.netmgt.config.OpennmsServerConfigFactory,
                org.opennms.web.pathOutage.*" %>



<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Path Outages" />
  <jsp:param name="headTitle" value="Path Outages" />
  <jsp:param name="location" value="pathOutage" />
  <jsp:param name="breadcrumb" value="Path Outages" />
</jsp:include>

<% OpennmsServerConfigFactory.init(); %>
    

<%
        List<String[]> testPaths = PathOutageFactory.getAllCriticalPaths();
        String dcpip = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
        String[] pthData = PathOutageFactory.getCriticalPathData(dcpip, "ICMP");
%>
<% if (dcpip != null && !dcpip.equals("")) { %>
	<p>Default Critical Path = <%= dcpip %> ICMP</p>
<% } %>
	<h3>All path outages</h3>
	<table>
		<tr>
			<th>Critical Path Node</th>
			<th><%= "Critical Path IP" %></th>
			<th><%= "Critical Path Service" %></th>
			<th># of Nodes</th>
		</tr>
		<%          Iterator<String[]> iter2 = testPaths.iterator();
		while( iter2.hasNext() ) {
			String[] pth = iter2.next();
			pthData = PathOutageFactory.getCriticalPathData(pth[0], pth[1]); %>
			<tr class="CellStatus">
				<% if((pthData[0] == null) || (pthData[0].equals(""))) { %>
					<td>(interface not in DB)</td>
					<% } else if (pthData[0].indexOf("nodes have this IP") > -1) { %>
						<td><a href="element/nodeList.htm?iplike=<%= pth[0] %>"><%= pthData[0] %></a></td>
						<% } else { %>
							<td><a href="element/node.jsp?node=<%= pthData[1] %>"><%= pthData[0] %></a></td>
							<% } %>
							<td><%= pth[0] %></td>
							<td class="<%= pthData[3] %>" align="center"><%= pth[1] %></td>
							<td><a href="pathOutage/showNodes.jsp?critIp=<%= pth[0] %>&critSvc=<%= pth[1] %>"><%= pthData[2] %></a></td>
						</tr>
						<% } %>
</table>



<jsp:include page="/includes/footer.jsp" flush="false" />

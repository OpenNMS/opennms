<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
	import="
		java.util.List,
		org.opennms.netmgt.config.OpennmsServerConfigFactory,
		org.opennms.netmgt.poller.PathOutageManagerJdbcImpl
	"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Path Outages" />
  <jsp:param name="headTitle" value="Path Outages" />
  <jsp:param name="location" value="pathOutage" />
  <jsp:param name="breadcrumb" value="Path Outages" />
</jsp:include>

<% OpennmsServerConfigFactory.init(); %>

<%
        List<String[]> testPaths = PathOutageManagerJdbcImpl.getInstance().getAllCriticalPaths();
        String dcpip = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathIp();
        String[] pthData = PathOutageManagerJdbcImpl.getInstance().getCriticalPathData(dcpip, "ICMP");
%>
<% if (dcpip != null && !"".equals(dcpip)) { %>
	<p>The default critical path is service ICMP on interface <%= dcpip %>.</p>
<% } %>

<div class="panel panel-success fix-subpixel">
	<div class="panel-heading">
		<h3 class="panel-title">All Path Outages</h3>
	</div>
	<table class="table table-condensed severity">
		<thead class="dark">
			<tr>
				<th>Critical Path Node</th>
				<th>Critical Path IP</th>
				<th>Critical Path Service</th>
				<th>Number of Nodes</th>
			</tr>
		</thead>
		<% for (String[] pth : testPaths) {
			pthData = PathOutageManagerJdbcImpl.getInstance().getCriticalPathData(pth[0], pth[1]); %>
		<tr>
			<% if((pthData[0] == null) || (pthData[0].equals(""))) { %>
			<td>(Interface not in database)</td>
			<% } else if (pthData[0].indexOf("nodes have this IP") > -1) { %>
			<td><a href="element/nodeList.htm?iplike=<%= pth[0] %>"><%= pthData[0] %></a></td>
			<% } else { %>
			<td><a href="element/node.jsp?node=<%= pthData[1] %>"><%= pthData[0] %></a></td>
			<% } %>
			<td><%= pth[0] %></td>
			<td class="<%= pthData[3] %>"><%= pth[1] %></td>
			<td><a
				href="pathOutage/showNodes.jsp?critIp=<%= pth[0] %>&critSvc=<%= pth[1] %>"><%= pthData[2] %></a></td>
		</tr>
		<% } %>
	</table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

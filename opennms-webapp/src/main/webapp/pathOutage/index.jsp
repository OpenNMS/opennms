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
<%@page language="java" contentType="text/html" session="true"
	import="
		java.net.InetAddress,
		java.util.List,
		org.opennms.core.utils.InetAddressUtils,
		org.opennms.features.topology.link.Layout,
		org.opennms.features.topology.link.TopologyLinkBuilder"
%>
<%@ page import="org.opennms.features.topology.link.TopologyProvider" %>
<%@ page import="org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Path Outages")
          .breadcrumb("Path Outages")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<%
        List<String[]> testPaths = PathOutageManagerDaoImpl.getInstance().getAllCriticalPaths();
        InetAddress dcpip = PathOutageManagerDaoImpl.getInstance().getDefaultCriticalPathIp();
        String dcpipString = dcpip != null ? InetAddressUtils.toIpAddrString(dcpip) : null;
        String[] pthData = PathOutageManagerDaoImpl.getInstance().getCriticalPathData(dcpipString, "ICMP");
%>
<% if (dcpip != null) { %>
	<p>The default critical path is service ICMP on interface <%= dcpipString %>.</p>
<% } %>

<div class="card fix-subpixel">
	<div class="card-header">
		<span>All Path Outages</span>
	</div>
	<table class="table table-sm severity">
			<tr>
				<th>Critical Path Node</th>
				<th>Critical Path IP</th>
				<th>Critical Path Service</th>
				<th>Number of Nodes</th>
				<th>Actions</th>
			</tr>
		<% for (String[] pth : testPaths) {
			pthData = PathOutageManagerDaoImpl.getInstance().getCriticalPathData(pth[1], pth[2]); %>
		<tr>
			<% if((pthData[0] == null) || (pthData[0].equals(""))) { %>
			<td>(Interface not in database)</td>
			<% } else if (pthData[0].indexOf("nodes have this IP") > -1) { %>
			<td><a href="element/nodeList.htm?iplike=<%= pth[1] %>"><%= pthData[0] %></a></td>
			<% } else { %>
			<td><a href="element/node.jsp?node=<%= pthData[1] %>"><%= pthData[0] %></a></td>
			<% } %>
			<td><%= pth[1] %></td>
			<td class="severity-<%= pthData[3] %> bright"><%= pth[2] %></td>
			<td><a href="pathOutage/showNodes.jsp?critIp=<%= pth[1] %>&critSvc=<%= pth[2] %>"><%= pthData[2] %></a></td>
			<%
				final String topologyLink = new TopologyLinkBuilder()
						.focus(pthData[1])
						.szl(0)
						.layout(Layout.HIERARCHY)
						.provider(TopologyProvider.PATH_OUTAGE)
						.getLink();
			%>
			<td><a href="<%= topologyLink%>"><i class="fa fa-external-link-square"></i> View in Topology</a></td>
		</tr>
		<% } %>
	</table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

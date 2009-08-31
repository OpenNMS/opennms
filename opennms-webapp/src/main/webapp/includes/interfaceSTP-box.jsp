<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
//
// Modifications:
//
// 2009 Aug 28: Restore search and display capabilities for non-ip interfaces
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
// Foundation, Inc.:
// 51 Franklin Street
// 5th Floor
// Boston, MA 02110-1301
// USA
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of links.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*" %>

<%
    
    Interface intf = null;
    String requestNode = request.getParameter("node");
    String requestIntf = request.getParameter("intf");
    String requestIfindex = request.getParameter("ifindex");
    if(requestNode != null && requestIfindex != null && requestIntf == null) {
        intf = ElementUtil.getSnmpInterfaceByParams(request);
    } else {
        intf = ElementUtil.getInterfaceByParams(request);
    }


// find STP interface info
    StpInterface[] stpifs = NetworkElementFactory.getStpInterface(intf.getNodeId(), intf.getIfIndex());

%>
<h3>Interface Spanning Tree Protocol Info</h3>
<table>
  
  
<% if(stpifs.length == 0) { %>
  <tr>
    <td colspan="11">No spanning tree information has been collected for this interface.</td>
  </tr>
<% } else { %>
        <% for (int i=0; i < stpifs.length;i++) { %>
	<tr>
        <td>VlanIdentifier</td>			  
		<td bgcolor="<%=stpifs[i].getVlanColorIdentifier()%>"><%=stpifs[i].get_stpvlan()%>
		</td>			  
	</tr>
	<tr>
        <td>STP Port Status</td>
        <td><%=stpifs[i].getStpPortState()%></td>
	</tr>
	<tr>
        <td>Path Cost</td>
        <td><%=stpifs[i].get_stpportpathcost()%></td>
	</tr>
	<tr>
        <td>Stp Root</td>
	<% if (stpifs[i].get_stprootnodeid() != 0) { 
	Node node = NetworkElementFactory.getNode(stpifs[i].get_stprootnodeid());
	%>
	<td><a href="element/node.jsp?node=<%=stpifs[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedroot()%></strong>)</td>
	<% } else { %>
	<td><%=stpifs[i].get_stpdesignatedroot()%></td>
	<% } %>
	</tr>
	<tr>
        <td>Designated Bridge</td>
	<% if (stpifs[i].get_stpbridgenodeid() != 0) { 
	Node node = NetworkElementFactory.getNode(stpifs[i].get_stpbridgenodeid());
	%>
	<td><a href="element/node.jsp?node=<%=stpifs[i].get_stpbridgenodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedbridge()%></strong>)</td>
	<% } else {%>
	<td><%=stpifs[i].get_stpdesignatedbridge()%></td>
	<% } %>
	</tr>
	<tr>
        <td>Designated Port</td>
        <td><%=stpifs[i].get_stpdesignatedport()%></td>
	</tr>
	<tr>
        <td>Designated Cost</td>
        <td><%=stpifs[i].get_stpportdesignatedcost()%></td>
	</tr>
	<tr>
        <td>Information Status</td>
        <td><%=stpifs[i].getStatusString()%></td>
	</tr>
	<tr>
        <td>Last Poll Time</td>
        <td><%=stpifs[i].get_lastPollTime()%></td>
        </tr>
        <% } %>
<% } %>
                     
</table>      


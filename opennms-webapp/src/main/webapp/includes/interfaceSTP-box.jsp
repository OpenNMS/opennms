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

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of links.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,org.opennms.netmgt.model.OnmsNode, org.opennms.core.utils.WebSecurityUtils" %>

<%
    
    String requestNode = request.getParameter("node");
    String requestIfindex = request.getParameter("ifindex");
	
    int nodeId = -1;
	int ifIndex = -1;    
	
	if ( requestNode != null && requestIfindex != null ) {
		nodeId = WebSecurityUtils.safeParseInt(requestNode);
		ifIndex = WebSecurityUtils.safeParseInt(requestIfindex);
	}
    
    StpInterface[] stpifs = NetworkElementFactory.getInstance(getServletContext()).getStpInterface(nodeId, ifIndex);


%>
<div class="panel panel-default">
<div class="panel-heading">
<h3 class="panel-title">Interface Spanning Tree Protocol Info</h3>
</div>
<table class="table table-condensed">
  
  
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
	OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(stpifs[i].get_stprootnodeid());
	%>
	<td><a href="element/node.jsp?node=<%=stpifs[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br/>(<strong><%=stpifs[i].get_stpdesignatedroot()%></strong>)</td>
	<% } else { %>
	<td><%=stpifs[i].get_stpdesignatedroot()%></td>
	<% } %>
	</tr>
	<tr>
        <td>Designated Bridge</td>
	<% if (stpifs[i].get_stpbridgenodeid() != 0) { 
	OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(stpifs[i].get_stpbridgenodeid());
	%>
	<td><a href="element/node.jsp?node=<%=stpifs[i].get_stpbridgenodeid()%>"><%=node.getLabel()%></a><br/>(<strong><%=stpifs[i].get_stpdesignatedbridge()%></strong>)</td>
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
</div>

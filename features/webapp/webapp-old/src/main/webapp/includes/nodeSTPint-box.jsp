<%--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

--%>

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of last active inventories.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.WebSecurityUtils,org.opennms.web.element.*" %>


<%
    //required parameter node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node");
    }
        
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
   	StpInterface[] stpifs = NetworkElementFactory.getStpInterface(nodeId);

%>
<h3>Node STP Interface Info</h3>
<table>
<% if(stpifs.length == 0) { %>
  <tr>
    <td >There have been no STP Interfaces info on this node.</td>
  </tr>
<% } else { %>
			<thead>
              <tr>
                <th>Vlan Identifier</th>			  
                <th>Port/Ifindex</th>
                <th>Port Status</th>
                <th>Status</th>
                <th>Path Cost</th>
                <th>Stp Root</th>
                <th>Designated Bridge</th>
                <th>Designated Port</th>
                <th>Designated Cost</th>
                <th>Last Poll Time</th>
              </tr>
             </thead>
              <% for (int i=0; i < stpifs.length;i++) { %>
			  <tr bgcolor="<%=stpifs[i].getVlanColorIdentifier()%>">
                <td><%=stpifs[i].get_stpvlan()%></td>		  
                <td><%=stpifs[i].get_bridgeport()%>/<a href="element/interface.jsp?node=<%=nodeId%>&intf=<%=stpifs[i].get_ipaddr()%>&ifindex=<%=stpifs[i].get_ifindex()%>"><%=stpifs[i].get_ifindex()%></a></td>
                <td><%=stpifs[i].getStpPortState()%></td>
                <td><%=stpifs[i].getStatusString()%></td>
                <td><%=stpifs[i].get_stpportpathcost()%></td>
				<% if (stpifs[i].get_stprootnodeid() != 0) { 
						Node node = NetworkElementFactory.getNode(stpifs[i].get_stprootnodeid());
				%>
				<td><a href="element/node.jsp?node=<%=stpifs[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedroot()%></strong>)</td>
				<% } else { %>
				<td><%=stpifs[i].get_stpdesignatedroot()%></td>
				<% } %>
				<% if (stpifs[i].get_stpbridgenodeid() != 0) { 
						Node node = NetworkElementFactory.getNode(stpifs[i].get_stpbridgenodeid());
				%>
				<td><a href="element/node.jsp?node=<%=stpifs[i].get_stpbridgenodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedbridge()%></strong>)</td>
				<% } else {%>
				<td><%=stpifs[i].get_stpdesignatedbridge()%></td>
				<% } %>
                <td><%=stpifs[i].get_stpdesignatedport()%></td>
                <td><%=stpifs[i].get_stpportdesignatedcost()%></td>
                <td><%=stpifs[i].get_lastPollTime()%></td>
              </tr>
              <% } %>
       <% } %>
                     
</table>      


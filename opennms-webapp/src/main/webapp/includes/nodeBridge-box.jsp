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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.WebSecurityUtils, org.opennms.web.element.*" %>


<%
    //required parameter node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node");
    }
        
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
   	StpNode[] stpnodes = NetworkElementFactory.getStpNode(nodeId);

%>


  

<h3>Node Bridge Info</h3>
<% if(stpnodes.length == 0) { %>
		
	<table>
		<tr>
		<td>There have been no bridge info on this node.</td>
		</tr>
	</table>
        
<% } else { %>
		<table>
			<thead>
              <tr>
                <th>Vlan Id</th>			  
                <th>Vlan Name</th>			  
                <th>Base Address</th>
                <th>Type</th>
                <th>Stp Proto Spec</th>
                <th>Port Num.</th>
                <th>Status</th>
                <th>Stp Root</th>
                <th>Stp Priority</th>
                <th>Stp Root Cost</th>
                <th>Stp Root Port</th>
                <th>Last Poll Time</th>
              </tr>
             </thead>
              <% for (int i=0; i < stpnodes.length;i++) { %>
			  <tr bgcolor="<%=stpnodes[i].getVlanColorIdentifier()%>">
                <td><%=stpnodes[i].get_basevlan()%></td>			  
                <td><%=stpnodes[i].getBaseVlanName()%></td>			  
                <td><%=stpnodes[i].get_basebridgeaddress()%></td>
                <td><%=stpnodes[i].getBaseType()%></td>
                <td><%=stpnodes[i].getStpProtocolSpecification()%></td>
                <td><%=stpnodes[i].get_basenumports()%></td>
                <td><%=stpnodes[i].getStatusString()%></td>
				<% if (stpnodes[i].get_stprootnodeid() != 0) { 
					   	Node node = NetworkElementFactory.getNode(stpnodes[i].get_stprootnodeid());
				%>
                <td><a href="element/node.jsp?node=<%=stpnodes[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br/>(<strong><%=stpnodes[i].get_stpdesignatedroot()%></strong>)</td>
				<% } else { %>
				<td><%=stpnodes[i].get_stpdesignatedroot()%></td>
				<% } %>
                <td><%=stpnodes[i].get_stppriority()%></td>
                <td><%=stpnodes[i].get_stprootcost()%></td>
                <td><%=stpnodes[i].get_stprootport()%></td>
                <td><%=stpnodes[i].get_lastPollTime()%></td>
              </tr>
              <% } %>
       <% } %>
                     
</table>      

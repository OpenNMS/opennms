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

<%@page language="java" contentType="text/html" session="true" import="
  org.opennms.core.utils.WebSecurityUtils, 
  org.opennms.web.element.*,
  org.opennms.netmgt.model.OnmsNode
"%>


<%
    //required parameter node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException("node");
    }
        
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
   	StpNode[] stpnodes = NetworkElementFactory.getInstance(getServletContext()).getStpNode(nodeId);

%>


  

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Node Bridge Info</h3>
  </div>
<% if(stpnodes.length == 0) { %>
		
	<table class="table table-condensed">
		<tr>
		<td>There have been no bridge info on this node.</td>
		</tr>
	</table>
        
<% } else { %>
		<table class="table table-condensed">
			<thead>
              <tr>
                <th>VLAN ID</th>			  
                <th>VLAN Name</th>			  
                <th>Base Address</th>
                <th>Type</th>
                <th>STP Proto Spec</th>
                <th>Port Num.</th>
                <th>Status</th>
                <th>STP Root</th>
                <th>STP Priority</th>
                <th>STP Root Cost</th>
                <th>STP Root Port</th>
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
					   	OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(stpnodes[i].get_stprootnodeid());
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
</div> <!-- panel -->

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
  abbreviated list of last active inventories.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="
  org.opennms.core.utils.WebSecurityUtils,
  org.opennms.web.element.*,
  org.opennms.netmgt.model.OnmsNode
"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    //required parameter node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException("node");
    }
        
    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
   	StpInterface[] stpifs = NetworkElementFactory.getInstance(getServletContext()).getStpInterface(nodeId);

%>
<div class="panel panel-default">
<div class="panel-heading">
  <h3 class="panel-title">Node STP Interface Info</h3>
</div>
<table class="table table-condensed">
<% if(stpifs.length == 0) { %>
  <tr>
    <td>There are no STP interfaces on this node.</td>
  </tr>
<% } else { %>
			<thead>
              <tr>
                <th>VLAN Identifier</th>			  
                <th>Port/Ifindex</th>
                <th>Port Status</th>
                <th>Status</th>
                <th>Path Cost</th>
                <th>STP Root</th>
                <th>Designated Bridge</th>
                <th>Designated Port</th>
                <th>Designated Cost</th>
                <th>Last Poll Time</th>
              </tr>
             </thead>
              <% for (int i=0; i < stpifs.length;i++) { %>
			  <tr bgcolor="<%=stpifs[i].getVlanColorIdentifier()%>">
                <td><%=stpifs[i].get_stpvlan()%></td>		  
		<% if (stpifs[i].get_ipaddr() != null && !"0.0.0.0".equals(stpifs[i].get_ipaddr())) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                  <c:param name="intf" value="<%=stpifs[i].get_ipaddr()%>"/>
                  <c:param name="ifindex" value="<%=String.valueOf(stpifs[i].get_ifindex())%>"/>
                </c:url>
                <td><%=stpifs[i].get_bridgeport()%>/<a href="${interfaceLink}"><%=stpifs[i].get_ifindex()%></a></td>
		<% } else { %>
			<td><%=stpifs[i].get_bridgeport()%>/<a href="element/snmpinterface.jsp?node=<%=nodeId%>&ifindex=<%=stpifs[i].get_ifindex()%>"><%=stpifs[i].get_ifindex()%></a></td>
		<% } %>
                <td><%=stpifs[i].getStpPortState()%></td>
                <td><%=stpifs[i].getStatusString()%></td>
                <td><%=stpifs[i].get_stpportpathcost()%></td>
				<% if (stpifs[i].get_stprootnodeid() != 0) { 
						OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(stpifs[i].get_stprootnodeid());
				%>
				<td>
				    <a href="element/node.jsp?node=<%=stpifs[i].get_stprootnodeid()%>"><c:out value="<%=node.getLabel()%>"/></a>
				    <br/>
				    (<strong><%=stpifs[i].get_stpdesignatedroot()%></strong>)
				</td>
				<% } else { %>
				<td><%=stpifs[i].get_stpdesignatedroot()%></td>
				<% } %>
				<% if (stpifs[i].get_stpbridgenodeid() != 0) { 
						OnmsNode node = NetworkElementFactory.getInstance(getServletContext()).getNode(stpifs[i].get_stpbridgenodeid());
				%>
				<td>
				    <a href="element/node.jsp?node=<%=stpifs[i].get_stpbridgenodeid()%>"><c:out value="<%=node.getLabel()%>"/></a>
				    <br/>
				    (<strong><%=stpifs[i].get_stpdesignatedbridge()%></strong>)
				</td>
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
</div> <!-- panel -->

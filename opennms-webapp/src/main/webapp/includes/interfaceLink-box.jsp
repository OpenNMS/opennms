<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*,org.opennms.core.utils.WebSecurityUtils" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    String requestNode = request.getParameter("node");
    String requestIfindex = request.getParameter("ifindex");
	
    int nodeId = -1;
	int ifIndex = -1;    
	
	if ( requestNode != null && requestIfindex != null ) {
		nodeId = WebSecurityUtils.safeParseInt(requestNode);
		ifIndex = WebSecurityUtils.safeParseInt(requestIfindex);
	}

	NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());    

%>
	
<h3>Links on Interface</h3>
		<!-- Link box -->
		<table class="standard">
		
		<thead>
			<tr>
			<th>Linked to <%=nodeId%> and <%=ifIndex%></th>
			<th width="10%">Status</th>
			<th>Last Scan</th>
			 
<%--
			// TODO - turning this off until the SET is verified.
			<% if( request.isUserInRole( Authentication.ROLE_ADMIN )) { %> 
			<th width="10%">Set Admin Status</th> 
			<% } %>
--%>

			</tr>
		</thead>
				
		<% for( LinkInterface linkInterface: factory.getDataLinksOnInterface(nodeId, ifIndex)) { %>
		    <tr>
			<td class="standard" style="font-size:70%" width="35%">
		       	<a href="element/linkednode.jsp?node=<%=linkInterface.getLinkedNodeId()%>"><%=factory.getNodeLabel(linkInterface.getLinkedNodeId())%></a>
		       	&nbsp;
		       	<%	if (linkInterface.hasLinkedInterface()) { %>
		       	on 
                
                <% if (linkInterface.getLinkedInterface().getSnmpIfName() != null && !linkInterface.getLinkedInterface().getSnmpIfName().equals("")) { %>
            	<a href="element/snmpinterface.jsp?node=<%=linkInterface.getLinkedNodeId()%>&ifindex=<%=linkInterface.getLinkedInterface().getSnmpIfIndex()%>">
                    <%=linkInterface.getLinkedInterface().getSnmpIfName()%>
                </a>
                <% } else if (linkInterface.getLinkedInterface().hasIpAddresses() && linkInterface.getLinkedInterface().getIpaddresses().size() > 0 ) { %>
	                <% for (String ipaddress : linkInterface.getLinkedInterface().getIpaddresses()) { %>
                	<c:url var="interfaceLink" value="element/interface.jsp">
	            	<c:param name="node" value="<%=String.valueOf(linkInterface.getLinkedNodeId())%>"/>
    	        	<c:param name="intf" value="<%=ipaddress%>"/>
        			</c:url>
                	<a href="<c:out value="${interfaceLink}"/>"> <%=ipaddress%> </a> &nbsp;
    	    		<% } %>                 
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
            	(ifindex <%=linkInterface.getLinkedIfindex()%>)
                
                <% if (linkInterface.getLinkedInterface().getSnmpIfAlias() != null && !linkInterface.getLinkedInterface().getSnmpIfAlias().equals("")) { %>
                    ifAlias <%=linkInterface.getLinkedInterface().getSnmpIfAlias()%>"
                <% } else { %> 
                 	&nbsp;
    			<% } %> 
    			    			
			<% } else { %>
                 <c:out value="with No Interface Associated"/>
            <% } %>
		       	
			</td>

		    <td class="standard">
		    <% if (linkInterface.getStatus() != null ) { %>
             	<%=linkInterface.getStatus()%>
            <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>

		    <td class="standard">
		    <% if (linkInterface.getLastPollTime() != null ) { %>
             	<%=linkInterface.getLastPollTime()%>
		    <% } else { %>
     			&nbsp;
		    <% } %>
		    </td>
					
		    </tr>
	    <% } %>
		    
	    </table>

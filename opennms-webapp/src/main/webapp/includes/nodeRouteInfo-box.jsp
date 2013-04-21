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
  java.util.List,
  org.opennms.core.utils.WebSecurityUtils,
  org.opennms.web.element.*,
  org.opennms.netmgt.model.OnmsNode
"%>


<%
    //required parameter node
    final String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.servlet.MissingParameterException("node");
    }
        
    final int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
    final NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());
   	final IpRouteInterface[] iproutes = ElementUtil.getIpRouteByParams(request,getServletContext());

%>

<table width="50%">
  


<% if (iproutes == null || iproutes.length == 0) { %>
  <tr>
    <td colspan="7">No IP routes have been discovered for this node.</td>
  </tr>
<% } else { %>
 <thead>
  <tr>
    <th>Destination</th>
    <th>Mask</th>
    <th>Next Hop</th>
    <th>Ifindex</th>
    <th>Metric 1</th>
    <th>Protocol</th>
    <th>Type</th>
  </tr>
 </thead>

    <% for (int t = 0; t < iproutes.length; t++) { %>
        <% IpRouteInterface iface = iproutes[t]; %>
        <% if (iface != null) { %>
	        <tr width="40%">
	            <td align="left" ><%=iface.get_routedest()%></td>
	            <td align="left" ><%=iface.get_routemask()%></td>
	            <td align="left" ><%=iface.get_routenexthop()%></td>
	            <td align="left" ><%=iface.get_ifindex()%></td>
	            <td align="left" ><%=iface.get_routemetric1()%></td>
	            <td align="left" ><%=iface.get_routeproto()%></td>
	            <td align="left" ><%=iface.get_routetype()%></td>
	        </tr>
	    <% } %>
    <% } %>
<% } %>

</table>

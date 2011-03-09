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
    final String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node");
    }
        
    final int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //gets active route entry on node
    
    final NetworkElementFactoryInterface factory = NetworkElementFactory.getInstance(getServletContext());
   	final IpRouteInterface[] iproutes = factory.getIpRoute(nodeId);

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
	            <% if (iface.get_routenexthop() != null) { %>
		            <% 
		            Node[] nodes = null;
		            if (!iface.get_routenexthop().equals("0.0.0.0")) {
		                nodes = factory.getNodesWithIpLike(iface.get_routenexthop());
		            }
		            if (nodes != null && nodes.length > 0) { %>
		                <td align="left" ><a href="element/node.jsp?node=<%=nodes[0].getNodeId()%>"><%=iface.get_routenexthop()%></a></td>
		            <% } else { %>
		                <td align="left" ><%=iface.get_routenexthop()%></td>
		            <% } %>
		        <% } else { %>
		                <td align="left" >&nbsp;</td>
		        <% } %>
	            <td align="left" ><%=iface.get_ifindex()%></td>
	            <td align="left" ><%=iface.get_routemetric1()%></td>
	            <td align="left" ><%= IP_ROUTE_PROTO.length < iface.get_routeproto() ? IP_ROUTE_PROTO[iface.get_routeproto()] : "&nbsp;" %></td>
	            <td align="left" ><%= IP_ROUTE_TYPE.length < iface.get_routetype() ? IP_ROUTE_TYPE[iface.get_routetype()] : "&nbsp;" %></td>
	        </tr>
	    <% } %>
    <% } %>
<% } %>

</table>

<%!
  //from the book _SNMP, SNMPv2, SNMPv3, and RMON 1 and 2_  (3rd Ed)
  //by William Stallings

  public static final String[] IP_ROUTE_TYPE = new String[] {
    "&nbsp;",         //0 (not supported)
    "Other",          //1
    "Invalid",        //2
    "Direct",         //3
    "Indirect",       //4
  };

  public static final String[] IP_ROUTE_PROTO = new String[] {
    "&nbsp;",         //0 (not supported)
    "Other",          //1
    "Local",          //2
    "Netmgmt",        //3
    "icmp",           //4
    "egp",            //5
    "ggp",            //6
    "hello",          //7
    "rip",            //8
    "is-is",          //9
    "es-is",          //10
    "CiscoIGRP",      //11
    "bbnSpfIgp",      //12
    "ospf",           //13
    "bgp",            //14
  };
  
  
%>

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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    Interface intf = null;
    DataLinkInterface[] dl_if = null;
    String requestNode = request.getParameter("node");
    String requestIntf = request.getParameter("intf");
    String requestIfindex = request.getParameter("ifindex");
    if(requestNode != null && requestIfindex != null && requestIntf == null) {
        intf = ElementUtil.getSnmpInterfaceByParams(request, getServletContext());
        dl_if = NetworkElementFactory.getInstance(getServletContext()).getDataLinksOnInterface(intf.getNodeId(), intf.getSnmpIfIndex());
    } else {
        intf = ElementUtil.getInterfaceByParams(request, getServletContext());
        dl_if = NetworkElementFactory.getInstance(getServletContext()).getDataLinksOnInterface(intf.getNodeId(), intf.getIfIndex());
    }

%>
	
<h3>Link Node/Interface</h3>
<table>
 
<% if(dl_if == null  || dl_if.length == 0) { %>
  <tr>
    <td colspan="4">No link information has been collected for this interface.</td>
  </tr>
<% } else { %>
  <tr>
    <th>Node</th>
    <th>Interface</th>
    <th>Status</th>
    <th>Last Poll Time</th>
  </tr>

  <% for( int i=0; i < dl_if.length; i++ ) { %>
    <% Interface iface = null; %>
    <tr>
      <td class="standard"><a href="element/linkednode.jsp?node=<%=dl_if[i].get_nodeparentid()%>"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(dl_if[i].get_nodeparentid())%></a></td>
      <td class="standard">
      <% if( "0.0.0.0".equals( dl_if[i].get_parentipaddr() ) || dl_if[i].get_parentipaddr() == null ) {
        iface = NetworkElementFactory.getInstance(getServletContext()).getSnmpInterface(dl_if[i].get_nodeparentid(),dl_if[i].get_parentifindex()); %>
        <a href="element/snmpinterface.jsp?node=<%=dl_if[i].get_nodeparentid()%>&ifindex=<%=dl_if[i].get_parentifindex()%>"><c:out value="<%=iface.getSnmpIfDescription()%>"/></a>
      <% } else { %>
        <c:url var="interfaceLink" value="element/interface.jsp">
            <c:param name="node" value="<%=String.valueOf(dl_if[i].get_nodeparentid())%>"/>
            <c:param name="intf" value="<%=dl_if[i].get_parentipaddr()%>"/>
        </c:url>
        <a href="${interfaceLink}"><c:out value="<%=dl_if[i].get_parentipaddr()%>"/></a>
      <% } %>
      </td>

      <td><%=dl_if[i].getStatusString() == null ? "&nbsp;" : dl_if[i].getStatusString()%></td>
      <td><%=dl_if[i].get_lastPollTime()%></td>
    </tr>
  <% } %>
<% } %>

</table>

<%!
     public String getVlanColorIdentifier( int i ) {
        int red = 128;
        int green = 128;
        int blue = 128;
        int redoffset = 47;
        int greenoffset = 29;
        int blueoffset = 23;
        if (i == 1) return "#FFFFFF";
        red = (red + i * redoffset)%255;
        green = (green + i * greenoffset)%255;
        blue = (blue + i * blueoffset)%255;
        if (red < 64) red = red+64;
        if (green < 64) green = green+64;
        if (blue < 64) blue = blue+64;
        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
    }
%>

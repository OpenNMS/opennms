<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com///

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*" %>

<%
    Node[] nodes = null;
    String nameParm = request.getParameter( "nodename" );
    String ipLikeParm = request.getParameter( "iplike" );
    String serviceParm = request.getParameter( "service" );

    if( nameParm != null ) {
        nodes = NetworkElementFactory.getNodesLike( nameParm );
    }
    else if( ipLikeParm != null ) {
        nodes = NetworkElementFactory.getNodesWithIpLike( ipLikeParm );
    }
    else if( serviceParm != null ) {
        int serviceId = Integer.parseInt( serviceParm );
        nodes = NetworkElementFactory.getNodesWithService( serviceId );
    }
    else {
        nodes = NetworkElementFactory.getAllNodes();
    }

    int lastIn1stColumn = (int)Math.ceil( nodes.length/2.0 );
    int interfaceCount = 0;
%>

<html>
<head>
  <title>Node List | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href ='element/index.jsp'>Search</a>"; %>
<% String breadcrumb2 = "Node List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <h3>Nodes and their Interfaces</h3>
    <td> &nbsp; </td>
  </tr>

  <tr>
    <td> &nbsp; </td>

  <% if( nodes.length > 0 ) { %>
    <td valign="top">
      <!-- left column -->
      <ul>
      <% for( int i=0; i < lastIn1stColumn; i++ ) { %>
        <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
        <% Interface[] interfaces = NetworkElementFactory.getActiveInterfacesOnNode(nodes[i].getNodeId()); %>
        <ul>
          <% for( int j=0; j < interfaces.length; j++ ) { %>
            <% if( !"0.0.0.0".equals(interfaces[j].getIpAddress() )) { 
               interfaceCount++;
            %>
            <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a>
            <% } %>
          <% } %>
        </ul>
      <% } %>
      </ul>
    </td>

    <td valign="top">      
      <!-- right column -->
      <ul>
      <% for( int i=lastIn1stColumn; i < nodes.length; i++ ) { %>
        <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
        <% Interface[] interfaces = NetworkElementFactory.getActiveInterfacesOnNode(nodes[i].getNodeId()); %>
        <ul>
          <% for( int j=0; j < interfaces.length; j++ ) { %>
            <% if( !"0.0.0.0".equals(interfaces[j].getIpAddress() )) { 
               interfaceCount++;
            %>
            <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a>
            <% } %>
          <% } %>
        </ul>
      <% } %>
      </ul>
    </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
    
    <td> &nbsp; </td>
  </tr>
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <br><%=nodes.length%> Nodes, <%=interfaceCount%> Interfaces</td>
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="nodelist" />
</jsp:include>

</body>
</html>

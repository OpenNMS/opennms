<%--

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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.element.*"
%>

<%
    Node[] nodes = null;
    String nameParm = request.getParameter( "nodename" );
    String ipLikeParm = request.getParameter( "iplike" );
    String serviceParm = request.getParameter( "service" );
    String ifAliasParm = request.getParameter( "ifAlias" );
    boolean isIfAliasSearch = false;

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
    else if( ifAliasParm != null ) {
        nodes = NetworkElementFactory.getNodesWithIfAlias( ifAliasParm );
        isIfAliasSearch = true;
    }
    else {
        nodes = NetworkElementFactory.getAllNodes();
    }

    int lastIn1stColumn = (int)Math.ceil( nodes.length/2.0 );
    int interfaceCount = 0;
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="headTitle" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<a href ='element/index.jsp'>Search</a>"/>
  <jsp:param name="breadcrumb" value="Node List"/>
</jsp:include>

<h3>Nodes and their Interfaces</h3>

  <% if( nodes.length > 0 ) { %>
    <!-- left column -->
    <div style="float: left; width=45%;">
      <ul>
      <% if(isIfAliasSearch) { %>
        <% for( int i=0; i < lastIn1stColumn; i++ ) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
          <% Interface[] interfaces = NetworkElementFactory.getInterfacesWithIfAlias(nodes[i].getNodeId(), ifAliasParm); %>
          <ul>
          <% for( int j=0; j < interfaces.length; j++ ) {
            if(interfaces[j].getSnmpIfAlias() != null && !interfaces[j].getSnmpIfAlias().equals("")) {
              interfaceCount++;
              if(interfaces[j].getIpAddress().equals("0.0.0.0")) { %>
                <li> <%=interfaces[j].getSnmpIfName()%>
              <% } else { %>
                <li> <%=interfaces[j].getIpAddress()%>
              <% } %>
              <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>&ifindex=<%=interfaces[j].getSnmpIfIndex()%>"><%=interfaces[j].getSnmpIfAlias()%></a>
            <% } %>
          <% } %>
          </ul>
        <% } %>
      <% } else {%>
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
      <% } %>
      </ul>
    </div>


    <!-- right column -->
    <div style="float: left; width=45%;">
      <ul>
      <% if(isIfAliasSearch) { %>
        <% for( int i=lastIn1stColumn; i < nodes.length; i++ ) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
          <% Interface[] interfaces = NetworkElementFactory.getInterfacesWithIfAlias(nodes[i].getNodeId(), ifAliasParm); %>
          <ul>
          <% for( int j=0; j < interfaces.length; j++ ) {
            if(interfaces[j].getSnmpIfAlias() != null && !interfaces[j].getSnmpIfAlias().equals("")) {
              interfaceCount++;
              if(interfaces[j].getIpAddress().equals("0.0.0.0")) { %>
                <li> <%=interfaces[j].getSnmpIfName()%>
              <% } else { %>
                <li> <%=interfaces[j].getIpAddress()%>
              <% } %>
              <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>&ifindex=<%=interfaces[j].getSnmpIfIndex()%>"><%=interfaces[j].getSnmpIfAlias()%></a>
            <% } %>
          <% } %>
	  </ul>
        <% } %>
      <% } else { %>
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
      <% } %>
      </ul>
    </div>
  <% } else { %>
      None found.
  <% } %>

<div class="spacer"></div>
<%=nodes.length%> Nodes, <%=interfaceCount%> Interfaces
                                     
<jsp:include page="/includes/footer.jsp" flush="false"/>

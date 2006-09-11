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

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%
    Node[] nodes = null;
    String nameParm = request.getParameter("nodename");
    String ipLikeParm = request.getParameter("iplike");
    String serviceParm = request.getParameter("service");
    String ifAliasParm = request.getParameter("ifAlias");
    boolean listInterfaces = (request.getParameter("listInterfaces") != null);
    boolean isIfAliasSearch = false;

    if (nameParm != null) {
        nodes = NetworkElementFactory.getNodesLike(nameParm);
    } else if (ipLikeParm != null) {
        nodes = NetworkElementFactory.getNodesWithIpLike(ipLikeParm);
    } else if (serviceParm != null) {
        int serviceId = Integer.parseInt(serviceParm);
        nodes = NetworkElementFactory.getNodesWithService(serviceId);
    } else if (ifAliasParm != null) {
        nodes = NetworkElementFactory.getNodesWithIfAlias(ifAliasParm);
        isIfAliasSearch = true;
    } else {
        nodes = NetworkElementFactory.getAllNodes();
    }

    int lastIn1stColumn = (int) Math.ceil(nodes.length/2.0);
    int interfaceCount = 0;
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="headTitle" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<a href ='element/index.jsp'>Search</a>"/>
  <jsp:param name="breadcrumb" value="Node List"/>
</jsp:include>

<% if (listInterfaces) { %>
  <h3>Discovered Nodes and their Interfaces</h3>
<% } else { %>
  <h3>Discovered Nodes</h3>
<% } %>
	<div class="boxWrapper">
  <% if (nodes.length > 0) { %>
    <!-- left column -->
    <div class="col">
      <ul class="plain">
      <% if (isIfAliasSearch) { %>
        <% for (int i=0; i < lastIn1stColumn; i++) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
	  <% if (listInterfaces) { %>
            <% Interface[] interfaces = NetworkElementFactory.getInterfacesWithIfAlias(nodes[i].getNodeId(), ifAliasParm); %>
            <ul>
            <% for (int j=0; j < interfaces.length; j++) {
              if (interfaces[j].getSnmpIfAlias() != null && !interfaces[j].getSnmpIfAlias().equals("")) {
                interfaceCount++;
                if (interfaces[j].getIpAddress().equals("0.0.0.0")) { %>
                  <li> <%=interfaces[j].getSnmpIfName()%>
                <% } else { %>
                  <li> <%=interfaces[j].getIpAddress()%>
                <% } %>
                <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>&ifindex=<%=interfaces[j].getSnmpIfIndex()%>"><%=interfaces[j].getSnmpIfAlias()%></a></li>
              <% } %>
            <% } %>
            </ul>
          <% } %>
        <% } %>
      <% } else {%>
        <% for (int i=0; i < lastIn1stColumn; i++) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
	  <% if (listInterfaces) { %>
            <% Interface[] interfaces = NetworkElementFactory.getActiveInterfacesOnNode(nodes[i].getNodeId()); %>
            <ul>
            <% for (int j=0; j < interfaces.length; j++) { %>
              <% if (!"0.0.0.0".equals(interfaces[j].getIpAddress())) { 
                 interfaceCount++;
                %>
                <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a></li>
              <% } %>
            <% } %>
          </ul>		    
          <% } %>
					</li>
        <% } %>
      <% } %>
      </ul>
    </div>


    <!-- right column -->
    <div class="col">
      <ul class="plain">
      <% if (isIfAliasSearch) { %>
        <% for (int i=lastIn1stColumn; i < nodes.length; i++) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
	  <% if (listInterfaces) { %>
            <% Interface[] interfaces = NetworkElementFactory.getInterfacesWithIfAlias(nodes[i].getNodeId(), ifAliasParm); %>
            <ul>
            <% for( int j=0; j < interfaces.length; j++ ) {
              if (interfaces[j].getSnmpIfAlias() != null && !interfaces[j].getSnmpIfAlias().equals("")) {
                interfaceCount++;
                if (interfaces[j].getIpAddress().equals("0.0.0.0")) { %>
                  <li> <%=interfaces[j].getSnmpIfName()%>
                <% } else { %>
                  <li> <%=interfaces[j].getIpAddress()%>
                <% } %>
                <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>&ifindex=<%=interfaces[j].getSnmpIfIndex()%>"><%=interfaces[j].getSnmpIfAlias()%></a></li>
              <% } %>
            <% } %>
	    </ul>
		</li>
          <% } %>
        <% } %>
      <% } else { %>
        <% for (int i=lastIn1stColumn; i < nodes.length; i++) { %>
          <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
	  <% if (listInterfaces) { %>
            <% Interface[] interfaces = NetworkElementFactory.getActiveInterfacesOnNode(nodes[i].getNodeId()); %>
            <ul>
              <% for (int j=0; j < interfaces.length; j++) { %>
                <% if (!"0.0.0.0".equals(interfaces[j].getIpAddress())) { 
                  interfaceCount++;
                  %>
                  <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a><li>
                <% } %>
              <% } %>
            </ul>
						</li>
          <% } %>
        <% } %>
      <% } %>
      </ul>
    </div>
  <% } else { %>
      None found.
  <% } %>
	<hr />
</div>
<div class="spacer"><!-- --></div>
<p>
<% if (listInterfaces) { %>
  <%=nodes.length%> Nodes, <%=interfaceCount%> Interfaces
<% } else { %>
  <%=nodes.length%> Nodes
<% } %>


<c:url var="thisURL" value="element/nodelist.jsp">
  <c:if test="${param.nodename != null}">
    <c:param name="nodename" value="${param.nodename}"/>
  </c:if>
  <c:if test="${param.iplike != null}">
    <c:param name="iplike" value="${param.iplike}"/>
  </c:if>
  <c:if test="${param.service != null}">
    <c:param name="service" value="${param.service}"/>
  </c:if>
  <c:if test="${param.ifAlias != null}">
    <c:param name="ifAlias" value="${param.ifAlias}"/>
  </c:if>
  <c:if test="${param.listInterfaces == null}">
    <c:param name="listInterfaces"/>
  </c:if>
</c:url>

<c:choose>
  <c:when test="${param.listInterfaces == null}">
	<a href="<c:out value='${thisURL}'/>">Show interfaces</a>
  </c:when>
  <c:otherwise>
	<a href="<c:out value='${thisURL}'/>">Hide interfaces</a>
  </c:otherwise>
</c:choose>
</p>
<jsp:include page="/includes/footer.jsp" flush="false"/>

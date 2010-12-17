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
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of outages.  All current outages and any outages resolved
  within the last 24 hours are shown.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.WebSecurityUtils,org.opennms.web.outage.*,java.util.*" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
	int nodeId = (Integer)request.getAttribute("nodeId");
	Outage[] outages = (Outage[])request.getAttribute("outages");
%>
<c:set var="nodeId"><%=nodeId%></c:set>


<c:url var="outageLink" value="outage/list.htm">
  <c:param name="filter" value="node=${nodeId}"/>
</c:url>
<h3 class="o-box"><a href="${outageLink}">Recent&nbsp;Outages</a></h3>
<table class="o-box">
<% if(outages.length == 0) { %>
  <tr>
    <td>There have been no outages on this node in the last 24 hours.</td>
  </tr>
<% } else { %>
  <tr>
    <th>Interface</th>
    <th>Service</th>
    <th>Lost</th>
    <th>Regained</th>
    <th>Outage ID</th>
  </tr>

  <%
     for( int i=0; i < outages.length; i++ ) {
     Outage outage = outages[i];
     pageContext.setAttribute("outage", outage);
  %>
		<% if( outages[i].getRegainedServiceTime() == null ) { %>
      <tr class="Critical">
    <% } else { %>
      <tr class="Cleared">
    <% } %>
      <c:url var="interfaceLink" value="element/interface.jsp">
        <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
        <c:param name="intf" value="<%=outages[i].getIpAddress()%>"/>
      </c:url>
      <td class="divider"><a href="${interfaceLink}"><%=outages[i].getIpAddress()%></a></td>
      <c:url var="serviceLink" value="element/service.jsp">
        <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
        <c:param name="intf" value="<%=outages[i].getIpAddress()%>"/>
        <c:param name="service" value="<%=String.valueOf(outages[i].getServiceId())%>"/>
      </c:url>
      <td class="divider"><a href="${serviceLink}"><c:out value="<%=outages[i].getServiceName()%>"/></a></td>
      <td class="divider"><fmt:formatDate value="${outage.lostServiceTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${outage.lostServiceTime}" type="time" pattern="HH:mm:ss"/></td>
      
      <% if( outages[i].getRegainedServiceTime() == null ) { %>
        <td class="divider bright"><b>DOWN</b></td>
      <% } else { %>
        <td class="divider bright"><fmt:formatDate value="${outage.regainedServiceTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${outage.regainedServiceTime}" type="time" pattern="HH:mm:ss"/></td>      
      <% } %>
      <td class="divider"><a href="outage/detail.htm?id=<%=outages[i].getId()%>"><%=outages[i].getId()%></a></td>       
    </tr>
  <% } %>
<% } %>

</table>

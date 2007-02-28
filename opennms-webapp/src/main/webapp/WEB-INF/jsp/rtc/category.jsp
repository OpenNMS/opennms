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
// 2004 Nov 18: Fixed problem with category display when nodeLabel can't be found. Bill Ayres.
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

<%@ page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Category Service Level Monitoring" />
  <jsp:param name="headTitle" value="XXX Some category" />
  <jsp:param name="headTitle" value="Category" />
  <jsp:param name="headTitle" value="RTC" />
  <jsp:param name="breadcrumb" value="<a href='rtc/index.jsp'>RTC</a>" />
  <jsp:param name="breadcrumb" value="Category"/>
</jsp:include>

<h3>Stuff</h3>
<table>
  <tbody>
    <tr>
      <th>Nodes</th>
      <th>Outages</th>
      <th>24hr Availability</th>
    </tr>
    
    <c:forEach var="node" items="${model.nodeList}">
      <tr class="CellStatus">
        <c:url var="nodeLink" value="element/node.jsp">
          <c:param name="node" value="${node.node.id}"/>
        </c:url>
        
        <c:choose>
          <c:when test="${node.downServiceCount == node.serviceCount}">
            <c:set var="outageClass" value="Normal"/>
          </c:when>
          
          <c:otherwise>
            <c:set var="outageClass" value="Critical"/>
          </c:otherwise>
        </c:choose>
        
        <c:choose>
          <c:when test="${node.availability == 1.0}">
            <c:set var="availabilityClass" value="Normal"/>
          </c:when>
          
          <c:otherwise>
            <c:set var="availabilityClass" value="Critical"/>
          </c:otherwise>
        </c:choose>
        <td><a href="${nodeLink}">${node.node.label}</a></td>
        <td class="${outageClass}">${node.downServiceCount} of ${node.serviceCount}</td>
        <td class="${availabilityClass}"><fmt:formatNumber value="${node.availability}" pattern="0.000%"/></td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<jsp:include page="/includes/footer.jsp" flush="false" />
 
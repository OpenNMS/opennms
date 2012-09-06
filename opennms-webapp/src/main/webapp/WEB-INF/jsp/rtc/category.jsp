<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
          <c:when test="${node.downServiceCount == 0}">
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
 
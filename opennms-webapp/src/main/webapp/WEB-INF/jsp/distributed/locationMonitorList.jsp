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
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Distributed Poller Status" />
  <jsp:param name="headTitle" value="Distributed Poller Status" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Distributed Poller Status" />
</jsp:include>

  
<c:if test="${isAdmin}">
  <div style="float:left;">
    <form action="admin/distributed/locationMonitorPauseAll.htm" method="post">
      <input type="submit" value="Pause All"/>
    </form>
  </div>
  <form action="admin/distributed/locationMonitorResumeAll.htm" method="post">
    <input type="submit" value="Resume All"/>
  </form>
 
</c:if>

<h3><spring:message code="distributed.pollerStatus.title"/></h3>

<table>
  <tr>
    <th><spring:message code="distributed.area"/></th>
    <th><spring:message code="distributed.definitionName"/></th>
    <th><spring:message code="distributed.id"/></th>
    <th><spring:message code="distributed.hostName"/></th>
    <th><spring:message code="distributed.ipAddress"/></th>
    <th><spring:message code="distributed.status"/></th>
    <th><spring:message code="distributed.lastCheckInTime"/></th>
  </tr>
  

  <c:forEach items="${model.locationMonitors}" var="monitor">
    <spring:message var="statusClass" code="distributed.status.style.${monitor.status}" text="distributed.status.style._DEFAULT"/>
    <tr class="${statusClass}">
      <td class="divider">${monitor.area}</td>
      <td class="divider">${monitor.definitionName}</td>
      <td class="divider">
        <c:url var="detailsUrl" value="distributed/locationMonitorDetails.htm">
          <c:param name="monitorId" value="${monitor.id}"/>
        </c:url> 
        <a href="${detailsUrl}">${monitor.id}</a>
      </td>
      <td class="divider">${monitor.hostName}</td>
      <td class="divider">${monitor.ipAddress}</td>
      <td class="divider bright"><spring:message code="distributed.status.value.${monitor.status}" text="${monitor.status}"/></td>
      <td class="divider">
        <c:choose>
          <c:when test="${!empty monitor.lastCheckInTime}">
            <fmt:formatDate value="${monitor.lastCheckInTime}" type="date" dateStyle="short"/>
            <fmt:formatDate value="${monitor.lastCheckInTime}" type="time" dateStyle="short"/>
          </c:when>
          
          <c:otherwise>
            Never
          </c:otherwise>
        </c:choose>
      </td>
    </tr> 
  </c:forEach>
</table>


<jsp:include page="/includes/footer.jsp" flush="false"/>

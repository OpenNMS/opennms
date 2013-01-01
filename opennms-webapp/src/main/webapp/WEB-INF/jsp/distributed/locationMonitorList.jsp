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
  <form action="admin/distributed/locationMonitorPauseAll.htm" method="post" style="display:inline;">
    <input type="submit" value="Pause All"/>
  </form>
  <form action="admin/distributed/locationMonitorResumeAll.htm" method="post" style="display:inline;">
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

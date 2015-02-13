<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Remote Poller Status" />
  <jsp:param name="headTitle" value="Remote Poller Status" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Remote Poller Status" />
</jsp:include>

  
<c:if test="${isAdmin}">
  <div class="btn-group" role="group">
  <form action="admin/distributed/locationMonitorPauseAll.htm" method="post" style="display:inline;">
    <button class="btn btn-default" type="submit">Pause All</button>
  </form>
  <form action="admin/distributed/locationMonitorResumeAll.htm" method="post" style="display:inline;">
    <button class="btn btn-default" type="submit">Resume All</button>
  </form>
  </div>
</c:if>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title"><spring:message code="distributed.pollerStatus.title"/></h3>
  </div>
  <table class="table table-condensed table-bordered severity">
    <tr>
      <th><spring:message code="distributed.area"/></th>
      <th><spring:message code="distributed.definitionName"/></th>
      <th><spring:message code="distributed.id"/></th>
      <th><spring:message code="distributed.hostName"/></th>
      <th><spring:message code="distributed.ipAddress"/></th>
      <th><spring:message code="distributed.connectionHostName"/></th>
      <th><spring:message code="distributed.connectionIpAddress"/></th>
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
        <td class="divider">${monitor.connectionHostName}</td>
        <td class="divider">${monitor.connectionIpAddress}</td>
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
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

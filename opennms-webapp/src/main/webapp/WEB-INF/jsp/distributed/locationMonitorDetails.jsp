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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Location Monitor Details" />
  <jsp:param name="headTitle" value="Location Monitor  Details" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='distributed/locationMonitorList.htm'>Location Monitors</a>" />
  <jsp:param name="breadcrumb" value="Details" />
</jsp:include>

<c:choose>
  <c:when test="${model.errors.errorCount > 0}">
    <h3><spring:message code="error"/></h3>
    <div class="boxWrapper">
      <ul class="error">
        <c:forEach var="err" items="${model.errors.allErrors}">
          <li><spring:message message="${err}"/></li>
        </c:forEach>
      </ul>
    </div>
  </c:when>
  
  <c:otherwise>
    <c:set var="monitor" value="${model.locationMonitors[0]}"/>
    
    <h3><spring:message code="distributed.locationMonitorDetails.title"/></h3>
    <table>
      <tr>
        <th><spring:message code="distributed.area"/></th>
        <td>${monitor.area}</td>
      </tr>
      <tr>
        <th><spring:message code="distributed.definitionName"/></th>
        <td>${monitor.definitionName}</td>
      </tr>
      <tr>
        <th><spring:message code="distributed.id"/></th>
        <td>${monitor.id}</td>
      </tr>
      <tr>
        <th><spring:message code="distributed.hostName"/></th>
        <td>${monitor.hostName}</td>
      </tr>
      <tr>
        <th><spring:message code="distributed.ipAddress"/></th>
        <td>${monitor.ipAddress}</td>
      </tr>
      <tr>
        <th><spring:message code="distributed.status"/></th>
        <td><spring:message code="distributed.status.value.${monitor.status}" text="${monitor.status}"/></td>
      </tr>
      <tr>
        <th><spring:message code="distributed.lastCheckInTime"/></th>
        <td>
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
    </table>
    
    <h3><spring:message code="distributed.locationMonitorDetails.additionalTitle"/></h3>
    <table>
      <c:forEach items="${monitor.additionalDetails}" var="detail">
        <tr>
          <th>
            <spring:message code="distributed.detail.${detail.key}" text="${detail.key}"/>
          </th>
          <td>
            ${detail.value}
          </td>
        </tr>
      </c:forEach>
    </table>
    
    <c:if test="${isAdmin}">
      <script type="text/javascript" >
          function confirmDelete() {
              if (confirm("Are you sure you want to proceed? This action will permanently delete all data for this location monitor in the database and cannot be undone.")) {
                  document.deleteForm.submit();
              }
          }
      </script>
      
      <h3>Manage Location Monitor</h3>
      <div class="boxWrapper">
        <form action="admin/distributed/locationMonitorDelete.htm" method="post" name="deleteForm">
          <input type="hidden" name="monitorId" value="${monitor.id}"/>
        </form>
        <input type="button" value="Delete" onClick="confirmDelete();"/>
  
        <c:choose>
          <c:when test="${monitor.status != 'PAUSED'}">
            <form action="admin/distributed/locationMonitorPause.htm" method="post">
              <p>
                <input type="hidden" name="monitorId" value="${monitor.id}"/>
                <input type="submit" value="Pause"/>
              </p>
            </form>
          </c:when>
            
          <c:otherwise>
            <form action="admin/distributed/locationMonitorResume.htm" method="post">
              <p>
                <input type="hidden" name="monitorId" value="${monitor.id}"/>
                <input type="submit" value="Resume"/>
              </p>
            </form>
          </c:otherwise>
        </c:choose>
  
        <p>
          <b>Delete</b> will delete all database data for this location monitor
          and cause the location monitor to shutdown when it next checks in.
        </p>
        
        <p>
          <b>Pause</b> will cause the location monitor to stop polling when it
          next checks in.  The location monitor can be unpaused to re-enable
          polling.
        </p>
      </div>
    </c:if>
  
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false"/>

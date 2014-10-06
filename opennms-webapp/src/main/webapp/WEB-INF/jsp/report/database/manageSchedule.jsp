<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@ page import="org.opennms.web.api.Authentication" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage Report Schedule" />
  <jsp:param name="headTitle" value="Manage Report Schedule" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Manage Report Schedule"/>
</jsp:include>

<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/manageSchedule.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>


<c:choose>
	<c:when test="${empty pagedListHolder.pageList}">
        <h3>Report Schedule List</h3>
        <div class="boxWrapper">
            <p>The database report schedule is empty.</p>
        </div>

	</c:when>

	<c:otherwise>
		<form:form commandName="command">
		<element:pagedList pagedListHolder="${pagedListHolder}"
			pagedLink="${pagedLink}" />

		<div class="spacer"><!--  --></div>
		<table>
			<thead>
				<tr>
					<th>Trigger Name</th>
					<th>Next fire time</th>
					<th>Report Parameters</th>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
					<th>Select</th>
                    <% } %>
				</tr>
			</thead>
			<%-- // show only current page worth of data --%>
			<c:forEach items="${pagedListHolder.pageList}" var="trigger">
				<tr>
					<td>${trigger.triggerName}</td>
					<td>${trigger.nextFireTime}</td>
					<td><table>
						<tr><th>Report ID</th><td>${trigger.reportId}</td></tr>
						<tr><th>Format</th><td>${trigger.deliveryOptions.format}</td></tr>
						<tr><th>Persist</th><td>${trigger.deliveryOptions.persist}</td></tr>
						<tr><th>Mail</th><td>
							<c:choose>
								<c:when test="${trigger.deliveryOptions.sendMail}">${trigger.deliveryOptions.mailTo}</c:when>
								<c:otherwise>false</c:otherwise>
							</c:choose>
						</td></tr>
						<c:forEach items="${trigger.reportParameters}" var="entry">
							<tr><th>${entry.key}</th><td>${entry.value}</td></tr>
						</c:forEach>
					</table></td>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
					<td><form:checkbox path="triggerNames" value="${trigger.triggerName}"/></td>
                    <% } %>
				</tr>
			</c:forEach>
		</table>
        <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
            <div class="pagination">
                <a onClick="toggle(true, 'triggerNames')">Select all</a> /
                <a onClick="toggle(false, 'triggerNames')">Deselect all</a>
            </div>
        <% } %>

        <% // if deletion was successful %>
        <c:if test="${not empty success}">
            <div class="alert-success" style="clear:both">
                    ${success}
            </div>
        </c:if>

        <% // If user is not allowed to delete %>
        <c:if test="${not empty error}">
            <div class="alert-error" style="clear:both">
                    ${error}
            </div>
        </c:if>
        <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
            <input type="submit" value="unschedule selected jobs"/>
        <% } %>

	</form:form>
	</c:otherwise>
</c:choose>


<jsp:include page="/includes/footer.jsp" flush="false" />

<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage Reports" />
  <jsp:param name="headTitle" value="Manage Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Manage Reports"/>
</jsp:include>

<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/manage.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>


<c:choose>
	<c:when test="${empty pagedListHolder.pageList}">
		<p>None found.</p>
	</c:when>

	<c:otherwise>
		<form:form commandName="ManageDatabaseReportCommand">
		<element:pagedList pagedListHolder="${pagedListHolder}"
			pagedLink="${pagedLink}" />

		<div class="spacer"><!--  --></div>
		<table>
			<thead>
				<tr>
					<th>title</th>
					<th>report ID</th>
					<th>run date</th>
					<th>view report</th>
					<th>select</th>
				</tr>
			</thead>
			<%-- // show only current page worth of data --%>
			<c:forEach items="${pagedListHolder.pageList}" var="report">
				<tr>
					<td>${report.title}</td>
					<td>${report.reportId}</td>
					<td>${report.date}</td>
					<td>
                    <c:if test="${empty formatMap[report.reportId]}">
                        <a href="report/database/downloadReport.htm?fileName=${report.location}">Download</a>
                    </c:if>
					<c:forEach items='${formatMap[report.reportId]}' var="format">
						<a href="report/database/downloadReport.htm?locatorId=${report.id}&format=${format}">${format}</a>
					</c:forEach>
					</td>
					<td><form:checkbox path="ids" value="${report.id}"/></td>
				</tr>
			</c:forEach>
		</table>
		<input type="submit" value="delete checked reports"/>
	</form:form>
	</c:otherwise>
</c:choose>


<jsp:include page="/includes/footer.jsp" flush="false" />

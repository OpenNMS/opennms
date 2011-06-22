<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="List Reports" />
  <jsp:param name="headTitle" value="List Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
	<jsp:param name="breadcrumb" value="Batch Reports" />
</jsp:include>

<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/batchList.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>


<c:choose>
	<c:when test="${empty pagedListHolder.pageList}">
		<p>No batch reports are defined</p>
	</c:when>

	<c:otherwise>
		<element:pagedList pagedListHolder="${pagedListHolder}"
			pagedLink="${pagedLink}" />

		<div class="spacer"><!--  --></div>
		<table>
			<thead>
				<tr>
					<th>name</th>
					<th>description</th>
					<th>action</th>
				</tr>
			</thead>
			<%-- // show only current page worth of data --%>
			<c:forEach items="${pagedListHolder.pageList}" var="report">
				<tr>
					<td>${report.displayName}</td>
					<td>${report.description}</td>
					<td> <a href="report/database/batchReport.htm?reportId=${report.id}&schedule=false">execute</a>
					<a href="report/database/batchReport.htm?reportId=${report.id}&schedule=true">schedule</a></td>
				</tr>
			</c:forEach>
		</table>
	</c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false" />

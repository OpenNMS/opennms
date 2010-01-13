<%--

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Nov 12: Created jonathan@opennms.org
 *  
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Manage"/>
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
					<th>category</th>
					<th>report ID</th>
					<th>period ending</th>
					<th>available</th>
					<th>view report</th>
					<th>select</th>
				</tr>
			</thead>
			<%-- // show only current page worth of data --%>
			<c:forEach items="${pagedListHolder.pageList}" var="report">
				<tr>
					<td>${report.category}</td>
					<td>${report.format}</td>
					<td>${report.date}</td>
					<td>${report.available}</td>
					<td><a
						href="report/database/downloadReport.htm?locatorId=${report.id}&format=html">html</a>
					<a
						href="report/database/downloadReport.htm?locatorId=${report.id}&format=pdf">pdf</a>
					<a
						href="report/database/downloadReport.htm?locatorId=${report.id}&format=svg">svg</a>
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

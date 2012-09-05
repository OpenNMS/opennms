<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="List Reports" />
  <jsp:param name="headTitle" value="List Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
	<jsp:param name="breadcrumb" value="List Reports" />
</jsp:include>
<%--
<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/reportList.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>
--%>
<c:choose>
	<c:when test="${empty repositoryList}">
		<p>No repositories with reports available.</p>
	</c:when>

	<c:otherwise>
	    <c:forEach var="mapEntry" items="${repositoryList}">
		<c:url value="/report/database/reportList.htm" var="pagedLink">
		    <c:param name="p_${mapEntry.key.id}" value="~" />
		</c:url>

		<div class="spacer" style="height: 15px"><!--  --></div>
		<%-- <h3 class="o-box"><c:out value="${mapEntry.key.displayName}" /></h3> --%>
		<table>
			<thead>
			    <tr>
				<td colspan="2" style="padding: 0px 0px"><h3 class="o-box" style="margin-top: 0px 0px 0px 0px; border: 0px;"><c:out value="${mapEntry.key.displayName}" /></td>
				<td width="150px"><element:pagedList pagedListHolder="${mapEntry.value}" pagedLink="${pagedLink}" /></td>
			    </tr>
			</thead>
			<thead>
				<tr>
					<th>name</th>
					<th>description</th>
					<th style="text-align: center">action</th>
				</tr>
			</thead>
			<%-- // show only current page worth of data --%>
			<c:forEach items="${mapEntry.value.pageList}" var="report">
				<tr>
					<td>${report.displayName}</td>
					<td>${report.description}</td>
                    <td>${report.isOnline}</td>
                    <c:choose>
                        <c:when test="${report.isOnline}">
                            <td align="center"><a href="report/database/onlineReport.htm?reportId=${report.id}">execute</a></td>
                        </c:when>
                        <c:otherwise>
                            <td>&nbsp;</td>
                        </c:otherwise>
                    </c:choose>
				</tr>
			</c:forEach>
		</table>
	    </c:forEach>
	</c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false" />

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
 * 2009 December 14th: Created jonathan@opennms.org
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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="enableSpringDojo" value="true" />
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<h3>Report Parameters</h3>

<form:form modelAttribute="parameters">
	
	<table>
		<%-- // string parameters --%>
		<c:forEach items="${parameters.stringParms}" var="stringParm" varStatus="stringParmRow">
			<tr>
				<td><c:out value="${stringParm.displayName}"/></td>
                <td>
                <c:choose>
	                <c:when test="${stringParm.inputType == 'reportCategorySelector'}">
	                	<form:select path="stringParms[${stringParmRow.index}].value"> 
		                <form:options items="${categories}"/>
		                </form:select>
	                </c:when>
	                <c:otherwise>
	                	<form:input path="stringParms[${stringParmRow.index}].value"/>
	                </c:otherwise>
                </c:choose>
				</td>
			</tr>
		</c:forEach>
		<%-- // int parameters --%>
		<c:forEach items="${parameters.intParms}" var="intParm" varStatus="intParmRow">
			<tr>
				<td><c:out value="${intParm.displayName}"/></td>
                <td>
	                <form:input path="intParms[${intParmRow.index}].value"/>
				</td>
			</tr>
		</c:forEach>
		<%-- // date parameters --%>
		<c:forEach items="${parameters.dateParms}" var="date" varStatus="dateParmRow">
			<c:choose>
				<c:when test="${ schedule && !date.useAbsoluteDate}">
					<tr>
						<td><c:out value="${date.displayName}"/></td>
						<td>
							<form:input path="dateParms[${dateParmRow.index}].count" />
							<form:input path="dateParms[${dateParmRow.index}].interval" />
							ago.
						</td>
					</tr>
				</c:when>
				<c:otherwise>
					<tr>
					<td>
					<c:out value="${date.displayName}"/></td>
						<td>
							<form:input path="dateParms[${dateParmRow.index}].value" />
							<script type="text/javascript">
								Spring.addDecoration(new Spring.ElementDecoration({
								elementId : "dateParms${dateParmRow.index}.value",
								widgetType : "dijit.form.DateTextBox",
								widgetAttrs : { datePattern : "yyyy-MM-dd", required : true }}));  
							</script>
						</td>
					</tr>
				</c:otherwise>
			</c:choose>
		</c:forEach>
	</table>
  
 	<input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
	<input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;
	
 </form:form>
  

<jsp:include page="/includes/footer.jsp" flush="false" />
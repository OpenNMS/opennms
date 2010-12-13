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
 * 2009 December 14th:	Created jonathan@opennms.org
 * 2010 December 6th:	Removed table formatting, replaced with CSS
 *						removed springDojo
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
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>
<style type="text/css">
	
.newform {
	margin:0 50px; 
	padding:50px 0;
}

.newform .label{
	font-size: 100%;
	float: left;
	width: 230px;
	margin-right: 10px;
	text-align: right;
	clear: left;
	display: block;
}

.newform p select, .newform p input, .newform p option, .newform p options {
	font-size: 70%;
}

.newform .indent {
	margin-left: 240px;
}

</style>

<h3>Report Parameters</h3>

<form:form modelAttribute="parameters" cssClass="newform">
	
	
		<%-- // string parameters --%>
		<c:forEach items="${parameters.stringParms}" var="stringParm" varStatus="stringParmRow">
			<p><form:label path="stringParms[${stringParmRow.index}].value" cssClass="label">
				<c:out value="${stringParm.displayName}"/>
			</form:label>
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
            </p>
		</c:forEach>
		<%-- // int parameters --%>
		<c:forEach items="${parameters.intParms}" var="intParm" varStatus="intParmRow">
			<p><form:label path="intParms[${intParmRow.index}].value" cssClass="label" >
				<c:out value="${intParm.displayName}"/>
			</form:label>
            <form:input path="intParms[${intParmRow.index}].value"/></p>
		</c:forEach>
		<%-- // Float parameters --%>
		<c:forEach items="${parameters.floatParms}" var="floatParm" varStatus="floatParmRow">
			<p><form:label path="floatParms[${floatParmRow.index}].value" cssClass="label" >
				<c:out value="${floatParm.displayName}"/>
			</form:label>
			<form:input path="floatParms[${floatParmRow.index}].value"/></p>
		</c:forEach>
		<%-- // Double parameters --%>
		<p><c:forEach items="${parameters.doubleParms}" var="doubleParm" varStatus="doubleParmRow">
			<form:label path="doubleParms[${doubleParmRow.index}].value" cssClass="label" >
				<c:out value="${doubleParm.displayName}"/>
			</form:label>
	        <form:input path="doubleParms[${doubleParmRow.index}].value"/></p>
		</c:forEach>
		<%-- // date parameters --%>
		<c:forEach items="${parameters.dateParms}" var="date" varStatus="dateParmRow">
			<p><span class="label">
				<c:out value="${date.displayName}"/>
			</span>
			<c:choose>
				<c:when test="${ schedule && !date.useAbsoluteDate}">
					<form:select path="dateParms[${dateParmRow.index}].count">
						<c:forEach var="count" begin="0" end="31">
							<form:option value="${count}" />
						</c:forEach>
					</form:select>
					<form:select path="dateParms[${dateParmRow.index}].interval">
							<form:option value="day">day</form:option>
							<form:option value="month">month</form:option>
							<form:option value="year">year</form:option>
                	</form:select>	
					ago, at
				</c:when>
				<c:otherwise>
					<form:input path="dateParms[${dateParmRow.index}].date" />
				</c:otherwise>
			</c:choose>
			<form:select path="dateParms[${dateParmRow.index}].hours">
				<c:forEach var="hour" begin="0" end="23">
					<form:option value="${hour}">
						<fmt:formatNumber minIntegerDigits="2" value="${hour}" />
					</form:option>
				</c:forEach>
			</form:select>
			:
			<form:select path="dateParms[${dateParmRow.index}].minutes">
				<c:forEach var="minute" begin="0" end="59">
					<form:option value="${minute}">
						<fmt:formatNumber minIntegerDigits="2" value="${minute}" />
					</form:option>
				</c:forEach>
			</form:select></p>
		</c:forEach>
		
	<span class="indent">
 		<input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
		<input type="submit" id="cancel" name="_eventId_cancel" value="Cancel"/>&#160;
	</span>
	
 </form:form>
  

<jsp:include page="/includes/footer.jsp" flush="false" />
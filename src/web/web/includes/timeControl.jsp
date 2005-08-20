<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

-->

<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<fmt:parseDate var="morning" value="01-08-2005 03:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<fmt:parseDate var="evening" value="01-08-2005 16:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<c:set var="amPmList"><fmt:formatDate value="${morning}" pattern="a"/>,<fmt:formatDate value="${evening}" pattern="a"/></c:set> 

<c:set var="prefix" value="${param.prefix}" />
<fmt:parseDate var="time" value="${param.time}" pattern="HH:mm:ss" />

					<select name="<c:out value='${prefix}'/>Hour">
					<fmt:formatDate var="startHour" value="${time}" pattern="h"/>
					<c:forEach var="h" begin="1" end="12">
						<c:choose>
							<c:when test="${h == startHour}">
								<option selected value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="<c:out value='${prefix}'/>Minute">
					<fmt:formatDate var="startMinute" value="${time}" pattern="m"/>
					<c:forEach var="half" begin="0" end="1">
						<c:choose>
							<c:when test="${half == startMinute/30}">
								<option selected value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:when>
							<c:otherwise>
								<option value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="<c:out value='${prefix}'/>AmOrPm">
					<fmt:formatDate var="startAmOrPm" value="${time}" pattern="a"/>
					<c:forEach var="a" items="${amPmList}">
						<c:choose>
						<c:when test="${a == startAmOrPm}">
							<option selected value="<c:out value='${a}'/>"><c:out value='${a}'/></option>
						</c:when>
						<c:otherwise>
							<option value="<c:out value='${a}'/>"><c:out value='${a}'/></option>
						</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>



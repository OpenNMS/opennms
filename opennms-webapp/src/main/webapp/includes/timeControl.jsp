<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:parseDate var="morning" value="01-08-2005 03:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<fmt:parseDate var="evening" value="01-08-2005 16:00:00" pattern="dd-MM-yyyy HH:mm:ss"/>
<c:set var="amPmList"><fmt:formatDate value="${morning}" pattern="a"/>,<fmt:formatDate value="${evening}" pattern="a"/></c:set> 

<c:set var="prefix" value="${param.prefix}" />
<fmt:parseDate var="time" value="${param.time}" pattern="HH:mm:ss" />

<div class="form-group">
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>Hour">
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
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>Minute">
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
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>AmOrPm">
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
  </div> <!-- column -->
</div> <!-- form-group -->

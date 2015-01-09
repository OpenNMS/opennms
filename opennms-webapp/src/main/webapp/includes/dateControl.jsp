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

<c:set var="prefix" value="${param.prefix}" />
<fmt:parseDate var="date" value="${param.date}" pattern="dd-MM-yyyy" />

<div class="form-group">
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>Date">
					<fmt:formatDate var="startDate" value="${date}" pattern="d"/>
					<c:forEach var="d" begin="1" end="31">
						<c:choose>
							<c:when test="${d == startDate}">
								<option selected value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>Month">
					<fmt:formatDate var="startMonth" value="${date}" pattern="M"/>
					<c:forEach var="m" begin="1" end="12">
						<fmt:parseDate var="mo" value="${m}" pattern="M" />
						<fmt:formatDate var="monthName" value="${mo}" pattern="MMMM" />
						<c:choose>
							<c:when test="${m == startMonth}">
								<option selected value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
  <div class="col-sm-4">
					<select class="form-control" name="<c:out value='${prefix}'/>Year">
					<fmt:formatDate var="yearStr" value="${date}" pattern="yyyy" />
					<fmt:parseNumber var="startYear" value="${yearStr}"/>
					<c:forEach var="y" begin="0" end="6">
						<c:set var="year" value="${startYear+y-3}"/>
						<c:choose>
							<c:when test="${year == startYear}">
								<option selected value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
  </div> <!-- column -->
</div> <!-- form-group -->

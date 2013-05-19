<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="OpenNMS Maps" />
	<jsp:param name="breadcrumb" value="Maps" />
</jsp:include>

<h3 class="o-box">OpenNMS Maps</h3>
<div class="boxWrapper">
<ul class="plain o-box">
<c:forEach var="entry" items="${entries.entries}">
	<c:choose>
		<c:when test="${empty entry.url}">
			<li>${entry.name}</li>
		</c:when>
		<c:otherwise>
			<li><a href="${entry.url}">${entry.name}</a></li>
		</c:otherwise>
	</c:choose>
</c:forEach>
</ul>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" >
	<jsp:param name="location" value="map" />
</jsp:include>
    

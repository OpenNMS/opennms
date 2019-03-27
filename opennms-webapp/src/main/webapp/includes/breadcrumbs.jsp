<%@ page import="org.opennms.web.api.Util" %><%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    final String baseHref = Util.calculateUrlBase( request );
%>
<c:if test="${((param.nonavbar != 'true') && (!empty pageContext.request.remoteUser)) && param.nobreadcrumbs != 'true'}">
    <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="<%= baseHref %>index.jsp">Home</a></li>
            <c:forEach var="breadcrumb" items="${paramValues.breadcrumb}" varStatus="loop">
                <c:if test="${breadcrumb != ''}">
                    <c:choose>
                        <c:when test="${loop.last}">
                            <li class="breadcrumb-item active"><c:out value="${breadcrumb}" escapeXml="false"/></li>
                        </c:when>
                        <c:otherwise>
                            <li class="breadcrumb-item"><c:out value="${breadcrumb}" escapeXml="false"/></li>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </c:forEach>
        </ol>
    </nav>
</c:if>
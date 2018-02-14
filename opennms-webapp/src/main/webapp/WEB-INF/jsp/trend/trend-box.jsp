<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.netmgt.config.trend.TrendDefinition"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    int columns = 2;

    if (request.getParameter("columns") != null) {
        columns = Integer.parseInt(request.getParameter("columns"));
    }

    int colClass = 12 / columns;
%>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Trend</h3>
    </div>
    <div class="alert-box panel-body">
        <div class="row gutter-10">
            <c:forEach var="trendDefinition" items="${trendDefinitions}">
                <div class="col-xs-<%= colClass %>">
                    <jsp:include page="/trend/trend.htm" flush="false">
                        <jsp:param name="name" value="${trendDefinition.name}"/>
                    </jsp:include>
                </div>
            </c:forEach>
        </div>
    </div>
</div>



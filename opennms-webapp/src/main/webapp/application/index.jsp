<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html;charset=UTF-8"
        session="true"
        import="
            java.util.*,
            org.opennms.web.element.*,
            org.opennms.core.utils.WebSecurityUtils,
            org.opennms.netmgt.model.remotepolling.ApplicationStatus,
            org.opennms.netmgt.model.OnmsApplication"
%>
<%@ page import="org.opennms.netmgt.model.remotepolling.Location" %>
<%@ page import="org.opennms.web.category.CategoryUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Application Status" />
    <jsp:param name="headTitle" value="Application Status" />
    <jsp:param name="breadcrumb" value="Application Status" />
</jsp:include>

<%
    final NetworkElementFactoryInterface networkElementFactory = NetworkElementFactory.getInstance(getServletContext());

    final List<OnmsApplication> applications = networkElementFactory.getAllApplications();

    long end = new Date().getTime();
    long start = end - (24 * 60 * 60);

    TreeMap<String, ApplicationStatus> statusMap = new TreeMap<>();
    for(final OnmsApplication a : applications) {
        statusMap.put(a.getName(), networkElementFactory.getApplicationStatus(a, start, end));
    }
%>

<%
    for(Map.Entry<String, ApplicationStatus> entry : statusMap.entrySet()) {
%>
    <div class="card">
        <div class="card-header">
            <span><%= WebSecurityUtils.sanitizeString(entry.getKey()) %></span>
        </div>
        <table class="table table-sm severity">
            <thead>
                <th>Location</th>
                <th>Availability (24h)</th>
            </thead>

            <%
                for(Location location : entry.getValue().getLocations()) {
                    String availClass = "normal";

                    if (location.getAggregatedStatus() < 100.0) {
                        availClass = "warning";
                    }

                    if (location.getAggregatedStatus() < 90.0) {
                        availClass = "critical";
                    }
                %>
                <tbody>
                    <tr>
                        <td class="bright"><%= location.getName() %></td>
                        <td width="30%" class="bright severity-<%=availClass%> divider" align="right"><%= CategoryUtil.formatValue(location.getAggregatedStatus()) %>%</td>
                    </tr>
                </tbody>
                <%
                }
                %>
        </table>
    </div>

        <%
    }
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
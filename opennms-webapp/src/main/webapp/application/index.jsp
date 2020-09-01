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
<%@ page import="org.opennms.netmgt.model.OnmsMonitoredService" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Application Status" />
    <jsp:param name="headTitle" value="Application Status" />
    <jsp:param name="breadcrumb" value="Application Status" />
</jsp:include>

<style>
    .scrollable {
        overflow-x: scroll;
        overflow-y: visible;
        display: block;
    }

    th, td {
        height: 1.2em;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>

<%
    final NetworkElementFactoryInterface networkElementFactory = NetworkElementFactory.getInstance(getServletContext());

    final Map<String, OnmsApplication> applications = new TreeMap<>();

    for(final OnmsApplication onmsApplication : networkElementFactory.getAllApplications()) {
        applications.put(onmsApplication.getName(), onmsApplication);
    }

    final long end = new Date().getTime();
    final long start = end - (24 * 60 * 60);

    for(final Map.Entry<String, OnmsApplication> entry : applications.entrySet()) {

        final Map<OnmsMonitoredService, Map<String, Double>> statuses = networkElementFactory.getApplicationServiceStatus(entry.getValue(), start, end);

        if (statuses.size() == 0) {
            continue;
        }

        final Set<String> locations = new TreeSet(statuses.entrySet().iterator().next().getValue().keySet());

        if (locations.size() == 0) {
            continue;
        }
%>

<div class="card">
    <div class="card-header">
        <span><%= WebSecurityUtils.sanitizeString(entry.getKey()) %></span>
    </div>
    <div class="container-fluid">
        <div class="row flex-nowrap">
            <div class="col-sm-4">
                <table class="table table-sm severity">
                    <tr>
                        <th>Node</th>
                        <th>Interface</th>
                        <th>Service</th>
                    </tr>
                    <%
                        for(final Map.Entry<OnmsMonitoredService, Map<String, Double>> serviceEntry : statuses.entrySet()) {
                    %>
                    <tr>
                        <td><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpInterface().getNode().getLabel()) %></td>
                        <td><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpAddressAsString()) %></td>
                        <td><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getServiceName()) %></td>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <th colspan="3">Overall availability</th>
                    </tr>
                </table>
            </div>

            <div class="col-sm-8 scrollable">
                <table class="table table-sm severity">
                    <tr>
                        <%
                            for(final String location : locations) {
                        %>
                        <th><%= WebSecurityUtils.sanitizeString(location) %></th>
                        <%
                            }
                        %>
                    </tr>
                    <%
                        for(final Map.Entry<OnmsMonitoredService, Map<String, Double>> serviceEntry : statuses.entrySet()) {
                    %>
                    <tr>
                        <%
                            for(final Map.Entry<String, Double> status : serviceEntry.getValue().entrySet()) {
                                final Double value = status.getValue();
                                String availClass = "normal";
                                if (value < 100.0) {
                                    availClass = "warning";
                                }
                                if (value < 90.0) {
                                    availClass = "critical";
                                }
                        %>
                        <td class="bright severity-<%= availClass %> divider" align="right"><%= CategoryUtil.formatValue(value) %>%</td>
                        <%
                            }
                        %>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <%
                            final ApplicationStatus applicationStatus = networkElementFactory.getApplicationStatus(entry.getValue(), start, end);
                            for(final String locationName : locations) {
                                final Location location = applicationStatus.getLocation(locationName);
                                final Double value = location.getAggregatedStatus();

                                String availClass = "normal";

                                if (value < 100.0) {
                                    availClass = "warning";
                                }

                                if (value < 90.0) {
                                    availClass = "critical";
                                }
                        %>
                        <td class="bright severity-<%= availClass %> divider" align="right"><%= CategoryUtil.formatValue(value) %>%</td>
                        <%
                            }
                        %>
                    </tr>
                </table>
            </div>
        </div>
    </div>
</div>
<%
    }
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
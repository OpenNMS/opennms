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
            org.opennms.netmgt.model.perspectivepolling.ApplicationStatus,
            org.opennms.netmgt.model.OnmsApplication"
%>
<%@ page import="org.opennms.netmgt.model.perspectivepolling.Location" %>
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
        overflow-x: auto;
        overflow-y: visible;
        display: block;
    }

    th.fixed-col, td.fixed-col {
        height: 1.2em;
        white-space: nowrap;
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
    final long start = end - (24 * 60 * 60 * 1000);

    for(final Map.Entry<String, OnmsApplication> entry : applications.entrySet()) {

        final Map<OnmsMonitoredService, Map<String, Double>> statuses = networkElementFactory.getApplicationServiceStatus(entry.getValue(), start, end);

        if (statuses.size() == 0) {
            continue;
        }

        final Set<String> locations = new TreeSet(statuses.entrySet().iterator().next().getValue().keySet());

        if (locations.size() == 0) {
            continue;
        }

        final ApplicationStatus applicationStatus = networkElementFactory.getApplicationStatus(entry.getValue(), start, end);
%>

<div class="card">
    <div class="card-header" style="padding: 0;">
        <table class="table table-sm severity">
            <tr>
                <%
                    final Double overallValue = applicationStatus.getOverallStatus();

                    String overallAvailClass = "normal";
                    if (overallValue < 100.0) {
                        overallAvailClass = "warning";
                    }
                    if (overallValue < 90.0) {
                        overallAvailClass = "critical";
                    }
                %>
                <td class="bright severity-<%= overallAvailClass %>" align="right"><%= CategoryUtil.formatValue(overallValue) %>%&nbsp</td>

                <td width="100%"><%= WebSecurityUtils.sanitizeString(entry.getKey()) %></td>
            </tr>
        </table>
    </div>
    <div style="display: flex; flex-wrap: nowrap; flex-direction: row;">
        <table class="table table-sm severity" style="width: 30%; margin-bottom: 0;">
            <colgroup>
                <col width="33%">
                <col width="33%">
                <col width="33%">
            </colgroup>
            <tr>
                <th>Node</th>
                <th>Interface</th>
                <th>Service</th>
            </tr>
            <%
                for(final Map.Entry<OnmsMonitoredService, Map<String, Double>> serviceEntry : statuses.entrySet()) {
            %>
            <c:url var="nodeLink" value="element/node.jsp">
                <c:param name="node" value="<%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpInterface().getNode().getNodeId())%>"/>
            </c:url>
            <c:url var="interfaceLink" value="element/interface.jsp">
                <c:param name="node" value="<%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpInterface().getNode().getNodeId())%>"/>
                <c:param name="intf" value="<%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpAddressAsString()) %>"/>
            </c:url>
            <c:url var="serviceLink" value="element/service.jsp">
                <c:param name="node" value="<%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpInterface().getNode().getNodeId())%>"/>
                <c:param name="intf" value="<%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpAddressAsString()) %>"/>
                <c:param name="service" value="<%= WebSecurityUtils.sanitizeString(String.valueOf(serviceEntry.getKey().getServiceId())) %>"/>
            </c:url>
            <tr>
                <td class="fixed-col"><a href="<c:out value="${nodeLink}"/>" target="_BLANK"><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpInterface().getNode().getLabel()) %></a></td>
                <td class="fixed-col"><a href="<c:out value="${interfaceLink}"/>" target="_BLANK"><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getIpAddressAsString()) %></a></td>
                <td class="fixed-col"><a href="<c:out value="${serviceLink}"/>" target="_BLANK"><%= WebSecurityUtils.sanitizeString(serviceEntry.getKey().getServiceName()) %></a></td>
            </tr>
            <%
                }
            %>
            <tr>
                <th class="fixed-col" colspan="3">Overall availability</th>
            </tr>
        </table>

        <table class="table table-sm severity scrollable" style="width: 70%; margin-bottom: 0;">
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
<%
    }
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

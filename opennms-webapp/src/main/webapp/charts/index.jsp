<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
        contentType="text/html; charset=UTF-8"
            pageEncoding="UTF-8"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Charts" />
  <jsp:param name="headTitle" value="Charts" />
  <jsp:param name="location" value="chart" />
  <jsp:param name="breadcrumb" value="Charts" />
</jsp:include>

<%@ page import="org.opennms.web.charts.ChartUtils" %>
<%@ page import="org.opennms.netmgt.config.charts.BarChart" %>

<%--Align images in the center of the page --%>

<div class="row row-centered" id="include-charts">
<%--Get collection of charts --%>
<%
for (BarChart chartConfig : ChartUtils.getChartCollection()) {
    String chartName = chartConfig.getName();
%>
        <img src="charts?chart-name=<%=chartName %>" alt="<%=chartName %>" />
<%
}
%>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

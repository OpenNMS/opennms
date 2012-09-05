<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html; charset=UTF-8"
            pageEncoding="UTF-8"
%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Charts" />
  <jsp:param name="headTitle" value="Charts" />
  <jsp:param name="location" value="chart" />
  <jsp:param name="breadcrumb" value="Charts" />
</jsp:include>

<%@ page import="org.opennms.netmgt.charts.ChartUtils" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.opennms.netmgt.config.charts.BarChart" %>

<%--Align images in the center of the page --%>

<div id="include-charts">
<%--Get collection of charts --%>
<%
Iterator it = ChartUtils.getChartCollectionIterator();
while (it.hasNext()) {
    BarChart chartConfig = (BarChart)it.next();
    String chartName = chartConfig.getName();
%>
        <img src="charts?chart-name=<%=chartName %>" alt="<%=chartName %>" />
<%
}
%>

</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

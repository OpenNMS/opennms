<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html; charset=UTF-8"
	    pageEncoding="UTF-8"
%>

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

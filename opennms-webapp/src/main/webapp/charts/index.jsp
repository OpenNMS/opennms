<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2006 Jun 08: Made a Charts tab
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%@page language="java"
        contentType="text/html; charset=UTF-8"
            pageEncoding="UTF-8"
%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Web Console" />
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

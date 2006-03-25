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

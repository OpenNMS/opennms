<%@ page import="java.io.*" %>
<%@ page import="org.opennms.netmgt.charts.ChartUtils" %>
<%@page contentType="image/jpeg" %>
<%
    OutputStream o = response.getOutputStream();
	ChartUtils.getBarChart("sample-bar-chart", o);
	out.flush();
	out.close();
%>
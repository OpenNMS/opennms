<!--

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
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init1.jsp"%>

<%
    // Get The Customizable (Working) Graph 
    Graph graph = this.reportFactory.getWorkingGraph();

    // Get Form Variables
    String action = request.getParameter("action");
    String timespan = request.getParameter("timespan");
    String graphtype = request.getParameter("graphtype");
    String title = request.getParameter("title");
    String g_index = request.getParameter("graphindex");
    int graph_index = (Integer.parseInt(g_index));
    graph_index--; 
 
    // Save the modified variables into the working graph 
    graph.setTitle(title);
    graph.setTimespan(timespan);
    graph.setGraphtype(graphtype);

    if (action.equals("Save")) {
        // The working graph is complete now... lets save working graph to working report 
        this.reportFactory.unloadWorkingGraph(graph_index);
    } 
      
%>


<html>
<head>
  <title>Customized Report Save Page</title>
</head>


<!-- Body -->
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<h3 align="center">Processing Graph Form...</h3>
<h3> Action: <%=action%> </h3>


<% if (action.equals("Save") || action.equals("Cancel")) { %>
    <script>
        window.location="custom_report.jsp"
    </script>
<% } %>

<% if (action.equals("Update")) { %>
    <form name="do_update" method="get" action="custom_graph3.jsp">
        <input type="hidden" name="node" value="<%=graph.getNodeId()%>">
        <input type="hidden" name="intf" value="<%=graph.getInterfaceId()%>">
    </form> 
    <script>
       document.do_update.submit(); 
    </script>
<% } %>


</body>
</html>



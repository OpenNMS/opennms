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



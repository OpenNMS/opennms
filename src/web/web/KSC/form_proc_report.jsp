<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init1.jsp" %>

<%!
    public void saveFactory() throws ServletException {    
      try {
          this.reportFactory.unloadWorkingReport();  // first copy working report into report arrays
          this.reportFactory.saveCurrent();          // Now unmarshal array to file
      }   
      catch( Exception e ) {
          throw new ServletException ( "Couldn't save KSC_PerformanceReportFactory.", e );
      }
    }
%>

<%
    // Get The Customizable Report 
    Report report = this.reportFactory.getWorkingReport();
    int report_index = this.reportFactory.getWorkingReportIndex();

    // Get Form Variables
    String action = request.getParameter("action");
    String report_title = request.getParameter("report_title");
    String show_timespan = request.getParameter("show_timespan");
    String show_graphtype = request.getParameter("show_graphtype");
    String g_index = request.getParameter("graph_index");
    int graph_index = Integer.parseInt(g_index);
 
    // Save the global variables into the working report
    report.setTitle(report_title);
    if (show_graphtype == null) {
        report.setShow_graphtype_button(false);
    }
    else {
        report.setShow_graphtype_button(true);
    } 
    if (show_timespan == null) {
        report.setShow_timespan_button(false);
    }
    else {
        report.setShow_timespan_button(true);
    } 

    if (action.equals("Save")) {
        // The working model is complete now... lets save working model to configuration file 
        saveFactory();
    } 
    else {
        if (action.equals("AddGraph") || action.equals("ModGraph")) {
            // Making a graph change... load it into the working area (the graph_index of -1 indicates a new graph)
            this.reportFactory.loadWorkingGraph(graph_index);
        }
        else {
            if (action.equals("DelGraph")) { 
                report.removeGraph(report.getGraph(graph_index));
            } 
            else {
                throw new ServletException ( "Invalid Argument for Customize Form Action.");
            }
        }
    }
      
%>


<html>
<head>
  <title>Customized Report Save Page</title>
</head>

<!-- Body -->
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<h3 align="center">Processing Form...</h3>
<h3> Action: <%=action%> </h3>


<% if (action.equals("Save")) { %>
    <script>
        window.location="index.jsp"
    </script>
<% } %>
<% if (action.equals("DelGraph")) { %>
    <script>
        window.location="custom_report.jsp"
    </script>
<% } %>
<% if (action.equals("AddGraph") || action.equals("ModGraph")) { %>
    <form name="do_next" method="get" action="custom_graph1.jsp">
        
        <% if (action.equals("AddGraph")) { %>
            <input type="hidden" name="node" value="null">
            <input type="hidden" name="intf" value="null">
        <% } else { %>
            <% Graph graph = this.reportFactory.getWorkingGraph(); %>
            <input type="hidden" name="node" value="<%=graph.getNodeId()%>">
            <input type="hidden" name="intf" value="<%=graph.getInterfaceId()%>">
        <% } %>
    </form>
    <script>
        document.do_next.submit();
    </script>
<% } %>


</body>

</html>



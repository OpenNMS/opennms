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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.netmgt.config.kscReports.*,
		org.opennms.netmgt.config.KSC_PerformanceReportFactory
	"
%>

<%@ include file="/WEB-INF/jspf/KSC/init1.jspf" %>

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


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Custom Report Save Page" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="headTitle" value="KSC" />
  <jsp:param name="quiet" value="true" />
</jsp:include>

<h3 align="center">Processing Form...</h3>
<h3> Action: <%=action%> </h3>


<% if (action.equals("Save")) { %>
    <script>
        window.location="KSC/index.jsp"
    </script>
<% } %>
<% if (action.equals("DelGraph")) { %>
    <script>
        window.location="KSC/custom_report.jsp"
    </script>
<% } %>
<% if (action.equals("AddGraph") || action.equals("ModGraph")) { %>
    <form name="do_next" method="get" action="KSC/custom_graph1.jsp">
        
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

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>


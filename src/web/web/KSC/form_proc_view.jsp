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
//      http://www.blast.com/

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.*,org.opennms.web.performance.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init2.jsp" %>
<%@ include file="include_nodereport.jsp" %>

<%
    // Get Form Variables
    int report_index = 0; 
    String override_timespan = null;
    String override_graphtype = null;
    String report_action = request.getParameter("action");
    if (report_action == null) {
        throw new MissingParameterException ("action", new String[] {"action","report","type"});
    }
    String report_type = request.getParameter("type");
    if (report_type == null) {
        throw new MissingParameterException ("type", new String[] {"action","report","type"});
    }
      
    if ((report_action.equals("Customize")) || (report_action.equals("Update"))) {
        String r_index = request.getParameter("report");
        if (r_index == null) {
            throw new MissingParameterException ("report", new String[] {"action","report","type"});
        } 
        report_index = Integer.parseInt(r_index);
        override_timespan = request.getParameter("timespan");
        if ((override_timespan == null) || (override_timespan.equals("null"))) {
            override_timespan = "none";
        }
        override_graphtype = request.getParameter("graphtype");
        if ((override_graphtype == null) || (override_graphtype.equals("null"))) {
            override_graphtype = "none";
        }
        if (report_action.equals("Customize")) {
            if (report_type.equals("node")) {
                Report report = buildNodeReport(report_index);
                this.reportFactory.loadWorkingReport(report); 
                this.reportFactory.setWorkingReportIndex(-1); // Must set index to -1 to make customizer create a new report, not replace
            }
            else { 
                // Go ahead and tell report factory to put the indexed report config into the working report area
                this.reportFactory.loadWorkingReport(report_index);
            }
            // Now inject any override characteristics into the working report model
            Report working_report = this.reportFactory.getWorkingReport();
            for (int i=0; i<working_report.getGraphCount(); i++) {
                Graph working_graph = working_report.getGraph(i);
                if (!override_timespan.equals("none")) { 
                    working_graph.setTimespan(override_timespan); 
                }
                if (!override_graphtype.equals("none")) { 
                    working_graph.setGraphtype(override_graphtype); 
                }
            }
        }
    }        
    else { 
        if (!report_action.equals("Exit")) {
            throw new ServletException ("Invalid Parameter contents for report_action");
        }
    }  

%>

<html>
<head>
</head>

<body> 


<h3 align="center">Processing Form</h3>
<h3>Action: <%=report_action%></h3>
<h3>Report Index: <%=report_index%></h3>

<% if (report_action.equals("Update")) { %>
       <form name="do_next" method="get" action="custom_view.jsp">
           <input type="hidden" name="report" value="<%=report_index%>" >
           <input type="hidden" name="type" value="<%=report_type%>" >
           <% if (override_timespan != null) { %> 
               <input type="hidden" name="timespan" value="<%=override_timespan%>" >
           <% } %>
           <% if (override_graphtype != null) { %> 
               <input type="hidden" name="graphtype" value="<%=override_graphtype%>" >
           <% } %>
       </form>
       <script>
           document.do_next.submit();
       </script>
<% } %> 
<% if (report_action.equals("Customize")) { %> 
       <form name="do_next" method="get" action="custom_report.jsp">
           <input type="hidden" name="report" value="<%=report_index%>" >
       </form>
       <script>
           document.do_next.submit();
       </script>
<% } %> 
<% if (report_action.equals("Exit")) { %> 
       <script>
           window.location="index.jsp";
       </script>
<% } %> 



</body>
</html>

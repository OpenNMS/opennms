<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.kscReports.*,org.opennms.netmgt.config.KSC_PerformanceReportFactory" %>

<%@ include file="include_init1.jsp" %>

<%
    // Get Form Variables
    int report_index = 0; 
    String report_action = request.getParameter("report_action");
    
    if (report_action == null) {
        throw new ServletException ("Missing Parameter report_action");
    }
      
    if ((report_action.equals("Customize")) || (report_action.equals("View")) || (report_action.equals("CreateFrom")) || (report_action.equals("Delete"))) {
        String r_index = request.getParameter("report");
        if (r_index == null) {
            throw new ServletException ("Missing Parameter report");
        } 
        report_index = Integer.parseInt(r_index);
        if ((report_action.equals("Customize")) || (report_action.equals("CreateFrom"))) {  
            // Go ahead and tell report factory to put the report config into the working report area
            this.reportFactory.loadWorkingReport(report_index);
            if (report_action.equals("CreateFrom")) {  // Need to set index to -1 for this case to have Customizer create new report index 
               this.reportFactory.setWorkingReportIndex(-1);
            }
        }
        if (report_action.equals("Delete")) {  // Take care of this case right now
            this.reportFactory.deleteReportAndSave(report_index); 
        }
    }        
    else { 
        if (report_action.equals("Create")) {
            report_index = -1;
           // Go ahead and tell report factory to put the report config (a blank config) into the working report area
           this.reportFactory.loadWorkingReport(report_index);
        }
        else {
            throw new ServletException ("Invalid Parameter contents for report_action");
        }
    }  

%>

<html>
<head>
</head>

<body> 

<% if (report_action.equals("View")) { %>
       <form name="do_next" method="get" action="custom_view.jsp">
           <input type="hidden" name="report" value="<%=report_index%>" >
           <input type="hidden" name="type" value="custom" >
       </form>
       <script>
           document.do_next.submit();
       </script>
<% } 
   else { 
       if ((report_action.equals("Customize")) || (report_action.equals("Create")) || (report_action.equals("CreateFrom"))) { %> 
           <script>
               window.location="custom_report.jsp";
           </script>
<%     } 
       else { %>
           <script>
               window.location="index.jsp";
           </script>
<%     } 
   } %> 

</body>
</html>

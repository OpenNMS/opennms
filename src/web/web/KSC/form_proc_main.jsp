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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.*,org.opennms.netmgt.config.categories.*, java.util.*"  %>
<html>
<head>
  <title>Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Reports"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Reports" />
  <jsp:param name="location" value="report" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br> 

<!-- Body -->
<table>
    <tr>
        <td>&nbsp;</td>
        
        <td valign="top">
            <h3>Reports</h3>

            <!-- Performance Reports -->    
            <p><a href="performance/index.jsp">Performance Reports</a></p>

            <!-- Availability Report -->
            <p><a href="availability/index.jsp">Availability Reports</a></p>

            <!-- Response Time Report -->
            <p><a href="response/index.jsp">Response Time Reports</a></p>
            <!-- more reports will follow -->
        </td>
        
        <td>&nbsp;</td>
         
        <td width="60%" valign="top">
            <h3>Descriptions</h3>
            
            <p><b>Performance Reports</b> provide a way to easily 
                visualize the critical SNMP data collected from managed nodes throughout
                your network.  
            </p>
    
            <p><b>Availability Reports</b> provide graphical or numeric
                view of your service level metrics for the current
                month-to-date, previous month, and last twelve months by categories.
                The graphical PDF report is generated automatically on Sundays at 12:00 
                AM and is emailed to the <i>Reporting</i> group.
            </p>
            
            <p><b>Response Time Reports</b> provide a way to easily 
                visualize the response time data collected from managed nodes throughout
                your network.  
            </p>
            
         </td>
         
         <td>&nbsp;</td>
    </tr>        
</table>
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="report" />
</jsp:include>

</body>
</html>

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
// 2003 Feb 04: Added Key SNMP Custom Performance Reports.
// 2003 Jan 27: Removed reference to e-mailing reports.
// 2002 Nov 12: Added response time reports to webUI. Based on original
//              performance reports.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.*,org.opennms.netmgt.config.categories.*, java.util.*"  %>
<html>
<head>
  <title>Reports | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "Reports"; %>
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

            <!-- KSC Performance Reports and Node Reports -->
            <p><a href="KSC/index.jsp">KSC Performance Reports and Node Reports</a></p>

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

            <p><b>Key SNMP Customized (KSC) Performance Reports and Node Reports</b>
                KSC reports allow the user to create and view SNMP performance data using prefabricated graph types.
                The reports provide a great deal of flexibility in timespans and graphtypes. KSC report configurations may be saved allowing
                the user to define key reports that may be referred to at future dates.  Node reports show SNMP data for all
                SNMP interfaces on a node. Node reports may be loaded into the customizer and saved as a KSC report.
            </p>
    
            <p><b>Availability Reports</b> provide graphical or numeric
                view of your service level metrics for the current
                month-to-date, previous month, and last twelve months by categories.
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

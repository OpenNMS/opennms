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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2002 Nov 12: Add response time graphs to webUI.
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

--%>

<%@page language="java" contentType="text/html" session="true"  %>

<html>
<head>
  <title>OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<!--
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
-->
<body>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Web Console" />
</jsp:include>

<br> 

<!-- Body -->
<!--
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr> 
    <td>&nbsp;</td>
-->
  
    <!-- Column 1 of Body -->  
    <div id="index-contentleft">

<!--
    <td width="25%" valign="top" >
-->
      <!-- Services down box -->
      <jsp:include page="/outage/servicesdown-box.htm" flush="false" />
    </div>

<!--
    </td>
    
    <td>&nbsp;</td>
-->

    <!-- Middle Column -->
    <div id="index-contentmiddle">
<!--
    <td valign="top">
-->
      <!-- category box(es) -->    
      <jsp:include page="/includes/categories-box.jsp" flush="false" />                
    </div>

      <!--desktop query box -->  
<!--
    </td>

    <td>&nbsp;</td>
-->

    <!-- Column 3 of Body -->  
    <div id="index-contentright">
<!--
    <td width="25%" valign="top" >
-->
      <!-- notification box -->    
      <jsp:include page="/includes/notification-box.jsp" flush="false" />    
            
      <br>
      
      <!-- Performance box -->    
      <jsp:include page="/includes/performance-box.jsp" flush="false" />

        <br>
      
      <!-- Response Time box -->    
      <jsp:include page="/includes/response-box.jsp" flush="false" />

        <br>
      
      <!-- KSC Reports box -->    
      <jsp:include page="/includes/ksc-box.jsp" flush="false" />
      
        <br>

        <!-- security box -->    
      <%--
        Commenting out the security box include until it is functional
        <jsp:include page="/includes/security-box.jsp" flush="false" />
      --%>
    </div>

<!--
    </td>
    
    <td>&nbsp;</td>   
  </tr>
</table>
-->
<br>
<jsp:include page="/includes/charts.jsp" flush="false" />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

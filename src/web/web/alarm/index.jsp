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
// 2003 Feb 04: Added a check to pralarm null entries in queries. Bug #536.
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
//      http://www.opennms.com///

-->

<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Alarms | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />

</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "Alarms"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarms" />
  <jsp:param name="location" value="alarm" />  
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">     
      <h3>Alarm Queries</h3>
      
      <jsp:include page="/alarm/querypanel.jsp" flush="false" />

      <p><a href="alarm/list" title="View all outstanding alarms">View all alarms</a></p>
      <p><a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></p>

      <p>      
        <table width="50%" border="0" cellpadding="2" cellspacing="0" >
          <tr>
            <td colspan="2">Get&nbsp;details&nbsp;for&nbsp;Alarm&nbsp;ID:</td>
          </tr>
          <tr>
            <form action="alarm/detail.jsp" method="GET">          
              <td><input type="TEXT" NAME="id" /></td>
              <td><input type="submit" value="Search"/></td>                
            </form>
          </tr>                    
        </table>
      </p>
      
    </td>

    <td>&nbsp;</td>

    <td valign="top" width="60%">
      <h3>Outstanding and acknowledged alarms</h3>

      <p>Alarms can be <em>acknowledged</em>, or removed from the view of other users, by
        selecting the alarm in the <em>Ack</em> check box and clicking the <em>Acknowledge
        Selected Alarms</em> at the bottom of the page.  Acknowledging an alarm gives
        users the ability to personally take responsibility for addressing a network
        or systems-related issue.  Any alarm that has not been acknowledged is
        active in all users' browsers and is considered <em>outstanding</em>.
      </p>
            
      <p>If an alarm has been acknowledged in error, you can select the appropriate
        <em>View all acknowledged alarms</em> link, find the alarm, and <em>unacknowledge</em> it,
        making it available again to all users' views.
      </p>
        
      <p>If you have a specific alarm identifier for which you want a detailed alarm
        description, type the identifier into the <em>Get details for Alarm ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
    </td>

     <td> &nbsp; </td>
  </tr>
</table>                                    
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="alarm" />
</jsp:include>

</body>
</html>

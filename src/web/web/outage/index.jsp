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
// 2003 Apr 07: Corrected small typo.
// 2003 Feb 07: Fixed URLEncoder issues.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Outages | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = "Outages"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outages" />
  <jsp:param name="location" value="outages" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />  
</jsp:include>

<br />
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Outage Menu</h3>    

      <p><a href="outage/current.jsp">View current outages</a></p>
      <p><a href="outage/list">View all outages</a></p>
      
      <p>
        <form method="GET" action="outage/detail.jsp" >
          Get&nbsp;details&nbsp;for&nbsp;Outage&nbsp;ID:<br />
          <input type="text" name="id" />
          <input type="submit" value="Search" />
        </form>      
      </p>
    </td>
    
    <td>&nbsp;</td>

    <td valign="top" width="60%" >
      <h3>Outages and Service Level Availability</h3>

      <p>
        Outages are tracked by OpenNMS by polling services that have been
        discovered.  If the service does not respond to the poll, a service outage
        is created and service level availbility levels are impacted.  Service 
        outages created notifications. 
      </p>     
    </td>

    <td>&nbsp;</td>
  </tr>
</table>                                    
                                     
<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

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
// 2003 Jan 31: Added RRA information to poller packages.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.blast.com///

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*" %>

<html>
<head>
  <title>Outage | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href ='report/index.jsp'>Reports</a>"; %>
<% String breadcrumb2 = "<a href ='availability/index.jsp'>Availability Report</a>"; %>
<% String breadcrumb3 = "No Email"; %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="No Email" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br />

<!-- page title -->
<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr><td>No Email Address Configured</td></tr>
</table>

<br />

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>
      <p>
        The Availability report you requested could not be generated because you
        do not have an email address configured.  You will need to have your
        system administrator setup a valid email address for you.  Then, when
        you request this outage report, it will be emailed to you.
      </p>
      
      <p>
        <a href="availability/index.jsp">Go back to the Availability Report page</a>
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br />

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="report" />
</jsp:include>

</body>
</html>

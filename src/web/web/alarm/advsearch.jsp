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
  <title>Advanced Alarm Search | Alarms | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='alarm/index.jsp'>Alarms</a>"; %>
<% String breadcrumb2 = "Advanced Alarm Search"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Advanced Alarm Search" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br />
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td width="10">&nbsp;</td>

    <td valign="top">
      <h3>Advanced Alarm Search</h3>

      <jsp:include page="/alarm/advquerypanel.jsp" flush="false" />
    </td>

    <td width="20">&nbsp;</td>

    <td width="50%" valign="top">
      <h3>Searching Instructions</h3>

      <p>The <strong>Advanced Alarm Search</strong> page can be used to search the alarm list on
      multiple fields. Fill in values for each field that you wish to use to narrow down
      the search.</p>

      <p>To select alarms by time, first check the box for the time range
      that you wish to limit and then fill out the time in the boxes provided.</p>

      <p>If you wish to select alarms within a specific time span, check <em>both</em>
      boxes and enter the beginning and end of the range in the boxes provided.</p>
    </td>

    <td width="10">&nbsp;</td>
  </tr>
</table>

<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

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
//      http://www.opennms.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.Date" %>
<html>
<head>
  <title>Users and Groups | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Users and Groups"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Users and Groups" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Users and Groups</h3>

      <p>
        <a HREF="admin/userGroupView/users/list.jsp">Configure Users</a>
      </p>
      <p>
        <a HREF="admin/userGroupView/groups/list.jsp">Configure Groups</a>
      </p>
      <p>
        <a HREF="admin/userGroupView/roles">Configure Roles</a>
      </p>
      <!--
      <p>
        <a HREF="admin/userGroupView/views/list.jsp">Configure Views</a>
      </p>
      -->
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Users</h3>
      <p>
        Add new <em>Users</em>, change user names and passwords, and edit notification information.
      </p>

      <h3>Groups</h3>
      <p>
        Assign and unassign <em>Users</em> to <em>Groups</em>.
      </p>

      <h3>Roles</h3>
      <p>
        Configure Roles that define On Call schedules for users.
      </p>
      <!--
      <h3>Views</h3>
      <p>
        Assign and unassign <em>Users</em> and <em>Groups</em> to <em>Views</em>.
      </p>
      -->

    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>
</body>
</html>

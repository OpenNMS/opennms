<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="" %>

<html>
<head>
  <title>Import/Export | Assets | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Import/Export Assets"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Import/Export Assets" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Import and Export Assets</h3>

      <p>
        <a href="admin/asset/import.jsp">Import Assets</a>
      </p>

      <p>
        <a href="admin/asset/assets.csv">Export Assets</a>
      </p>
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Importing Asset Information</h3>

      <p>
        The asset import page imports a comma-separated value file (.csv),
        (probably exported from spreadsheet) into the assets database.
      </p>

      <h3>Exporting Asset Information</h3>

      <p>
        All the nodes with asset information will be exported to a 
        comma-separated value file (.csv), which is suitable for use in a 
        spreadsheet application. 
      </p>
    </td>
    
    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>

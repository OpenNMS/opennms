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
// 2004 Jan 06: Added support for Display, Notify, Poller and Threshold categories
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Import Assets | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/asset/index.jsp'>Import/Export Assets</a>"; %>
<% String breadcrumb3 = "Import"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Import Assets" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h3>Assets</h3>
      <p>Paste your comma-seperated values into this text field to import
         them into the assets database.  There is one line per record, and 
         the fields are delimited by commas.
      </p>
      <form action="admin/asset/import" method="POST">
        <textarea name="assetsText" cols="80" rows="25" wrap="off" ></textarea>
        <br>
        <input type="submit" value="Import"/>
      </form>

      <p>The asset fields are (in order):
        <table width="100%" cellspacing="0" cellpadding="2" border="0">
          <tr>
            <td colspan="33%">
              <ol>
                <li> NodeLabel (for display only)
                <li> NodeId (database identifier, integer)
                <li> Category
                <li> Manufacturer
                <li> Vendor
                <li> ModelNumber
                <li> SerialNumber
                <li> Description
                <li> CircuitId
                <li> AssetNumber
                <li> OperatingSystem
                <li> Rack
		<li> Slot
              </ol>
            </td>
            <td colspan="33%">
              <ol start="14">
                <li> Port
                <li> Region
                <li> Division
                <li> Department
                <li> Address1
                <li> Address2
                <li> City
                <li> State
                <li> Zip
                <li> Building
                <li> Floor
                <li> Room
                <li> VendorPhone
              </ol>
            </td>
            <td colspan="33%">
              <ol start="27">
                <li> VendorFax
                <li> DateInstalled
                <li> Lease
                <li> LeaseExpires
                <li> SupportPhone
                <li> MaintContract
                <li> VendorAssetNumber
                <li> MaintContractExpires
		<li> Display Category
		<li> Notification Category
		<li> Poller Category
		<li> Threshold Category
                <li> Comments
              </ol>
            </td>
          </tr>
        </table>
      </p>
    </td>
    
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

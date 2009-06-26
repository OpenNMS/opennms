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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Import Assets" />
  <jsp:param name="headTitle" value="Import Assets" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/asset/index.jsp'>Import/Export Assets</a>" />
  <jsp:param name="breadcrumb" value="Import" />
</jsp:include>

<h3>Assets</h3>

<p>
  Paste your comma-separated values into this text field to import
  them into the assets database.  There is one line per record, and 
  the fields are delimited by commas.
</p>

<form action="admin/asset/import" method="post">
  <textarea name="assetsText" cols="80" rows="25" wrap="off" ></textarea>

 <% if (request.getParameter("showMessage") != null && request.getParameter("showMessage").equalsIgnoreCase("true")) { %>
 <p>
 <span class="error"><%= request.getSession(false).getAttribute("message") %></span>
 </p>
 <% } %>

 <p>
  <input type="submit" value="Import"/>
 </p>
</form>

<br />

<p>
  The asset fields are (in order):
</p>

	<div style="width: 33%; position: relative; float: left">
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
                <li> Port
              </ol>
	</div>

	<div style="width: 33%; position: relative; float: left">
              <ol start="15">
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
                <li> VendorFax
                <li> DateInstalled
              </ol>
	</div>

	<div style="width: 33%; position: relative; float: left">
              <ol start="29">
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
                <li> Username
                <li> Password
                <li> Enable
                <li> Connection
                <li> Auto Enable
                <li> Comments
              </ol>
	</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

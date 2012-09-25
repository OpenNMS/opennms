<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

	<div style="width: 25%; position: relative; float: left">
              <ol>
                <li> NodeLabel (for display only)
                <li> NodeId (database identifier, integer)
                <li> (64) Category
                <li> (64) Manufacturer
                <li> (64) Vendor
                <li> (64) ModelNumber
                <li> (64) SerialNumber
                <li> (128) Description
                <li> (64) CircuitId
                <li> (64) AssetNumber
                <li> (64) OperatingSystem
                <li> (64) Rack
				<li> (64) Slot
                <li> (64) Port
              </ol>
	</div>

	<div style="width: 25%; position: relative; float: left">
              <ol start="15">
                <li> (64) Region
                <li> (64) Division
                <li> (64) Department
                <li> (256) Address1
                <li> (256) Address2
                <li> (64) City
                <li> (64) State
                <li> (64) Zip
                <li> (64) Building
                <li> (64) Floor
                <li> (64) Room
                <li> (64) VendorPhone
                <li> (64) VendorFax
                <li> (64) DateInstalled
              </ol>
	</div>

	<div style="width: 25%; position: relative; float: left">
              <ol start="29">
                <li> (64) Lease
                <li> (64) LeaseExpires
                <li> (64) SupportPhone
                <li> (64) MaintContract
                <li> (64) VendorAssetNumber
                <li> (64) MaintContractExpires
				<li> (64) Display Category
				<li> (64) Notification Category
				<li> (64) Poller Category
				<li> (64) Threshold Category
                <li> (32) Username
                <li> (32) Password
                <li> (32) Enable
                <li> (32) Connection
                <li> (1) Auto Enable
                <li> Comments
              </ol>
	</div>
	
		<div style="width: 25%; position: relative; float: left">
              <ol start="45">
                <li> (64) Cpu
                <li> (10) Ram
                <li> (64) Storage Controller
                <li> (64) HDD 1
                <li> (64) HDD 2
                <li> (64) HDD 3
				<li> (64) HDD 4
				<li> (64) HDD 5
				<li> (64) HDD 6
				<li> (1) Number of power supplies
                <li> (11) Inputpower
                <li> (64) Additional hardware
                <li> (32) Admin
                <li> (32) SNMP Community
                <li> (2) Rack unit height
              </ol>
	</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

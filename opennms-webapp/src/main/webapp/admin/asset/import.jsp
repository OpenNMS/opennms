<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Import Assets" />
  <jsp:param name="headTitle" value="Import Assets" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/asset/index.jsp'>Import/Export Assets</a>" />
  <jsp:param name="breadcrumb" value="Import" />
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Assets</h3>
  </div>
  <div class="panel-body">
<p>
  Paste your comma-separated values into this text field to import
  them into the assets database.  There is one line per record, and 
  the fields are delimited by commas.
</p>

<form role="form" action="admin/asset/import" method="post">
 <div class="form-group">
  <textarea name="assetsText" class="form-control" rows="25" wrap="off" ></textarea>
 </div>

 <% if (request.getParameter("showMessage") != null && request.getParameter("showMessage").equalsIgnoreCase("true")) { %>
 <p>
 <span class="text-danger"><%= request.getSession(false).getAttribute("message") %></span>
 </p>
 <% } %>

 <div class="form-group">
  <input type="submit" class="btn btn-default" value="Import"/>
 </div>
</form>

<br />

<p>
  The asset fields are (in order):
</p>

  <div class="row">
	<div class="col-md-3">
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
	</div> <!-- column -->

	<div class="col-md-3">
              <ol start="15">
                <li> (64) Region
                <li> (64) Division
                <li> (64) Department
                <li> (256) Address1
                <li> (256) Address2
                <li> (64) City
                <li> (64) State
                <li> (64) ZIP Code
                <li> (64) Building
                <li> (64) Floor
                <li> (64) Room
                <li> (64) VendorPhone
                <li> (64) VendorFax
                <li> (64) DateInstalled
              </ol>
	</div> <!-- column -->

	<div class="col-md-3">
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
	</div> <!-- column -->

	<div class="col-md-3">
              <ol start="45">
                <li> (64) CPU
                <li> (10) RAM
                <li> (64) Storage Controller
                <li> (64) HDD 1
                <li> (64) HDD 2
                <li> (64) HDD 3
		<li> (64) HDD 4
		<li> (64) HDD 5
		<li> (64) HDD 6
		<li> (1) Number of power supplies
                <li> (11) Input power
                <li> (64) Additional hardware
                <li> (32) Admin
                <li> (32) SNMP Community
                <li> (2) Rack unit height
              </ol>
	</div> <!-- column -->
    </div> <!-- row -->
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

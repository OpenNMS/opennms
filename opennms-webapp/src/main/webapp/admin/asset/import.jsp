<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Import Assets")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Import/Export Assets", "admin/asset/index.jsp")
          .breadcrumb("Import")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Assets</span>
  </div>
  <div class="card-body">
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
  <input type="submit" class="btn btn-secondary" value="Import"/>
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
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

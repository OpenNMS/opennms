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
          .headTitle("Import/Export")
          .headTitle("Assets")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Import/Export Assets")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Import and Export Assets</span>
      </div>
      <div class="card-body">
        <p>
          <a href="admin/asset/import.jsp">Import Assets</a>
        </p>
        <p>
          <a href="admin/asset/assets.csv">Export Assets</a>
        </p>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Importing Asset Information</span>
      </div>
      <div class="card-body">
        <p>
          The asset import page imports a comma-separated value file (.csv),
          (probably exported from spreadsheet) into the assets database.
        </p>
      </div>
    </div> <!-- panel -->

    <div class="card">
      <div class="card-header">
        <span>Exporting Asset Information</span>
      </div>
      <div class="card-body">
        <p>
          All the nodes with asset information will be exported to a
          comma-separated value file (.csv), which is suitable for use in a
          spreadsheet application.
        </p>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
          .headTitle("Users and Groups")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Users and Groups</span>
      </div>
      <div class="card-body">
        <p>
          <a HREF="admin/userGroupView/users/list.jsp">Configure Users</a>
        </p>
        <p>
          <a HREF="admin/userGroupView/groups/list.htm">Configure Groups</a>
        </p>
        <p>
          <a HREF="admin/userGroupView/roles">Configure On-Call Roles</a>
        </p>
        <!--
        <p>
          <a HREF="admin/userGroupView/views/list.jsp">Configure Views</a>
        </p>
        -->
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Users</span>
      </div>
      <div class="card-body">
        <p>
          Add new <em>Users</em>, change user names and passwords, and edit notification information.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->

    <div class="card">
      <div class="card-header">
        <span>Groups</span>
      </div>
      <div class="card-body">
        <p>
          Assign and unassign <em>Users</em> to <em>Groups</em>.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->

    <div class="card">
      <div class="card-header">
        <span>Roles</span>
      </div>
      <div class="card-body">
        <p>
          Configure Roles that define On Call schedules for users.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

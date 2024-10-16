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

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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Users and Groups" />
  <jsp:param name="headTitle" value="Users and Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Users and Groups" />
</jsp:include>

<div class="TwoColLAdmin" >
      <h3>Users and Groups</h3>

      <p>
        <a HREF="admin/userGroupView/users/list.jsp">Configure Users</a>
      </p>
      <p>
        <a HREF="admin/userGroupView/groups/list.htm">Configure Groups</a>
      </p>
      <p>
        <a HREF="admin/userGroupView/roles">Configure Roles</a>
      </p>
      <!--
      <p>
        <a HREF="admin/userGroupView/views/list.jsp">Configure Views</a>
      </p>
      -->
</div>

<div  class="TwoColRAdmin">
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
</div>


<jsp:include page="/includes/footer.jsp" flush="true"/>

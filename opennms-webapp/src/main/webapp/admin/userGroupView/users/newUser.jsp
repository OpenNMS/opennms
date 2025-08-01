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
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %><%--
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

<%@page language="java" contentType="text/html" session="true"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("New")
          .headTitle("Users")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("User List", "admin/userGroupView/users/list.jsp")
          .breadcrumb("New User")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
  function validateFormInput() 
  {
    var id = new String(document.newUserForm.userID.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The user ID '" + document.newUserForm.userID.value + "' cannot be used. It may be confused with the administration user ID 'admin'.");
        return false;
    }

    if (/.*[&<>"`']+.*/.test(id)) {
        alert("The user ID must not contain any HTML markup.");
        return false;
    }

    if (document.newUserForm.pass1.value == document.newUserForm.pass2.value) 
    {
      return true;
    } 
    else
    {
      alert("The two password fields do not match!");
      document.newUserForm.pass1.value = "";
      document.newUserForm.pass2.value = "";
      return false;
    }
  }    
  function cancelUser()
  {
      window.location.href = "admin/userGroupView/users/list.jsp";
  }
</script>

<div class="card">
  <div class="card-header">
    <%if ("redo".equals(request.getParameter("action"))) { %>
      <span>The user <%= WebSecurityUtils.sanitizeString(request.getParameter("userID")) %> already exists.
        Please type in a different user ID.</span>
    <%} else { %>
      <span>Please enter a user ID and password below</span>
    <%}%>
  </div>
  <div class="card-body">
    <form class="form" role="form" id="newUserForm" method="post" name="newUserForm" onsubmit="return validateFormInput();" action="admin/userGroupView/users/addNewUser">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <div class="form-group">
        <label for="userID" class="">User ID</label>
        <input id="userID" type="text" name="userID" class="form-control">
      </div>

      <div class="form-group">
        <label for="pass1" class="">Password</label>
        <%-- Management of another user's password, so prevent autocomplete with `autocomplete="new-password"`.
         See MDN: https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password --%>
        <input id="pass1" type="password" name="pass1" class="form-control" autocomplete="new-password">
      </div>
      <div class="form-group">
        <label for="pass2" class="">Confirm Password</label>
        <%-- Management of another user's password, so prevent autocomplete with `autocomplete="new-password"`.
         See MDN: https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password --%>
        <input id="pass2" type="password" name="pass2" class="form-control" autocomplete="new-password">
      </div>
      <button type="submit" class="btn btn-secondary">OK</button>
      <button type="button" class="btn btn-secondary" onclick="cancelUser()">Cancel</button>
    </form>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

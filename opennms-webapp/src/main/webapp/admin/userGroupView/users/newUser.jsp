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

<%@page language="java" contentType="text/html" session="true"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="New User" />
	<jsp:param name="headTitle" value="New" />
	<jsp:param name="headTitle" value="Users" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/users/list.jsp'>User List</a>" />
	<jsp:param name="breadcrumb" value="New User" />
</jsp:include>

<script type="text/javascript">
  function validateFormInput() 
  {
    var id = new String(document.newUserForm.userID.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The user ID '" + document.newUserForm.userID.value + "' cannot be used. It may be confused with the administration user ID 'admin'.");
        return false;
    }
    
    if (document.newUserForm.pass1.value == document.newUserForm.pass2.value) 
    {
      document.newUserForm.action="admin/userGroupView/users/addNewUser";
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

<div class="panel panel-default">
  <div class="panel-heading">
    <%if ("redo".equals(request.getParameter("action"))) { %>
      <h3 class="panel-title">The user <%=request.getParameter("userID")%> already exists.
        Please type in a different user ID.</h3>
    <%} else { %>
      <h3 class="panel-title">Please enter a user ID and password below</h3>
    <%}%>
  </div>
  <div class="panel-body">
    <form class="form-horizontal" role="form" id="newUserForm" method="post" name="newUserForm" onsubmit="return validateFormInput();">
      <div class="form-group">
        <label for="userID" class="col-sm-2 control-label">User ID:</label>
        <div class="col-sm-10">
          <input id="userID" type="text" name="userID" class="form-control">
        </div>
      </div>
      <div class="form-group">
        <label for="pass1" class="col-sm-2 control-label">Password:</label>
        <div class="col-sm-10">
          <input id="pass1" type="password" name="pass1" class="form-control">
        </div>
      </div>
      <div class="form-group">
        <label for="pass2" class="col-sm-2 control-label">Confirm Password:</label>
        <div class="col-sm-10">
          <input id="pass2" type="password" name="pass2" class="form-control">
        </div>
      </div>
      <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
          <div class="btn-group" role="group">
            <button type="submit" class="btn btn-default">OK</button>
            <button type="button" class="btn btn-default" onclick="cancelUser()">Cancel</button>
          </div>
        </div>
      </div>
    </form>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

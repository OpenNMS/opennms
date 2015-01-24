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
  <jsp:param name="title" value="Change Password" />
  <jsp:param name="headTitle" value="Change Password" />
  <jsp:param name="breadcrumb" value="<a href='account/selfService/index.jsp'>Self-Service</a>" />
  <jsp:param name="breadcrumb" value="Change Password" />
</jsp:include>

<script type="text/javascript">
  function verifyGoForm() 
  {
    if (document.goForm.pass1.value == document.goForm.pass2.value) 
    {
      document.goForm.currentPassword.value=document.goForm.oldpass.value;
      document.goForm.newPassword.value=document.goForm.pass1.value;
      document.goForm.action="account/selfService/newPasswordAction";
      return true;
    } 
    else
    {
      alert("The two new password fields do not match!");
    }
}
</script>


<div class="row">
  <div class="col-md-4">
    <div class="panel panel-default">
       <div class="panel-heading">
        <h3 class="panel-title">Please enter the old and new passwords and confirm.</h3>
      </div>
      <div class="panel-body">
        <form role="form" method="post" name="goForm" onSubmit="verifyGoForm()">
          <input type="hidden" name="currentPassword" value="">
          <input type="hidden" name="newPassword" value="">
          <div class="form-group <% if ("redo".equals(request.getParameter("action"))) { %>has-error<% } %>">
            <label class="control-label" for="input_oldpass">Current Password:</label>
            <input type="password" class="form-control" id="input_oldpass" name="oldpass">
          </div>
          <div class="form-group">
            <label class="control-label" for="input_pass1">New Password:</label>
            <input type="password" class="form-control" name="pass1">
          </div>
          <div class="form-group">
            <label class="control-label" for="input_pass2">Confirm New Password:</label>
            <input type="password" class="form-control" name="pass2">
          </div>
          <button type="submit" class="btn btn-default">Submit</button>
          <a href="account/selfService/index.jsp" class="btn btn-default">Cancel</a>
        </form>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

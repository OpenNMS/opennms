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
      let newPassword=document.goForm.pass1.value
      const passwordRegex= /((?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%&.*+-]).{12,128})/;
      const sameCharacterRegex= /(.)\1{5}/;

      if(newPassword.match(passwordRegex) && !newPassword.match(sameCharacterRegex) )
      {
        document.goForm.currentPassword.value=document.goForm.oldpass.value;
        document.goForm.newPassword.value=document.goForm.pass1.value;
        return true;
      } else {
        alert("Password complexity is not correct! Please use at least 12 characters, consisting of 1 special character, 1 upper case letter, 1 lower case letter and 1 number. Identical strings with more than 6 characters in a row are also not allowed.");
      }
    }
    else
    {
      alert("The two new password fields do not match!");
    }
  }
</script>


<div class="row">
  <div class="col-md-4">
    <div class="card">
       <div class="card-header">
        <span>Please enter the old and new passwords and confirm.</span>
      </div>
      <div class="card-body">
        <form role="form" method="post" name="goForm" onSubmit="return verifyGoForm();" action="account/selfService/newPasswordAction">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <input type="hidden" name="currentPassword" value="">
          <input type="hidden" name="newPassword" value="">
          <div class="form-group">
            <label class="col-form-label" for="input_oldpass">Current Password</label>
            <input type="password" class="form-control <% if ("redo".equals(request.getParameter("action"))) { %>is-invalid<% } %>" id="input_oldpass" name="oldpass" autocomplete="off">
          </div>
          <div class="form-group">
            <label class="col-form-label" for="input_pass1">New Password</label>
            <input type="password" class="form-control" name="pass1" id="input_pass1" autocomplete="off">
          </div>
          <div class="form-group">
            <label class="col-form-label" for="input_pass2">Confirm New Password</label>
            <input type="password" class="form-control" name="pass2" id="input_pass2" autocomplete="off">
          </div>
          <button type="submit" class="btn btn-primary">Submit</button>
          <a href="account/selfService/index.jsp" class="btn btn-secondary">Cancel</a>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

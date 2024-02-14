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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@ page import="org.opennms.web.account.selfService.NewPasswordActionServlet" %>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Change Password")
          .breadcrumb("Self-Service", "account/selfService/index.jsp")
          .breadcrumb("Change Password")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
  function verifyGoForm() {
    if (document.goForm.pass1.value == document.goForm.pass2.value) {
      let newPassword = document.goForm.pass1.value
      const passwordRegex = /${fn:escapeXml(NewPasswordActionServlet.PASSWORD_REGEX)}/;
      const sameCharacterRegex = /${fn:escapeXml(NewPasswordActionServlet.SAME_CHARACTER_REGEX)}/;

      if (newPassword.match(passwordRegex) && !newPassword.match(sameCharacterRegex)) {
        document.goForm.currentPassword.value = document.goForm.oldpass.value;
        document.goForm.newPassword.value = document.goForm.pass1.value;
        return true;
      } else {
        alert("Password complexity is not correct! Please use at least 12 characters, consisting of 1 special character, 1 upper case letter, 1 lower case letter and 1 number. Identical strings with more than 6 characters in a row are also not allowed.");
        return false;
      }
    }
    else
    {
      alert("The two new password fields do not match!");
      return false;
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

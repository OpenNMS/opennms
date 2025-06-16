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
          .headTitle("New Password")
          .headTitle("Users")
          .headTitle("Admin")
          .flags("nobreadcrumbs", "quiet")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />


<script type="text/javascript">
  function verifyGoForm() 
  {
    if (document.goForm.pass1.value == document.goForm.pass2.value) 
    {
      window.opener.document.modifyUser.password.value=document.goForm.pass1.value;
      
      window.close();
    } 
    else
    {
      alert("The two password fields do not match!");
    }
}
</script>

<div class="card">
  <div class="card-header">
    <span>Please enter a new password and confirm</span>
  </div>
  <div class="card-body">
    <form role="form" class="form" method="post" name="goForm">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

      <div class="form-group">
        <label for="pass1" class="">Password</label>
        <%-- Management of another user's password, so prevent autocomplete with `autocomplete="new-password"`.
             See MDN: https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password --%>
        <input type="password" class="form-control" id="pass1" name="pass1" autocomplete="new-password">
      </div>

      <div class="form-group">
        <label for="pass2" class="">Confirm Password</label>
        <%-- Management of another user's password, so prevent autocomplete with `autocomplete="new-password"`.
             See MDN: https://developer.mozilla.org/en-US/docs/Web/Security/Securing_your_site/Turning_off_form_autocompletion#preventing_autofilling_with_autocompletenew-password --%>
        <input type="password" class="form-control" id="pass2" name="pass2" autocomplete="new-password">
      </div>

      <button type="button" class="btn btn-secondary" onclick="verifyGoForm()">OK</button>
      <button type="button" class="btn btn-secondary" onclick="window.close()">Cancel</button>
    </form>
    <p class="alert alert-warning mt-2">
      Note: Be sure to click "Finish" at the bottom of the user page to save
      changes.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" >
  <jsp:param name="quiet" value="true" />
</jsp:include>

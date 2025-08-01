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
<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Password Gate" />
  <jsp:param name="quiet" value="true" />
</jsp:include>

<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page
  language="java"
  contentType="text/html"
  session="true"
  import="org.opennms.web.account.selfService.PasswordGateActionServlet"
%>

<%
    // Horizon values - these are overridden in Meridian
    String wallpaperBgImagePath = "images/wallpapers/background_dark.png";
    String titleLogoImagePath = "images/opennms_horizon_title.svg";
    String labelTextColor = "#fff";
%>

<style type="text/css">
  .login-page {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('<%= wallpaperBgImagePath %>');
    background-size: cover;
  }

  .form-content-wrapper {
    margin-left: 10%;
    padding-left: 6px;
  }

  .form-input-wrapper {
    max-width: 300px;
  }

  .form-group {
    margin-bottom: 1.25rem;
  }

  .form-group.buttons {
    margin-top: 1.5rem;
  }

  .login-form {
    max-width: 480px;
  }

  .card {
    background-color: transparent;
    border-color: transparent;
    margin-left: 10%;
    margin-top: 10%;
  }

  input.form-control {
    font-size: 12px;
    padding: 10px;
    color: black;
    width: 225px;
    height: 32px;
    border-radius: 5px;
    outline: none;
    border: 2px solid rgba(97, 215, 231, 0.829);
    background-color: rgba(255, 255, 255, 0.623);
  }

  button {
    color: black;
    padding: 7px;
    padding-left: 28px;
    padding-right: 28px;
    font-size: 11px;
    border-radius: 30px;
    background-image: linear-gradient(to right, rgb(67, 194, 233), rgb(137, 230, 194));
    border: none;
  }

  button:hover {
    background-image: linear-gradient(to right, rgb(61, 168, 200), rgb(116, 187, 160));
  }

  .btn {
    border-radius: 1.25rem;
    color: #000;
    font-size: 11px;
  }

  .btn-primary, .btn-secondary {
    min-width: 80px;
  }

  .btn-secondary {
    margin-left: 4px;
    background-image: linear-gradient(to right, rgb(84, 142, 131), rgb(104, 156, 139));
  }

  .horizon {
    margin-left: 20%;
  }

  label.pg-text, span.pg-text {
    color: <%= labelTextColor %>;
  }

  a.pg-link, a.pg-link:visited {
    color: rgb(97, 184, 195);
    text-decoration: none;
  }

  a.pg-link:hover {
    color: rgb(90, 160, 173);
    text-decoration: underline;
  }
</style>

<%
  // Spring Security remembers the requested URL and redirects after a successful login.
  // If the session_expired parameter is set, it is known that this is a Javascript redirect.
  // In this case, we remove the remembered request attribute so the user is forwarded to the login page instead.
  if (request.getParameter("session_expired") != null
    && request.getParameter("session_expired").equals("true")
    && session != null) {
    session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
  }
%>
<script type="text/javascript">
  function verifyGoForm(event) {
    if (event.submitter.id === 'btn_skip') {
        return true;
    }

    if (document.goForm.pass1.value == document.goForm.pass2.value) {
      let newPassword = document.goForm.pass1.value
      const passwordRegex = /${fn:escapeXml(PasswordGateActionServlet.PASSWORD_REGEX)}/;
      const sameCharacterRegex = /${fn:escapeXml(PasswordGateActionServlet.SAME_CHARACTER_REGEX)}/;

      if (newPassword.match(passwordRegex) && !newPassword.match(sameCharacterRegex)) {
        document.goForm.currentPassword.value = document.goForm.oldpass.value;
        document.goForm.newPassword.value = document.goForm.pass1.value;
        return true;
      } else {
        alert("Password complexity is not correct! Please use at least 12 characters, consisting of 1 special character, 1 upper case letter, 1 lower case letter and 1 number. Identical strings with 6 or more characters in a row are also not allowed.");
        return false;
      }
    } else {
      alert("The two new password fields do not match!");
      return false;
    }
  }

  window.onload = function() {
    var oldPass = document.getElementById("loginForm:input_oldpass") || document.getElementById("input_oldpass");
    var pass1 = document.getElementById("loginForm:input_pass1") || document.getElementById("input_pass1");
    var pass2 = document.getElementById("loginForm:input_pass2") || document.getElementById("input_pass2");

    oldPass.value = '';
    pass1.value = '';
    pass2.value = '';
  }
</script>

<div class="login-page">
    <div class="card login-form rounded">
        <div style="padding-bottom: 36px; padding-top: 60px">
            <img src="<%= titleLogoImagePath %>" class="horizon" width="185px" />
        </div>
        <div class="form-content-wrapper">
            <form role="form" method="post" name="goForm" onSubmit="return verifyGoForm(event);" action="account/selfService/passwordGateAction">
                <div class="form-content">
                    <div class="form-group">
                        <span class="pg-text">
                            Please take a moment to change your <em>admin</em> user password from its default value.
                            This step helps protect your installation against
                            <a class="pg-link" target="_blank" rel="noopener" href="https://www.cisa.gov/news-events/cybersecurity-advisories/aa22-137a">default credential attacks</a>.
                        </span>
                    </div>
                    <div class="form-input-wrapper">
                        <div class="form-group">
                            <label class="col-form-label pg-text" for="input_oldpass">Current Password</label>
                            <input type="password" class="form-control <% if ("redo".equals(request.getParameter("action"))) { %>is-invalid<% } %>" id="input_oldpass" name="oldpass" autocomplete="off">
                        </div>
                        <div class="form-group">
                            <label class="col-form-label pg-text" for="input_pass1">New Password</label>
                            <input type="password" class="form-control" name="pass1" id="input_pass1" autocomplete="off">
                        </div>
                        <div class="form-group">
                            <label class="col-form-label pg-text" for="input_pass2">Confirm New Password</label>
                            <input type="password" class="form-control" name="pass2" id="input_pass2" autocomplete="off">
                        </div>
                        <div class="form-group buttons">
                            <button type="submit" id="btn_change_password" name="btn_change_password" class="btn btn-primary">Change Password</button>
                            <button type="submit" id="btn_skip" name="btn_skip" formaction="account/selfService/passwordGateAction?skip=1" class="btn btn-secondary">Skip</button>
                        </div>
                    </div>
                    <div class="card-body">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <input type="hidden" name="currentPassword" value="">
                        <input type="hidden" name="newPassword" value="">
                    </div>
                </div>
            </form>
        </div>
    </div>
    <div class="" style="position: absolute; bottom: 0px; right: 10px; font-size: 3em; padding: 20pt 20pt 5pt 20pt">
        <div style="padding-bottom: 20px; padding-top: 20px">
            <img src="images/opennms-logo-light.svg" class="" width="180px" />
        </div>
    </div>
</div>

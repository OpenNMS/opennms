<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

<style type="text/css">
  .login-page {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('images/wallpapers/background_dark.png');
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
    border:2px solid rgba(97, 215, 231, 0.829);
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
    color: #fff;
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
            <img src="images/opennms_horizon_title.svg" class="horizon" width="185px" />
        </div>
        <div class="form-content-wrapper">
            <form role="form" method="post" name="goForm" onSubmit="return verifyGoForm(event);" action="account/selfService/passwordGateAction">
                <div class="form-content">
                    <div class="form-group">
                        <span class="pg-text">You landed here because you have not changed your "admin" password from the default value.
                        It is highly recommended that you do so.
                        You may also click "Skip" to skip this step for now.</span>
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

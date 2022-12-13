<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Login" />
  <jsp:param name="quiet" value="true" />
</jsp:include>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core'%>
<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
<jsp:include page="/includes/mobile-app-promo.jsp" flush="false" />

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

  .form-group{
    margin-bottom: 1.25rem;
  }

  .login-form {
    max-width: 360px;
  }

  .card {
    background-color: transparent;
    border-color: transparent;
    margin-left: 10%;
    margin-top: 15%;
  }

  input {
    font-size: 12px;
    padding: 10px;
    color: black;
    width: 225px;
    height: 32px;
    border-radius: 5px;
    outline: none;
    border:2px solid rgba(97, 215, 231, 0.829);
    background-color: rgba(255, 255, 255, 0.623);
    margin-left: 21%;
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
    margin-left: 21%;
    margin-top: 20px;
  }

  button:hover {
    background-image: linear-gradient(to right, rgb(61, 168, 200), rgb(116, 187, 160));
  }

  .horizon {
      margin-left: 30%;
    }

  #login-attempt-failed {
    margin-top: 10px;
    margin-left: 21%;
    width: 225px;
    font-size: 9.5pt;
  }

  #login-expired {
    margin-top: 10px;
    margin-left: 21%;
    width: 225px;
    font-size: 9.5pt;
  }

  .alert {
    position: relative;
    padding: 0.5rem 0.5rem;
    border: 1px solid transparent;
    border-radius: 0.4rem;
  }

  .alert-warning {
    color: #495057;
    background-color: #d8c999;
    border-color: #e7b51e;
  }

  .alert-danger {
    color: #495057;
    background-color: #ffe5e7;
    border-color: #f15b65;
  }
  p {
    margin-bottom:0px;
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
<script>
  window.onload = function() {
    var username = document.getElementById("loginForm:input_j_username") || document.getElementById("input_j_username");
    var password = document.getElementById("loginForm:input_j_password") || document.getElementById("input_j_password");

    username.value = '';
    password.value = '';
  }
</script>
<div class="login-page">
    <div class="card login-form rounded">
      <div style="padding-bottom: 36px; padding-top: 60px">
        <img src="images/opennms_horizon_title.svg" class="horizon" width="185px" />
      </div>

      <form class="" id="loginForm" name="loginForm" role="form" method="post" action="<c:url value='j_spring_security_check'/>" autocomplete="off">
        <div class="form-content">
          <div class="form-group">
            <input type="text" id="input_j_username" name="j_username"
            <%-- This is deprecated and requires a custom AuthenticationFailureHandler to function properly --%>
                   <c:if test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:if>
                   placeholder="Username" autofocus="autofocus" autocomplete="username" required/>
          </div>

          <div class="form-group">
            <input type="password" id="input_j_password" name="j_password" placeholder="Password" autocomplete="off" required>
          </div>

          <c:if test="${not empty param.session_expired}">
            <div id="login-expired" class="alert alert-warning">
              <strong>Session expired</strong> <br> Please log back in.
            </div>
          </c:if>

          <c:if test="${not empty param.login_error}">
            <div id="login-attempt-failed" class="alert alert-danger">
              Your login attempt failed, please try again.

                <%-- This is: AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY --%>
              <p id="login-attempt-failed-reason">Reason: ${SPRING_SECURITY_LAST_EXCEPTION.message}</p>
            </div>
          </c:if>

          <div class="form-group">
            <input name="j_usergroups" type="hidden" value=""/>
            <button name="Login" type="submit">LOGIN</button>
          </div>
        </div>
      </form>
  </div>

  <div class="" style="position: absolute; bottom: 0px; right: 10px; font-size: 3em; padding: 20pt 20pt 5pt 20pt">
       <div style="padding-bottom: 20px; padding-top: 20px">
          <img src="images/opennms-logo-light.svg" class="" width="180px" />
       </div>
  </div>

</div>


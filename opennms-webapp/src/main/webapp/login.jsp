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
    /*overflow: auto;*/
    background-image: url('images/wallpapers/IMG_9269-X2.jpg');
    background-size: cover;
  }

  .login-form {
    max-width: 360px;
  }

  .card {
    background-color: #F7F7F7;
    /* just in case there no content*/
    padding: 20px 25px 30px;
    margin: 0 auto 25px;
    margin-top: 50px;
    /* shadows and rounded borders */
    -moz-border-radius: 2px;
    -webkit-border-radius: 2px;
    border-radius: 2px;
    -moz-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
    -webkit-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
    box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
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

<div class="login-page">
  <div class="" style=""> <!-- this way it appears a bit above center which feels more natural -->
    <div class="card login-form rounded">
      <div style="padding-bottom: 50px; padding-top: 20px">
        <img src="images/opennms-logo.png" class="" width="170px" />
        <span style="font-size: 100%" class="badge badge-horizon pull-right">Horizon</span>
      </div>
      <form class="" name="loginForm" role="form" method="post" action="<c:url value='j_spring_security_check'/>">
        <div class="form-content">
          <div class="form-group">
            <label for="input_j_username" class="sr-only">Username</label>
            <input type="text" class="form-control input-underline form-control-lg" id="input_j_username" name="j_username"
            <%-- This is deprecated and requires a custom AuthenticationFailureHandler to function properly --%>
                   <c:if test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:if>
                   placeholder="Username" autofocus="autofocus" autocomplete="username" required />
          </div>

          <div class="form-group">
            <label for="j_password" class="sr-only">Password</label>
            <input type="password" class="form-control input-underline form-control-lg" id="input_j_password" name="j_password" placeholder="Password" autocomplete="current-password" required>
          </div>

          <c:if test="${not empty param.session_expired}">
            <div class="alert alert-warning">
              <strong>Session expired</strong> Please log back in.
            </div>
          </c:if>

          <c:if test="${not empty param.login_error}">
            <div id="login-attempt-failed" class="alert alert-danger">
              Your log-in attempt failed, please try again.

                <%-- This is: AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY --%>
              <p id="login-attempt-failed-reason">Reason: ${SPRING_SECURITY_LAST_EXCEPTION.message}</p>
            </div>
          </c:if>

          <div class="form-group">
            <input name="j_usergroups" type="hidden" value=""/>
            <button name="Login" type="submit" class="btn btn-primary"><i class="fa fa-sign-in"></i> Login</button>
          </div>
        </div>
      </form>
    </div>
  </div>

  <div class="" style="position: absolute; bottom: 0px; right: 0px; font-size: 3em; padding: 20pt 20pt 5pt 20pt">
    <a href="https://docs.opennms.org/opennms" class="text-light" style="padding: 0.5rem" title="Show documentation"><i class="fa fa-book" aria-hidden="true"></i></a>
    <a href="https://github.com/OpenNMS/opennms.git" class="text-light" style="padding: 0.5rem" title="Fork us on Github"><i class="fa fa-github" aria-hidden="true"></i></a>
  </div>
</div>


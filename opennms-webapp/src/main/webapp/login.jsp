<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Login" />
  <jsp:param name="nonavbar" value="true" />
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

<%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
<c:if test="${not empty param.login_error}">
  <blockquote>
    <p id="login-attempt-failed" class="lead text-danger">
      Your log-in attempt failed, please try again.
    </p>

    <%-- This is: AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY --%>
    <p id="login-attempt-failed-reason">Reason: ${SPRING_SECURITY_LAST_EXCEPTION.message}</p>
  </blockquote>
</c:if>

<div class="row row-centered login">
  <div class="col-md-6 col-centered">
    <form class="form-horizontal" role="form" action="<c:url value='j_spring_security_check'/>" method="post">
      <div class="form-group">
        <label for="input_j_username" class="col-sm-4 control-label">Username</label>
        <div class="col-sm-8">
          <input type="text" class="form-control" id="input_j_username" name="j_username"
            <%-- This is deprecated and requires a custom AuthenticationFailureHandler to function properly --%>
            <c:if test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:if>
            placeholder="Username" autofocus="autofocus" autocomplete="username" />
        </div>
      </div>

      <div class="form-group">
        <label for="j_password" class="col-sm-4 control-label">Password</label>
        <div class="col-sm-8">
          <input type="password" class="form-control" id="input_j_password" name="j_password" placeholder="Password" autocomplete="current-password" >
        </div>
      </div>

      <div class="form-group">
        <div class="col-sm-offset-4 col-sm-8">
          <button type="submit" name="Login" class="btn btn-default">Login</button>
        </div>
      </div>

      <input name="j_usergroups" type="hidden" value=""/>

      <script type="text/javascript">
        if (document.getElementById) {
          document.getElementById('input_j_username').focus();
        }
      </script>
    </form>
  </div> <!-- End Column -->
</div> <!-- End Row -->

<hr />

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

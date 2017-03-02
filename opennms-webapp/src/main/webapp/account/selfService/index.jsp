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
	import="
	org.opennms.netmgt.config.UserManager,
	org.opennms.netmgt.config.users.User,
    org.springframework.web.context.WebApplicationContext,
    org.springframework.web.context.support.WebApplicationContextUtils,
    org.opennms.web.api.Authentication"
%>

<%
	boolean canEdit = false;
    String userid = request.getRemoteUser();
    if (request.isUserInRole(Authentication.ROLE_ADMIN)) {
        canEdit = true;
    } else {
	    try {
            final WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            final UserManager userFactory = webAppContext.getBean("userManager", org.opennms.netmgt.config.UserManager.class);
       		User user = userFactory.getUser(userid);
       		if (!user.getRoles().contains(Authentication.ROLE_READONLY)) {
       		    canEdit = true;
       		}
	    } catch (Throwable e) {
	    	throw new ServletException("Couldn't initialize UserFactory", e);
	    }
	}
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="User Account Self-Service" />
  <jsp:param name="headTitle" value="User Account Self-Service" />
  <jsp:param name="breadcrumb" value="User Account Self-Service" />
</jsp:include>

<script type="text/javascript">
  function changePassword() {
	  <% if (canEdit) { %>
    document.selfServiceForm.action = "account/selfService/newPasswordEntry";
    document.selfServiceForm.submit();
<% } else { %>
	alert("The <%= userid %> user is read-only!  Please have an administrator change your password.");
<% } %>
  }
</script>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">User Account Self-Service</h3>
      </div>
      <div class="panel-body">
        <ul class="list-unstyled">
          <li><a href="javascript:changePassword()">Change Password</a></li>
        </ul>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Account Self-Service Options</h3>
      </div>
      <div class="panel-body">
        <p>
         Currently, account self-service is limited to password changes. Note that in environments using a
         reduced sign-on system such as LDAP, changing your password here may have no effect and may not even be
         possible.
         </p>
         <p>
         If you require further changes to your account, please contact the person within your organization responsible for
         maintaining OpenNMS.
         </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<form name="selfServiceForm" method="post"></form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

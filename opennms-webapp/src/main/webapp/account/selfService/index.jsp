<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.UserFactory,
	org.opennms.netmgt.config.UserManager,
	org.opennms.netmgt.config.users.User,
	org.opennms.web.springframework.security.Authentication"
%>

<%
	boolean canEdit = false;
    String userid = request.getRemoteUser();
    if (request.isUserInRole(Authentication.ADMIN_ROLE)) {
        canEdit = true;
    } else {
	    try {
       		UserManager userFactory = UserFactory.getInstance();
       		User user = userFactory.getUser(userid);
       		if (!user.isReadOnly()) {
       		    canEdit = true;
       		}
	    } catch (Exception e) {
	    	throw new ServletException("Couldn't initialize UserFactory", e);
	    }
	}
%>

<jsp:include page="/includes/header.jsp" flush="false" >
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

<div class="TwoColLeft">
    <h3>User Account Self-Service</h3>
        <div class="boxWrapper">
        <ul class="plain">
        <li><a href="javascript:changePassword()">Change Password</a></li>
        </ul>
        </div>
</div>

<div class="TwoColRight">
    <h3>Account Self-Service Options</h3>
    <div class="boxWrapper">
    <p>
    Currently, account self-service is limited to password changes. Note that in environments using a
    reduced sign-on system such as LDAP, changing your password here may have no effect and may not even be
    possible.
    </p>
    <p>
    If you require further changes to your account, please contact the person within your organization responsible for
    maintaining OpenNMS.
    </p>
    </div>
</div>

<form name="selfServiceForm" method="post"></form>

<jsp:include page="/includes/footer.jsp" flush="false" />
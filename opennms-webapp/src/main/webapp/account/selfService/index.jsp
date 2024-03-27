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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("User Account Self-Service")
          .breadcrumb("User Account Self-Service")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
    <div class="card">
      <div class="card-header">
        <span>User Account Self-Service</span>
      </div>
      <div class="card-body">
        <ul class="list-unstyled mb-0">
          <li><a href="javascript:changePassword()">Change Password</a></li>
        </ul>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Account Self-Service Options</span>
      </div>
      <div class="card-body">
        <p>
         Currently, account self-service is limited to password changes. Note that in environments using a
         reduced sign-on system such as LDAP, changing your password here may have no effect and may not even be
         possible.
         </p>
         <p>
         If you require further changes to your account, please contact the person within your organization responsible for
         maintaining OpenNMS.
         </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<form name="selfServiceForm" method="post"></form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

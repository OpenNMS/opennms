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
	import="org.opennms.netmgt.config.users.*,
	        org.opennms.netmgt.config.*,
		java.util.*"
%>

<%
	UserManager userFactory;
  	Map<String,User> users = null;
	HashMap<String, String> usersHash = new HashMap<String, String>();
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Throwable e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}

	for (User curUser : users.values()) {
		usersHash.put(curUser.getUserId(), curUser.getFullName().orElse(null));
	}

%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("List")
          .headTitle("Roles")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users, Groups and On-Call Roles", "admin/userGroupView/index.jsp")
          .breadcrumb("Role List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

	function doOperation(op, role) {
		document.roleForm.operation.value=op;
		document.roleForm.role.value=role;
		document.roleForm.submit();
	}
	
	function doDelete(role) {
		doOperation("delete", role);
	}
	
	function doView(role) {
		doOperation("view", role);
	}

</script>

<form action="<c:url value='${reqUrl}'/>" method="post" name="roleForm">
	<input type="hidden" name="operation" />
	<input type="hidden" name="role" />
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form>

<div class="card">
  <div class="card-header">
    <span>On-Call Role Configuration</span>
  </div>
  <table class="table table-sm">
        <tr>
          <th>Delete</th>
          <th>Name</th>
          <th>Supervisor</th>
          <th>Currently On Call</th>
          <th>Membership Group</th>
          <th>Description</th>
        </tr>
        
        <c:choose>
          <c:when test="${empty roleManager.roles}">
            <tr>
              <td colspan="6">No roles defined.  Use the "Add new role" link
                to add roles.</td>
            </tr>
	 	  </c:when>
	 	  
	 	  <c:otherwise>
			<c:forEach var="role" items="${roleManager.roles}">
				<c:set var="deleteUrl" value="javascript:doDelete('${e:forJavaScript(role.name)}')" />
				<c:set var="viewUrl" value="javascript:doView('${e:forJavaScript(role.name)}')" />
				<c:set var="confirmScript" value="return confirm('Are you sure you want to delete the role ${e:forJavaScript(role.name)}?')"/>
				
				<tr>
				<td><a href="${deleteUrl}" onclick="${confirmScript}"><i class="fa fa-trash-o fa-2x"></i></a></td>
				<td><a href="${viewUrl}">${fn:escapeXml(role.name)}</a></td>
				<td>
				  <c:set var="supervisorUser">${role.defaultUser}</c:set>
				  <c:set var="fullName"><%= usersHash.get(pageContext.getAttribute("supervisorUser").toString()) %></c:set>
				  <span title="${fullName}">${role.defaultUser}</span>
				</td>
				<td>
					<c:forEach var="scheduledUser" items="${role.currentUsers}">
						<c:set var="curUserName">${scheduledUser}</c:set>
						<c:set var="fullName"><%= usersHash.get(pageContext.getAttribute("curUserName").toString()) %></c:set>
						<span title="${fullName}">${scheduledUser}</span>
					</c:forEach>	
				</td>
				<td>${role.membershipGroup}</td>
				<td><c:out value="${role.description}"/></td>
				</tr>
			</c:forEach>
	 	  </c:otherwise>
	 	</c:choose>
  </table>
</div> <!-- panel -->

<form action="<c:url value='${reqUrl}'/>" method="post" name="newForm">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <input name="operation" type="hidden" value="new"/>
  <button type="submit" class="btn btn-secondary">Add New On-Call Role</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

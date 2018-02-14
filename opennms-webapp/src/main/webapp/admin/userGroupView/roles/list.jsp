<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="On-Call Role Configuration" />
	<jsp:param name="headTitle" value="List" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and On-Call Roles</a>" />
	<jsp:param name="breadcrumb" value="Role List" />
</jsp:include>

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
</form>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">On-Call Role Configuration</h3>
  </div>
  <table class="table table-condensed">
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
				<td><a href="${viewUrl}">${role.name}</a></td>
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
  <input name="operation" type="hidden" value="new"/>
  <button type="submit" class="btn btn-default">Add New On-Call Role</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
	import="org.opennms.netmgt.config.users.*,
	        org.opennms.netmgt.config.*,
		java.util.*"
%>

<%
	UserManager userFactory;
  	Map users = null;
	HashMap<String, String> usersHash = new HashMap<String, String>();
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Exception e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}

	Iterator i = users.keySet().iterator();
	while (i.hasNext()) {
		User curUser = (User)users.get(i.next());
		usersHash.put(curUser.getUserId(), curUser.getFullName());
	}

%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="headTitle" value="List" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>" />
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

<h3>Role Configuration</h3>

<table>

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
              <td colspan="6">No roles defined.  Use the "Add New Role" button
                to add roles.</td>
            </tr>
	 	  </c:when>
	 	  
	 	  <c:otherwise>
			<c:forEach var="role" items="${roleManager.roles}">
				<c:set var="deleteUrl" value="javascript:doDelete('${role.name}')" />
				<c:set var="viewUrl" value="javascript:doView('${role.name}')" />
				<c:set var="confirmScript" value="return confirm('Are you sure you want to delete the role ${role.name}?')"/>
				
				<tr>
				<td><a href="${deleteUrl}" onclick="${confirmScript}"><img src="images/trash.gif" alt="Delete ${role.name}"></a></td>
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
				<td>${role.description}</td>
				</tr>
			</c:forEach>
	 	  </c:otherwise>
	 	</c:choose>
	 	
		</table>
		
<br/>

<form action="<c:url value='${reqUrl}'/>" method="post" name="newForm">
  <input name="operation" type="hidden" value="new"/>
  <input type="submit" value="Add New Role"/>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

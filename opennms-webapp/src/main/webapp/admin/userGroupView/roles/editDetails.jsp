<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	session="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="headTitle" value="Edit" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/roles'>Role List</a>" />
	<jsp:param name="breadcrumb" value="Edit Role" />
</jsp:include>

<script type="text/javascript" >

	function changeDisplay() {
		document.displayForm.submit();
	}
	
	function prevMonth() {
		document.prevMonthForm.submit();
	}
	
	function nextMonth() {
		document.nextMonthForm.submit();
	}

</script>

<h3>Edit Role</h3>

<form action="<c:url value='${reqUrl}'/>" method="post" name="editForm">
  <input type="hidden" name="operation" value="saveDetails"/>
  <input type="hidden" name="role" value="${role.name}"/>
  
		 <table>
	         <tr>
    		    		<th>Name</th>
				<td><input name="roleName" type="text" value="${role.name}"/></td>
    		    		<th>Currently On Call</th>
				<td>
					<c:forEach var="scheduledUser" items="${role.currentUsers}">
						${scheduledUser}
					</c:forEach>	
				</td>
          	</tr>
	         <tr>
    		    		<th>Supervisor</th>
				<td>
					<select name="roleUser">
					<c:forEach var="user" items="${userManager.users}">
						<c:choose>
							<c:when test="${user == role.defaultUser}">
								<option selected>${user}</option>
							</c:when>
							<c:otherwise>
								<option>${user}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
    		    		<th>Membership Group</th>
				<td>
					<select name="roleGroup">
					<c:forEach var="group" items="${groupManager.groups}">
						<c:choose>
							<c:when test="${group == role.membershipGroup}">
								<option selected>${group}</option>
							</c:when>
							<c:otherwise>
								<option>${group}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
          	</tr>
          	<tr>
    		    		<th>Description</th>
				<td colspan="3"><input name="roleDescr" size="100" type="text" value="${role.description}"/></td>
          	</tr>
		</table>

  <br/>
  <input type="submit" name="save" value="Save" />
  &nbsp;&nbsp;&nbsp;
  <input type="submit" name="cancel" value="Cancel" />
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

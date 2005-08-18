<!--

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

-->

<%@page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>
<head>
<title>List | Role Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

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


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0"
	TOPMARGIN="0">

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>" />
	<jsp:param name="breadcrumb" value="Role List" />
</jsp:include>

<form action="<c:url value='${reqUrl}'/>" method="POST" name="roleForm">
	<input type="hidden" name="operation" />
	<input type="hidden" name="role" />
</form>


<br />
<table>
	<tr>
		<td>&nbsp;</td>
		<td>
		<h3>Role Configuration</h3>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
		 <form action="<c:url value='${reqUrl}'/>" method="POST" name="newForm">
		 	<input name="operation" type="hidden" value="new"/>
		 	<input type="submit" value="New Role"/>
		 </form>
		 <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">

         <tr bgcolor="#999999">
          <td/>
          <td><b>Name</b></td>
          <td><b>Supervisor</b></td>
          <td><b>Currently On Call</b></td>
          <td><b>Membership Group</b></td>
          <td><b>Description</b></td>
			<c:forEach var="role" items="${roleManager.roles}">
				<c:set var="deleteUrl" value="javascript:doDelete('${role.name}')" />
				<c:set var="viewUrl" value="javascript:doView('${role.name}')" />
				<c:set var="confirmScript" value="return confirm('Are you sure you want to delete the role ${role.name}?')"/>
				
				<tr>
				<td><a href="<c:out value='${deleteUrl}'/>" onclick="<c:out value='${confirmScript}'/>"><img src="images/trash.gif" alt="<c:out value='Delete ${role.name}'/>"></a></td>
				<td><a href="<c:out value='${viewUrl}'/>"><c:out value="${role.name}"/></a></td>
				<td><c:out value="${role.defaultUser}"/></td>
				<td>
					<c:forEach var="scheduledUser" items="${role.currentUsers}">
						<c:out value="${scheduledUser}"/>
					</c:forEach>	
				</td>
				<td><c:out value="${role.membershipGroup}"/></td>
				<td><c:out value="${role.description}"/></td>
				</tr>
			</c:forEach>
		</table>
		</td>
	</tr>
</table>
<br/>
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

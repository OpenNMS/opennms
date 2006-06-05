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
%>

<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>


<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="headTitle" value="Edit Schedule" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/roles'>Role List</a>" />
	<jsp:param name="breadcrumb" value="Edit Entry" />
</jsp:include>

<h3>Edit Schedule Entry</h3>

<div class="error"><c:out value="${error}"/></div>

<form action="<c:url value='${reqUrl}'/>" method="post" name="saveEntryForm">
  <input type="hidden" name="operation" value="saveEntry"/>
  <input type="hidden" name="role" value="<c:out value='${role.name}'/>"/>
  <input type="hidden" name="schedIndex" value="<c:out value='${schedIndex}'/>"/>
  <input type="hidden" name="timeIndex" value="<c:out value='${timeIndex}'/>" /> 
  <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
	         <tr>
    		    		<td bgcolor="#999999"><b>Role</b></td>
				<td><c:out value='${role.name}'/></td>
    		    		<td bgcolor="#999999"><b>User</b></td>
				<td>
					<select name="roleUser">
					<c:forEach var="user" items="${role.membershipGroup.users}">
						<c:choose>
							<c:when test="${user == scheduledUser}"><option selected><c:out value="${user}"/></option></c:when>
							<c:otherwise><option><c:out value="${user}"/></option></c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
          	</tr>
	         <tr>
    		    		<td bgcolor="#999999"><b>Start Date</b></td>
				<td> 
					<c:import url="/includes/dateControl.jsp">
						<c:param name="prefix" value="start"/>
						<c:param name="date"><fmt:formatDate value="${start}" pattern="dd-MM-yyyy"/></c:param>
					</c:import>
				</td>
    		    		<td bgcolor="#999999"><b>Start Time</b></td>
    		    		<td>
					<c:import url="/includes/timeControl.jsp">
						<c:param name="prefix" value="start"/>
						<c:param name="time"><fmt:formatDate value="${start}" pattern="HH:mm:ss"/></c:param>
					</c:import>
				</td>
          	</tr>
	         <tr>
    		    		<td bgcolor="#999999"><b>End Date</b></td>
				<td>
    		    			<c:import url="/includes/dateControl.jsp">
						<c:param name="prefix" value="end"/>
						<c:param name="date"><fmt:formatDate value="${end}" pattern="dd-MM-yyyy"/></c:param>
					</c:import>
				</td>
    		    		<td bgcolor="#999999"><b>End Time</b></td>
				<td>
					<c:import url="/includes/timeControl.jsp">
						<c:param name="prefix" value="end"/>
						<c:param name="time"><fmt:formatDate value="${end}" pattern="HH:mm:ss"/></c:param>
					</c:import>
				</td>
          	</tr>
		</table>
  <br/>
  <input type="submit" name="save" value="Save" />
  &nbsp;&nbsp;&nbsp;
  <input type="submit" name="cancel" value="Cancel" />
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

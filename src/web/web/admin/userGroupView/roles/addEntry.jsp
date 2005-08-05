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
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<html>
<head>
<title>Add Schedule Entry | Role Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

</script>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0"
	TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>"; %>
<% String breadcrumb3 = "<a href='admin/userGroupView/roles'>Role List</a>"; %>
<% String breadcrumb4 = "Add Entry"; %>
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br />
<form action="<c:url value='${reqUrl}'/>" method="POST" name="saveEntryForm">
<input type="hidden" name="operation" value="saveEntry"/>
<input type="hidden" name="role" value="<c:out value='${role.name}'/>"/>
<table>
	<tr>
		<td>&nbsp;</td>
		<td>
		<h3>Add Schedule Entry</h3>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
		 <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
	         <tr>
    		    		<td bgcolor="#999999"><b>Role</b></td>
				<td><c:out value='${role.name}'/></td>
    		    		<td bgcolor="#999999"><b>User</b></td>
				<td>
					<select name="roleUser">
					<c:forEach var="user" items="${role.membershipGroup.users}">
						<c:choose>
							<c:when test="${user == scheduleEntry.user}"><option selected><c:out value="${user}"/></option></c:when>
							<c:otherwise><option><c:out value="${user}"/></option></c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
          	</tr>
	         <tr>
    		    		<td bgcolor="#999999"><b>Start Date</b></td>
				<td>
					<select name="startDate">
					<fmt:formatDate var="startDate" value="${date}" pattern="d"/>
					<c:forEach var="d" begin="1" end="31">
						<c:choose>
							<c:when test="${d == startDate}">
								<option selected value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="startMonth">
					<fmt:formatDate var="startMonth" value="${date}" pattern="M"/>
					<c:forEach var="m" begin="1" end="12">
						<fmt:parseDate var="mo" value="${m}" pattern="M" />
						<fmt:formatDate var="monthName" value="${mo}" pattern="MMMM" />
						<c:choose>
							<c:when test="${m == startMonth}">
								<option selected value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="startYear">
					<fmt:formatDate var="yearStr" value="${date}" pattern="yyyy" />
					<fmt:parseNumber var="startYear" value="${yearStr}"/>
					<c:forEach var="y" begin="0" end="6">
						<c:set var="year" value="${startYear+y-3}"/>
						<c:choose>
							<c:when test="${year == startYear}">
								<option selected value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
    		    		<td bgcolor="#999999"><b>Start Time</b></td>
    		    		<td>
					<select name="startHour">
					<c:forEach var="h" begin="1" end="12">
						<c:choose>
							<c:when test="${h == 12}">
								<option selected value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="startMinute">
					<c:forEach var="half" begin="0" end="1">
						<c:choose>
							<c:when test="${half == 0}">
								<option selected value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:when>
							<c:otherwise>
								<option value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="startAmOrPm">
						<option selected value="AM">AM</option>
						<option value="PM">PM</option>
					</select>
				</td>
          	</tr>
	         <tr>
    		    		<td bgcolor="#999999"><b>End Date</b></td>
				<td>
					<select name="endDate">
					<fmt:formatDate var="endDate" value="${date}" pattern="d"/>
					<c:forEach var="d" begin="1" end="31">
						<c:choose>
							<c:when test="${d == endDate}">
								<option selected value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${d}'/>"><c:out value="${d}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="endMonth">
					<fmt:formatDate var="endMonth" value="${date}" pattern="M"/>
					<c:forEach var="m" begin="1" end="12">
						<fmt:parseDate var="mo" value="${m}" pattern="M" />
						<fmt:formatDate var="monthName" value="${mo}" pattern="MMMM" />
						<c:choose>
							<c:when test="${m == endMonth}">
								<option selected value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${m}'/>"><c:out value="${monthName}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="endYear">
					<fmt:formatDate var="yearStr" value="${date}" pattern="yyyy" />
					<fmt:parseNumber var="endYear" value="${yearStr}"/>
					<c:forEach var="y" begin="0" end="6">
						<c:set var="year" value="${endYear+y-3}"/>
						<c:choose>
							<c:when test="${year == endYear}">
								<option selected value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${year}'/>"><c:out value="${year}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
				</td>
    		    		<td bgcolor="#999999"><b>End Time</b></td>
				<td>
					<select name="endHour">
					<c:forEach var="h" begin="1" end="12">
						<c:choose>
							<c:when test="${h == 12}">
								<option selected value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:when>
							<c:otherwise>
								<option value="<c:out value='${h}'/>"><c:out value="${h}"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="endMinute">
					<c:forEach var="half" begin="0" end="1">
						<c:choose>
							<c:when test="${half == 0}">
								<option selected value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:when>
							<c:otherwise>
								<option value="<fmt:formatNumber value='${half*30}' pattern="00"/>"><fmt:formatNumber value="${half*30}" pattern="00"/></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
					</select>
					<select name="endAmOrPm">
						<option selected value="AM">AM</option>
						<option value="PM">PM</option>
					</select>
				</td>
          	</tr>
		</table>
		</td>
	</tr>
	<tr align="right">
		<td>&nbsp;</td>
		<td>
		<table border="0">
		<tr>
		<td>
			<input type="submit" name="save" value="Save" />
		</td>
		<td>
			<input type="submit" name="cancel" value="Cancel" />
		</td>
		</tr>
		</table>
		</td>
	</tr>
</table>
</form>
<br/>
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

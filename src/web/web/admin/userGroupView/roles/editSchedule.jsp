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
<%@page import="java.util.*"%>
<%@page import="org.opennms.netmgt.config.*"%>
<%@page import="org.opennms.netmgt.config.users.*"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<html>
<head>
<title>Edit Role Schedule | Role Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0"
	TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>"; %>
<% String breadcrumb3 = "Edit Role Schedule"; %>
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
	<jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br />
<table>
	<tr>
		<td>&nbsp;</td>
		<td>
		<h3>Edit Role Schedule</h3>
		</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
		 <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">

         <tr bgcolor="#999999">
          <td width="50%"><b>Name</b></td>
          <td width="50%"><b>Supervisor</b></td>
			<c:forEach items="${roleList}" var="role" varStatus="roleStatus">
				<tr>
				<td><c:out value="${roleStatus.count}"/></td>
				<td><c:out value="${role}"/></td>
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

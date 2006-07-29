<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>

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



<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Current Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb" value="Current Outages By Node" />
</jsp:include>


<html>
<head>
<title>Current Outages</title>
</head>
<body>

<c:forEach items="${outages}" var="outageId">
	<c:out value="${outageId}" />
	<br>
	<br>
</c:forEach>



<a href="outage/list" title="See all outages in the outage browser">View
All Outages</a>
<%--&nbsp;&nbsp;&nbsp; <a href="outage/advsearch.jsp" 
title="More advanced searching and sorting options">Advanced Search</a>--%>
&nbsp;&nbsp;&nbsp;
<a href="outage/list?outtype=1"
	title="A more powerful way of looking at outages">Query Current
Outages</a>

<h3>Current Outages</h3>

<table class="standardfirst">

	<tr>
		<td class="standardheader" WIDTH="5%">ID</td>
		<td class="standardheader">Node</td>
		<td class="standardheader" WIDTH="15%">Interface</td>
		<td class="standardheader" width="10%">Service&nbsp;Down</td>
		<td class="standardheader" WIDTH="30%">Time&nbsp;Down</td>
		<td class="standardheader" WIDTH="10%">Suppress&nbsp;</td>
		<td class="standardheader" WIDTH="10%">&nbsp;</td>
	</tr>
	
	<tr valign="top"
		 "BGCOLOR="#cccccc" >
		
<c:forEach items="${outages}" var="outageId">
		
		<td class="standard"><a href="outage/detail.jsp?id="><c:out value="outageId"/></a></td>
		<%
		if (intfIndex == 0 && svcIndex == 0) {
		%>
		<td class="standard" rowspan="<%=serviceCnt%>"><a
			name="node<%=nodeId%>" /><a HREF="element/node.jsp?node=<%=nodeId%>"
			title="General information about this node"><%=outage.getNodeLabel()%></a></td>
		<%
		}
		%>

		<%
		if (svcIndex == 0) {
		%>
		<td class="standard" rowspan="<%=svcList.size()%>"><a
			HREF="element/interface.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>"
			title="General information about this interface"><%=ipAddr%></a> <%=!ipAddr.equals(outage
													.getHostname()) ? "("
											+ outage.getHostname() + ")" : ""%></td>
		<%
		}
		%>

		<td class="standard"><a
			HREF="element/service.jsp?node=<%=nodeId%>&intf=<%=ipAddr%>&service=<%=outage.getServiceId()%>"><%=outage.getServiceName()%></a></td>
		<td class="standard"><%=org.opennms.netmgt.EventConstants
												.formatToUIString(outage
														.getTimeDown())%></td>

		<td class="standard">

		<FORM action="outage/current" method="GET" NAME="suppressed">

		// Hidden tag contains the OutageID <input type="hidden"
			name="outageId" value="<%=outageId%>" /> <SELECT NAME="suppressTime">
			<OPTION VALUE="15">15 Minutes
			<OPTION VALUE="30">30 Minutes
			<OPTION VALUE="60">1 Hour
			<OPTION VALUE="120">2 Hours
			<OPTION VALUE="480">8 Hours
			<OPTION VALUE="1440">24 Hours
			<OPTION VALUE="4320">3 days
			<OPTION VALUE="0">Until Re-Enabled
		</SELECT>
		</td>
		<td><input type="submit" value="Submit" />
		</FORM>
		</td>


	</tr>
	

	<tr>
		<td class="standardheader" colspan="7"><%=outages.length%> total
		services down on <%=interfaceCount%> interfaces of <%=nodeCount%>
		nodes</td>
	</tr>
</table>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>


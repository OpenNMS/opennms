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

<%@page language="java" contentType = "text/html" session = "true"  %>
<%
	String error = null;
	String name = null;
	try
    	{
		error = request.getParameter( "error" );
		name = request.getParameter( "name" );
	}
	catch(Exception e)
	{
		throw new ServletException("Admin:pollerConfig " + e.getMessage());
	}
%>
<html>
<head>
  <title>Error Page | Configure Poller | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/pollerConfig/index.jsp'>Configure Pollers</a>"; %>
<% String breadcrumb3 = "Error Page"; %>
<jsp:include page="/WEB-INF/jspf/header.jspf" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br />

<table border="0" width="100%">
<tr>
<td>&nbsp;</td>
<td>
<% 
	int errorcode = (new Integer(error)).intValue();
	switch(errorcode)
	{
		case 0:	%>
				An error has occured due to a missing parameter <%= name %> in the poller configuration file
			
	<%		break;
		case 1: %>
				An error has occured since the <%= name %> poller already exists
	<%		break;
		case 2: %>
				An error has occured due to the poller-configuration.xml file being empty
	<%		break;
		case 3:%>
				An error has occured due to the capsd-configuration.xml file being empty
	<%
			break;
	}
%>

</td>
</tr>
<tr><td>&nbsp;</td>
<p><a href="admin/pollerConfig/index.jsp">Go back to the Poller Configuration page</a></p>
</td>

</tr></table>

<br />

<jsp:include page="/WEB-INF/jspf/footer.jspf" flush="true"/>
</body>
</html>

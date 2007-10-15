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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2002 Nov 12: Add response time graphs to webUI.
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

<%@ page language="java" contentType="text/html" session="true"  %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles" %>

<html>
	<head>
		<title><tiles:getAsString name="title"/></title>
		<base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
		<link rel="stylesheet" type="text/css" href="css/styles.css" />
  		<script src="javascripts/prototype.js" type="text/javascript"></script>
		<script src="javascripts/scriptaculous.js" type="text/javascript"></script>
	</head>

	<body>

<tiles:insert name="header">
<!-- <tiles:put name="title"><tiles:getAsString name="title"/></tiles:put> -->
</tiles:insert>


<tiles:insert name="content" />

<tiles:insert name="footer"/>

</body>
</html>

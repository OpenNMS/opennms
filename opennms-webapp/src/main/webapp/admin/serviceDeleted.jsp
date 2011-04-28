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
// 2004 Oct 5: Created File
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.MissingParameterException,
		org.opennms.web.category.*,
		java.util.*,
		org.opennms.web.event.*
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String serviceIdString = request.getParameter( "service" );

    if( nodeIdString == null ) {
        throw new MissingParameterException( "node", new String[] { "node", "intf", "service" } );
    }

    if( ipAddr == null ) {
        throw new MissingParameterException( "intf", new String[] { "node", "intf", "service" } );
    }

    if( serviceIdString == null ) {
        throw new MissingParameterException( "service", new String[] { "node", "intf", "service" } );
    }

    int nodeId = -1;
    int serviceId = -1;

    try {
        nodeId = WebSecurityUtils.safeParseInt( nodeIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type for node ID, should be integer", e );
    }

    try {
        serviceId = WebSecurityUtils.safeParseInt( serviceIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type for service ID, should be integer", e );
    }

    String serviceName = NetworkElementFactory.getInstance(getServletContext()).getServiceNameFromId( serviceId );

    String headTitle = serviceName + " Service on " + ipAddr;
%>

<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
  <c:param name="intf" value="<%=ipAddr%>"/>
</c:url>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="<%= headTitle %>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='${nodeLink}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='${interfaceLink}'>Interface</a>" />
  <jsp:param name="breadcrumb" value="Service Deleted" />
</jsp:include>

<h3>Finished Deleting <c:out value="<%=serviceName%>"/> Service on <c:out value="<%=ipAddr%>"/></h3>
<p>
  OpenNMS should not need to be restarted, but it may take a moment for
  the Categories to be updated.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />

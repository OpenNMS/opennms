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
        serviceId = WebSecurityUtils.safeParseInt( serviceIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer", e );
    }

    Service service_db = NetworkElementFactory.getInstance(getServletContext()).getService( nodeId, ipAddr, serviceId );

    if( service_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such service in database" );
    }

    String eventUrl = "event/list.htm?filter=node%3D" + nodeId + "&filter=interface%3D" + ipAddr + "&filter=service%3D" + serviceId;

    String headTitle = service_db.getServiceName() + " Service on " +ipAddr;
%>

<% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
<% String breadcrumb3 = "<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr  + "'>Interface</a>"; %>
<% String breadcrumb4 = "<a href='element/service.jsp?node=" + nodeId + "&intf=" + ipAddr  + "&service=" + serviceId + "'>Service</a>"; %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="<%= headTitle %>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
  <jsp:param name="breadcrumb" value="Service Deleted" />
</jsp:include>

<h3>Finished Deleting <%=service_db.getServiceName()%> Service
    on <%=ipAddr%></h3>

<p>
  OpenNMS should not need to be restarted, but it may take a moment for
  the Categories to be updated.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />

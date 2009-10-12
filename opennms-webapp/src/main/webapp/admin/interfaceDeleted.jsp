<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Oct 10: Add parameter ifIndex - ayres@opennms.org
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.element.*,
		org.opennms.web.category.*,
		org.opennms.web.WebSecurityUtils,
		java.util.*,
		org.opennms.web.event.*,
		org.opennms.web.MissingParameterException
	"
%>

<%
	
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String ifIndexString = request.getParameter("ifindex");

    if( nodeIdString == null ) {
        throw new MissingParameterException( "node", new String[] { "node", "intf or ifindex" } );
    }

    if( ipAddr == null && ifIndexString == null ) {
        throw new MissingParameterException( "intf or ifindex", new String[] { "node", "intf or ifindex" } );
    }

    int nodeId = -1;

    try {
        nodeId = WebSecurityUtils.safeParseInt( nodeIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer but got '"+nodeIdString+"'", e );
    }
    
    int ifIndex = -1;
    if (ifIndexString != null && ifIndexString.length() != 0) {
        try {
            ifIndex = WebSecurityUtils.safeParseInt( ifIndexString );
        }
        catch( NumberFormatException e ) {
            //throw new WrongParameterDataTypeException
            throw new ServletException( "Wrong data type, should be integer but got '"+ifIndexString+"'", e );
        }
    }
	
%>

<% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
<% String breadcrumb3 = "<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr  + "'>Interface</a>"; %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Interface Deleted" />
  <jsp:param name="headTitle" value="<%= ipAddr %>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="Interface Deleted" />
</jsp:include>

<% if (ifIndex == -1) { %>
<h3>Finished Deleting Interface <%= ipAddr %></h3>
<% } else if (!"0.0.0.0".equals(ipAddr) && ipAddr != null && ipAddr.length() !=0){ %>
<h3>Finished Deleting Interface <%= ipAddr %> with ifIndex <%= ifIndex %></h3>
<% } else { %>
<h3>Finished Deleting Interface with ifIndex <%= ifIndex %></h3>
<% } %>
<p>
  OpenNMS should not need to be restarted, but it may take a moment for
  the Categories to be updated.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />

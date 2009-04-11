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
// 2003 Apr 16: Changed the notification box to show outstanding notifications
//              for logged in user.
// 2003 Feb 07: Fixed URLEncoder issues.
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

<%--
  This page is included by other JSPs to create a box containing a
  table that provides links for notification queries.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html; charset=UTF-8" session="true" import="org.opennms.web.notification.*" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%
    //optional parameter: node
    String nodeIdString = request.getParameter("node");

    String nodeFilter = "";

    if( nodeIdString != null ) {
        nodeFilter = "&filter=node%3D" + nodeIdString;
    }
%>

<%!
    protected NotificationModel model = new NotificationModel();
    protected java.text.ChoiceFormat formatter = new java.text.ChoiceFormat( "0#<spring:message code='notification.format.0notices'/>|1#<spring:message code='notification.format.1notice'/>|2#<spring:message code='notification.format.2notices'/>" );
%>
<h3 class="o-box"><a href="notification/index.jsp"><spring:message code="node.notification"/></a></h3>
<div class="boxWrapper">
	<ul class="plain o-box">
		<% if( nodeIdString == null ) { %>
			<li><strong><spring:message code="notification.you"/></strong>: <%
				int count = this.model.getOutstandingNoticeCount(request.getRemoteUser());
				String format = this.formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
				%>
				(<a href="notification/browse?acktype=unack&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>"><spring:message code="notification.check"/></a>)</li>
			<li><strong><spring:message code="notification.all"/></strong>: <%
				count = this.model.getOutstandingNoticeCount();
				format = this.formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
				%>
				(<a href="notification/browse?acktype=unack"><spring:message code="notification.check"/></a>)</li>
			<li><a href="roles"><spring:message code="notification.on_call_schedule"/></a></li>
		<% } else { %>
			<li><strong><spring:message code="notification.you_outstanding"/></strong>: 
				(<a href="notification/browse?acktype=unack<%=nodeFilter%>&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>"><spring:message code="notification.check"/></a>)</li>
			<li><strong><spring:message code="notification.you_acknowledged"/></strong>: 
				(<a href="notification/browse?acktype=ack<%=nodeFilter%>&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>"><spring:message code="notification.check"/></a>)</li>
		<% } %>
	</ul>
</div>

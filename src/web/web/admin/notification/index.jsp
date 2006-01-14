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
	import="java.lang.*"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Notifications" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure Notifications" />
</jsp:include>

<h3>Configure Notifications</h3>

<div style="width: 40%; float: left;">
  <p>
    <a href="admin/notification/noticeWizard/eventNotices.jsp">Configure Event Notifications</a>
  </p>

  <p>
    <a href="admin/notification/destinationPaths.jsp">Configure Destination Paths</a>
  </p>
</div>

<div style="width: 60%; float: left;">
  <h3>Event Notifications</h3>

  <p>
    Each event can be configured to send a notification whenever that event is
    triggered. This wizard will walk you through the steps needed for
    configuring an event to send a notification.
  </p>

  <h3>Destination Paths</h3>

  <p>
    A destination path describes what users or groups will receive
    notifications, how the notifications will be sent, and who to notify
    if escalation is needed. This wizard will walk you through setting up
    a resuable list of who to contact and how to contact them, 
    which are used in the event configuration.
  </p>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

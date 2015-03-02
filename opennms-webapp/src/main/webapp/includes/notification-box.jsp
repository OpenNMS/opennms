<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%--
  This page is included by other JSPs to create a box containing a
  table that provides links for notification queries.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="
	org.opennms.web.filter.Filter,
	org.opennms.web.notification.*,
	org.opennms.web.notification.filter.*,
	org.opennms.netmgt.config.NotifdConfigFactory,
	org.springframework.web.context.WebApplicationContext,
	org.springframework.web.context.support.WebApplicationContextUtils
"
%>

<%!
	protected java.text.ChoiceFormat formatter = new java.text.ChoiceFormat( "0#no outstanding notices|1#1 outstanding notice|2#{0} outstanding notices" );
%>
<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
    WebNotificationRepository repository = context.getBean(WebNotificationRepository.class);

    //optional parameter: node
    String nodeIdString = request.getParameter("node");

    String nodeFilter = "";

    if( nodeIdString != null ) {
        nodeFilter = "&amp;filter=node%3D" + nodeIdString;
    }
%>

<div class="panel panel-default">
	<div class="panel-heading">
		<h3 class="panel-title"><a href="notification/index.jsp">Notifications</a></h3>
	</div>
	<div class="panel-body">
	<ul class="list-unstyled">
		<% if( nodeIdString == null ) { %>
			<li>
			<i class="fa fa-fw fa-user"></i>
			You have 
			<a href="notification/browse?acktype=unack&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">
			<%
				long count = repository.countMatchingNotifications(
					new NotificationCriteria(
						AcknowledgeType.UNACKNOWLEDGED, 
						new Filter[] { 
							new UserFilter(request.getRemoteUser())
						}
					)
				);
				String format = formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Long(count) } ));
			%>
			</a>
			</li>
			<li>
			<i class="fa fa-fw fa-users"></i>
			There are  
			<a href="notification/browse?acktype=unack">
			<%
				count = repository.countMatchingNotifications(
					new NotificationCriteria(
						AcknowledgeType.UNACKNOWLEDGED,
						new Filter[0]
					)
				);
				format = formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Long(count) } ));
			%>
			</a>
			</li>
			<li><i class="fa fa-fw fa-calendar"></i> <a href="roles">On-Call Schedule</a></li>
		<% } else { %>
			<li><a href="notification/browse?acktype=unack<%=nodeFilter%>&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">
				Your outstanding notifications for this node 
			</a></li>
			<li><a href="notification/browse?acktype=ack<%=nodeFilter%>&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">
				Your acknowledged notifications for this node 
			</a></li>
		<% } %>
	</ul>
	</div>
</div>

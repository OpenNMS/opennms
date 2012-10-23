<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	org.opennms.web.notification.*,
	org.opennms.netmgt.config.NotifdConfigFactory"
%>

<%!
	protected NotificationModel model = new NotificationModel();
	protected java.text.ChoiceFormat formatter = new java.text.ChoiceFormat( "0#No outstanding notices|1#1 outstanding notice|2#{0} outstanding notices" );
%>

<%
    //optional parameter: node
    String nodeIdString = request.getParameter("node");

    String nodeFilter = "";

    if( nodeIdString != null ) {
        nodeFilter = "&amp;filter=node%3D" + nodeIdString;
    }

		// @i18n
		String status = "Unknown";
		try {
				NotifdConfigFactory.init();
				status = NotifdConfigFactory.getInstance().getPrettyStatus();
		} catch (Throwable e) { 
			// If factory can't be initialized, status is already 'Unknown'
		}
%>

<style type="text/css">
	#notificationDisabled{
		border: 1px solid red;
		background-color: lightyellow;
		color: red;
		padding: 5px;
	}
</style>

<h3 class="o-box"><a href="notification/index.jsp">Notification</a></h3>
<div class="boxWrapper">
	<ul class="plain o-box">
		<li <%=("Off".equals(status) ? "id=\"notificationDisabled\"" : "")%>>
			<strong>Notification Status</strong>:
			<%=status%>
		</li>

		<% if( nodeIdString == null ) { %>
			<li><strong>You</strong>: <%
				int count = this.model.getOutstandingNoticeCount(request.getRemoteUser());
				String format = this.formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
				%>
				(<a href="notification/browse?acktype=unack&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Check</a>)</li>
			<li><strong>All</strong>: <%
				count = this.model.getOutstandingNoticeCount();
				format = this.formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
				%>
				(<a href="notification/browse?acktype=unack">Check</a>)</li>
			<li><a href="roles">On-Call Schedule</a></li>
		<% } else { %>
			<li><strong>You: Outstanding</strong>: 
				(<a href="notification/browse?acktype=unack<%=nodeFilter%>&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Check</a>)</li>
			<li><strong>You: Acknowledged</strong>: 
				(<a href="notification/browse?acktype=ack<%=nodeFilter%>&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Check</a>)</li>
		<% } %>
	</ul>
</div>

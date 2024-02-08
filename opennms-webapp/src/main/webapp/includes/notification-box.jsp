<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%--
  This page is included by other JSPs to create a box containing a
  table that provides links for notification queries.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="
	org.opennms.core.utils.WebSecurityUtils,
	org.opennms.web.filter.Filter,
	org.opennms.web.notification.AcknowledgeType,
	org.opennms.web.notification.WebNotificationRepository,
	org.opennms.web.notification.filter.NotificationCriteria,
	org.opennms.web.notification.filter.UserFilter
"
%>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>

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
        nodeFilter = "&amp;filter=node%3D" + WebSecurityUtils.sanitizeString(nodeIdString);
    }
%>

<div class="card">
	<div class="card-header">
		<span><a href="notification/index.jsp">Notifications</a></span>
	</div>
	<div class="card-body">
	<ul class="list-unstyled mb-0">
		<% if( nodeIdString == null ) { %>
			<li>
			<i class="fa fa-fw fa-user"></i>
			You have 
			<a href="notification/browse?acktype=unack&amp;filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">
			<%
				int count = repository.countMatchingNotifications(
					new NotificationCriteria(
						AcknowledgeType.UNACKNOWLEDGED, 
						new Filter[] { 
							new UserFilter(request.getRemoteUser())
						}
					)
				);
				String format = formatter.format( count );
				out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
			%>
			</a>
			</li>
			<li>
			<i class="fa fa-fw fa-users"></i>
				<%
					count = repository.countMatchingNotifications(
							new NotificationCriteria(
									AcknowledgeType.UNACKNOWLEDGED,
									new Filter[0]
							)
					);
				 	if( count == 1) { %>
						There is
					<% } else { %>
						There are
					<% } %>
				<a href="notification/browse?acktype=unack">
				<%
					format = formatter.format( count );
					out.println( java.text.MessageFormat.format( format, new Object[] { new Integer(count) } ));
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

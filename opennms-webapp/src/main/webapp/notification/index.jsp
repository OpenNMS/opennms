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

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notifications" />
  <jsp:param name="headTitle" value="Notifications" />
  <jsp:param name="location" value="notifications" />
  <jsp:param name="breadcrumb" value="Notifications" />
</jsp:include>

  <div class="TwoColLeft">
      <h3>Notification queries</h3>
      <div class="boxWrapper">
        <form method="get" action="notification/browse">
          <p align="right">User:
          <input type="text" name="user"/>
          <input type="submit" value="Check notices" /></p>
        </form>
        <form method="get" action="notification/detail.jsp" >
          <p align="right">Notice:
          <input type="text" name="notice" />
          <input type="submit" value="Get details" /></p>
        </form>
        <ul class="plain">
          <li><a href="notification/browse?acktype=unack&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Your outstanding notices</a></li>
          <li><a href="notification/browse?acktype=unack">All outstanding notices</a></li>
          <li><a href="notification/browse?acktype=ack">All acknowledged notices</a></li>
        </ul>
      </div>
  </div>

  <div class="TwoColRight">
    <h3>Outstanding and Acknowledged Notices</h3>
    <div class="boxWrapper">
      <p>When important events are detected by OpenNMS, users may 
        receive a <em>notice</em>, a descriptive message sent automatically
        to a pager, an email address, or both. In order to
        receive notices, the user must have their notification information 
        configured in their user profile (see your Administrator for assistance), 
        notices must be <em>on</em> (see the upper right corner of this window), 
        and an important event must be received.
      </p>

      <p>From this panel, you may: <strong>Check your outstanding notices</strong>, 
        which displays all unacknowledged notices sent to your user ID;
        <strong>View all outstanding notices</strong>, which displays all unacknowledged 
        notices for all users; or <strong>View all acknowledged notices</strong>, 
        which provides a summary of all notices sent and acknowledged for all users.
      </p>

      <p>You may also search for notices associated with a specific user ID 
        by entering that user ID in the <strong>Check notices for user</strong>
        text box. And finally, you can jump immediately to a page with details
        specific to a given notice identifier by entering that numeric 
        identifier in the <strong>Get notice detail</strong> text box. 
        Note that this is particularly useful if you are using a numeric 
        paging service and receive the numeric notice identifier as part of the page.
      </p>
    </div>
    <h3>Notification Escalation</h3>
    <div class="boxWrapper">
        <p>Once a notice is sent, it is considered <em>outstanding</em> until 
            someone <em>acknowledge</em>s receipt of the notice via the OpenNMS
            Notification interface.&nbsp; If the event that 
            triggered the notice was related to managed network devices or systems, 
            the <strong>Network/Systems</strong> group will be notified, one by one, with a
            notice sent to the next member on the list only after 15 minutes has 
            elapsed since the last message was sent. This progression through the
            list, or <em>escalation</em>, can be stopped at any time by acknowledging the
            notice.  Note that this is <strong>not</strong> the same as acknowledging 
            the event which triggered the notice. If all members of the group 
            have been notified and the notice has not been acknowledged, the 
            notice will be escalated to the <strong>Management</strong> group, 
            where all members of that group will be notified at once with no 
            15 minute escalation interval.
        </p>
      </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.Date" %>
<html>
<head>
  <title>Notification | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "Notification"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notification" />
  <jsp:param name="location" value="notification" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Notification queries</h3>

      <p>
        <a HREF="notification/browse?akctype=unack&filter=<%= java.net.URLEncoder.encode("user="+request.getRemoteUser()) %>">Check your outstanding notices</a>
      </p>
      <p>
        <a HREF="notification/browse?acktype=unack">View all outstanding notices</a>
      </p>
      <p>
        <a HREF="notification/browse?acktype=ack">View all acknowledged notices</a>
      </p>
      
      <form METHOD="GET" ACTION="notification/list.jsp" >
        Check&nbsp;notices&nbsp;for&nbsp;user<br>
        <input type="TEXT" NAME="username" />
        <input type="submit" value="Search" />
      </form>
      <form METHOD="GET" ACTION="notification/detail.jsp" >
        Get&nbsp;notice&nbsp;detail<br>
        <input type="TEXT" NAME="notice" />
        <input type="submit" value="Search" />        
      </form>

    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
        <h3>Outstanding and Acknowledged Notices</h3>

        <p>When important events are detected by OpenNMS, users may 
            receive a <em>notice</em>, a descriptive message sent automatically
            to a pager, an email address, or both. In order to
            receive notices, the user must have their notification information 
            configured in their user profile (see your Administrator for assistance), 
            notices must be <em>on</em> (see the upper right corner of this window), 
            and an important event must be received.
      </p>

        <p>From this panel, you may <strong>Check your outstanding notices</strong>, 
            which displays all unacknowledged notices sent to your user ID,
            <strong>View all outstanding notices</strong>, which displays all unacknowledged 
            notices for all users, or <strong>View all acknowledged notices</strong>, 
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
            
        <h3>Notification Escalation</h3>                
            
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
    </td>

     <td> &nbsp; </td>
  </tr>
</table>                                    
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="notification" />
</jsp:include>

</body>
</html>

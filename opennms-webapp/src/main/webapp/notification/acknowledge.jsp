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
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.notification.*,
		org.opennms.web.event.*,
		org.opennms.web.element.*,
		org.opennms.web.MissingParameterException
	"
%>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String[] noticeIds = request.getParameterValues("notices");

    if (noticeIds == null || noticeIds.length == 0) {
        //ok, this is an extremely ugly way of doing this...
        //all the error checking will be cleaned up in the second
        //iteration where all the JSPs are split into JSPs and servlets
        throw new MissingParameterException("notices");
    }
    
    Notification[] notices = null;   

    //actually acknowledge the notices
    //need to handle the interim states of each of these updates;
    //for example, say we are going to acknowledge two notices,
    //the first one succeeds and the second one fails, I need to 
    //give that information to the user, right now if any fail,
    //all you get is a servlet exception (very, very ugly)

    String username = request.getRemoteUser();
    for (int i = 0; i < noticeIds.length; i++ ) {
        this.model.acknowledged(username, WebSecurityUtils.safeParseInt(noticeIds[i]));
    }

    //here again, I'm assuming that all succeeded, really need
    //to address this in the second iteration
    notices = new Notification[noticeIds.length];

    for (int i = 0; i < noticeIds.length; i++) {
        notices[i] = this.model.getNoticeInfo(WebSecurityUtils.safeParseInt(noticeIds[i]));
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notifications Acknowledgment" />
  <jsp:param name="headTitle" value="Notification Acknowledgment" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp'>Notification</a>" />
  <jsp:param name="breadcrumb" value="Acknowledge" />
</jsp:include>

<h3>Acknowledgment Results</h3>
      
<ul>
  <% for( int i = 0; i < notices.length; i++ ) { %>
    <li>Notice #<%=notices[i].getId()%> was successfully
      acknowledged.</li>
  <% } %>
</ul>

<% for( int i = 0; i < notices.length; i++ ) {
  Event event = EventFactory.getEvent( notices[i].getEventId() );
  String eventSeverity = EventUtil.getSeverityLabel(event.getSeverity());%>

  <h4>Notice <%=notices[i].getId()%> Summary</h4>
  <table>
    <tr class="<%=eventSeverity%>">
      <td width="15%">Notice ID</td>
      <td width="15%"> <a href="notification/detail.jsp?notice=<%=notices[i].getId()%>"><%=notices[i].getId()%></a></td>
      <td width="15%">Event ID</td>
      <td width="15%"> <a href="event/detail.jsp?id=<%=notices[i].getEventId()%>"><%=notices[i].getEventId()%></a></td>
      <td width="15%">Sent</td>
      <td width="15%"> <%=notices[i].getTimeSent()%> </td>
    </tr>

    <tr class="<%=eventSeverity%>">
      <td width="15%">Interface</td>

      <td width="15%"> 
        <%if (NetworkElementFactory.getNodeLabel(notices[i].getNodeId())!=null && notices[i].getIpAddress()!=null) { %>
          <a href="element/interface.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>"><%=notices[i].getIpAddress()%></a>
        <% } else if (notices[i].getIpAddress()!=null) { %>
          <%=notices[i].getIpAddress()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>

      <td width="15%">Service</td>

      <td width="15%"> 
        <%if (NetworkElementFactory.getNodeLabel(notices[i].getNodeId())!=null && notices[i].getIpAddress()!=null && notices[i].getServiceName()!=null) { %>
          <a href="element/service.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>&service=<%=notices[i].getServiceId()%>"><%=notices[i].getServiceName()%></a>
        <% } else if (notices[i].getServiceName()!=null) { %>
          <%=notices[i].getServiceName()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>
          
      <td width="15%">Acknowledged</td>

      <td width="15%"> <%=notices[i].getTimeReplied()%> </td>
    </tr>

    <%if (notices[i].getNumericMessage() != null) { %>
      <tr class="<%=eventSeverity%>">
        <td colspan="6">
          <%=notices[i].getNumericMessage()%>
        </td>
      <tr>
    <% } %>

    <%if (notices[i].getTextMessage() != null) { %>
      <tr class="<%=eventSeverity%>">
        <td colspan="6">
          <%=notices[i].getTextMessage()%>
        </td>
      </tr>
    <% } %>
  </table>
<% } %>       
      

<jsp:include page="/includes/footer.jsp" flush="false" />

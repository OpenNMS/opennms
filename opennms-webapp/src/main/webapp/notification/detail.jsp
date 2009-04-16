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
	import="java.util.*,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.notification.*,
		org.opennms.web.element.*,
                org.opennms.web.event.*
	"
%>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String noticeIdString = request.getParameter("notice");

    String eventSeverity;
    
    int noticeID = -1;
    
    try {
        noticeID = WebSecurityUtils.safeParseInt( noticeIdString );
    }
    catch( NumberFormatException e ) {
        throw new NoticeIdNotFoundException("The notice id must be an integer.",
					     noticeIdString );
    }
    
    Notification notice = this.model.getNoticeInfo(noticeID);
    
    if( notice == null ) {
        throw new NoticeIdNotFoundException("An notice with this id was not found.", String.valueOf(noticeID));
    }

    if (NoticeFactory.canDisplayEvent(notice.getEventId())) {
		Event event = EventFactory.getEvent(notice.getEventId());
		eventSeverity = event.getSeverity().getLabel();
    } else {
		eventSeverity = new String("Cleared");
    }

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notification Detail" />
  <jsp:param name="headTitle" value="Notification Detail" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp'>Notification</a>" />
  <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

<script language="Javascript" type="text/javascript" >
    
    function acknowledgeNotice()
    {
        document.acknowledge.submit();
    }
</script>

<h3>Notice #<%=notice.getId()%> 
  <% if ( NoticeFactory.canDisplayEvent(notice.getEventId()) ) { %>
    from event #<a href="event/detail.jsp?id=<%=notice.getEventId()%>"><%=notice.getEventId()%></a>
  <% } %>
</h3>
      
<table>
  <tr class="<%=eventSeverity%>">
    <td width="15%">Notification Time</td>
    <td width="17%"><%=org.opennms.web.Util.formatDateToUIString(notice.getTimeSent())%></td>
    <td width="15%">Time&nbsp;Replied</td>
    <td width="17%"><%=notice.getTimeReplied()!=null ? org.opennms.web.Util.formatDateToUIString(notice.getTimeReplied()) : "&nbsp"%></td>
    <td width="15%">Responder</td>
    <td width="17%"><%=notice.getResponder()!=null ? notice.getResponder() : "&nbsp"%></td>
  </tr>
  <tr class="<%=eventSeverity%>">
    <td width="15%">Node</td>
    <td width="17%">
      <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null) { %>
        <a href="element/node.jsp?node=<%=notice.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
          
    <td width="15%">Interface</td>

    <td width="17%">
      <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null) { %>
        <a href="element/interface.jsp?node=<%=notice.getNodeId()%>&intf=<%=notice.getIpAddress()%>"><%=notice.getIpAddress()%></a>
      <% } else if (notice.getIpAddress()!=null) { %>
        <%=notice.getIpAddress()%>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
          
    <td width="15%">Service</td>

    <td width="17%">
      <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null && notice.getServiceName()!=null) { %>
        <a href="element/service.jsp?node=<%=notice.getNodeId()%>&intf=<%=notice.getIpAddress()%>&service=<%=notice.getServiceId()%>"><%=notice.getServiceName()%></a>
      <% } else if (notice.getServiceName()!=null) { %>
        <%=notice.getServiceName()%>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
  </tr>

  <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null) { %>
    <tr class="<%=eventSeverity%>">
      <td colspan="6">
        <a href="outage/list.htm?filter=node%3D<%=notice.getNodeId()%>">See outages for <%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
      </td>
    </tr>
  <% } %>
</table>
      
<% if (notice.getNumericMessage() != null || notice.getTextMessage() != null) { %>
  <table>
    <% if (notice.getNumericMessage()!=null) { %>
      <tr class="<%=eventSeverity%>">
        <td width="10%">Numeric Message</td>
      </tr>

      <tr class="<%=eventSeverity%>">
        <td><%=notice.getNumericMessage()%></td>
      </tr>
    <% } %>
          
    <% if (notice.getTextMessage() != null) { %>
      <tr class="<%=eventSeverity%>">
        <td width="10%">Text Message</td>
      </tr>

      <tr class="<%=eventSeverity%>">
        <td><%=notice.getTextMessage()%></td>
      </tr>
    <% } %>
  </table>
<% } %>
      
<table>
  <thead>
    <tr>
      <th>Sent To</th>
      <th>Sent At</th>
      <th>Media</th>
      <th>Contact Info</th>
    </tr>
  </thead>
  
  <%  for (NoticeSentTo sentTo : notice.getSentTo()) { %>

    <tr class="<%=eventSeverity%>">
      <td><%=sentTo.getUserId()%></td>

      <td><%=org.opennms.web.Util.formatDateToUIString(sentTo.getTime())%></td>

      <td>
        <% if (sentTo.getMedia()!=null && !sentTo.getMedia().trim().equals("")) { %>
          <%=sentTo.getMedia()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>

      <td>
        <% if (sentTo.getContactInfo()!=null && !sentTo.getContactInfo().trim().equals("")) { %>
          <%=sentTo.getContactInfo()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>
    </tr>
  <% } %>
</table>
 
<br/>
     
<% if (notice.getTimeReplied()==null) { %>
  <form method="post" name="acknowledge" action="notification/acknowledge">
    <input type="hidden" name="notices" value="<%=notice.getId()%>"/>
    <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
    <input type="button" value="Acknowledge" onClick="javascript:acknowledgeNotice()"/>
  </form>
<% } %>
      
<jsp:include page="/includes/footer.jsp" flush="false" />

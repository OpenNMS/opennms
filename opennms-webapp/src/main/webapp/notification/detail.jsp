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
		org.opennms.web.notification.*,
		org.opennms.web.element.*
	"
%>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String noticeIdString = request.getParameter("notice");
    
    int noticeID = -1;
    
    try {
        noticeID = Integer.parseInt( noticeIdString );
    }
    catch( NumberFormatException e ) {
        throw new NoticeIdNotFoundException("The notice id must be an integer.",
					     noticeIdString );
    }
    
    Notification notice = this.model.getNoticeInfo(noticeID);
    
    if( notice == null ) {
        throw new NoticeIdNotFoundException("An notice with this id was not found.", String.valueOf(noticeID));
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
      
<table class="standard">
  <tr>
    <td class="standardheader" width="15%">Notification Time</td>
    <td class="standard" width="17%"><%=org.opennms.netmgt.EventConstants.formatToUIString(notice.getTimeSent())%></td>
    <td class="standardheader" width="15%">Time&nbsp;Replied</td>
    <td class="standard" width="17%"><%=notice.getTimeReplied()!=null ? org.opennms.netmgt.EventConstants.formatToUIString(notice.getTimeReplied()) : "&nbsp"%></td>
    <td class="standardheader" width="15%">Responder</td>
    <td class="standard" width="17%"><%=notice.getResponder()!=null ? notice.getResponder() : "&nbsp"%></td>
  </tr>
</table>
      
<table class="standard">
  <tr>
    <td class="standardheader" width="15%">Node</td>

    <td class="standard" width="17%">
      <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null) { %>
        <a href="element/node.jsp?node=<%=notice.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
          
    <td class="standardheader" width="15%">Interface</td>

    <td class="standard" width="17%">
      <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null) { %>
        <a href="element/interface.jsp?node=<%=notice.getNodeId()%>&intf=<%=notice.getIpAddress()%>"><%=notice.getIpAddress()%></a>
      <% } else if (notice.getIpAddress()!=null) { %>
        <%=notice.getIpAddress()%>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
          
    <td class="standardheader" width="15%">Service</td>

    <td class="standard" width="17%">
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
    <tr>
      <td class="standard" colspan="6">
        <a href="outage/list?filter=node%3D<%=notice.getNodeId()%>">See outages for <%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
      </td>
    </tr>
  <% } %>
</table>
      
<% if (notice.getNumericMessage() != null || notice.getTextMessage() != null) { %>
  <table class="standard">
    <% if (notice.getNumericMessage()!=null) { %>
      <tr>
        <td class="standardheader" width="10%">Numeric Message</td>
      </tr>

      <tr>
        <td class="standard"><%=notice.getNumericMessage()%></td>
      </tr>
    <% } %>
          
    <% if (notice.getTextMessage() != null) { %>
      <tr>
        <td class="standardheader" width="10%">Text Message</td>
      </tr>

      <tr>
        <td class="standard"><%=notice.getTextMessage()%></td>
      </tr>
    <% } %>
  </table>
<% } %>
      
<table class="standard">
  <tr>
    <td class="standardheader">Sent To</td>
    <td class="standardheader">Sent At</td>
    <td class="standardheader">Media</td>
    <td class="standardheader">Contact Info</td>
  </tr>
  
  <% List sentToList = notice.getSentTo(); %>
  <%  for (int i=0; i < sentToList.size(); i++) { %>
    <%  NoticeSentTo sentTo = (NoticeSentTo)sentToList.get(i); %>

    <tr>
      <td class="standard"><%=sentTo.getUserId()%></td>

      <td class="standard"><%=org.opennms.netmgt.EventConstants.formatToUIString(sentTo.getTime())%></td>

      <td class="standard">
        <% if (sentTo.getMedia()!=null && !sentTo.getMedia().trim().equals("")) { %>
          <%=sentTo.getMedia()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>

      <td class="standard">
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

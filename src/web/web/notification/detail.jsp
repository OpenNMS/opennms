<!--

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

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.notification.*,org.opennms.web.element.*"%>

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
        throw new org.opennms.web.notification.NoticeIdNotFoundException( "The notice id must be an integer.", noticeIdString );
    }
    
    Notification notice = this.model.getNoticeInfo(noticeID);
    
    if( notice == null ) {
        throw new org.opennms.web.notification.NoticeIdNotFoundException( "An notice with this id was not found.", String.valueOf(noticeID) );
    }
%>

<html>
<head>
  <title>Notification Detail | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    
    function acknowledgeNotice()
    {
        document.acknowledge.submit();
    }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='notification/index.jsp'>Notification</a>"; %>
<% String breadcrumb2 = "Detail"; %>
<jsp:include page="/WEB-INF/jspf/header.jspf" flush="false" >
  <jsp:param name="title" value="Notification Detail" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      
      <td width="100%" valign="top" > 
      <h3>Notice #<%=notice.getId()%> 
          <% if ( NoticeFactory.canDisplayEvent(notice.getEventId()) ) { %>
                from event #<a href="event/detail.jsp?id=<%=notice.getEventId()%>"><%=notice.getEventId()%></a>
          <% } %>
      </h3>
      
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">
        <tr>
          <td width="15%" bgcolor="#999999"><b>Notification Time</b></td>
          <td width="17%"><%=org.opennms.netmgt.EventConstants.formatToUIString(notice.getTimeSent())%></td>
          <td width="15%" bgcolor="#999999"><b>Time&nbsp;Replied</b></td>
          <td width="17%"><%=notice.getTimeReplied()!=null ? org.opennms.netmgt.EventConstants.formatToUIString(notice.getTimeReplied()) : "&nbsp"%></td>
          <td width="15%" bgcolor="#999999"><b>Responder</b></td>
          <td width="17%"><%=notice.getResponder()!=null ? notice.getResponder() : "&nbsp"%></td>
        </tr>
      </table>
      
      <br>
      
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">
        <tr>
          <td align="left" valign="top" bgcolor="#999999" width="15%"><b>Node</b></td>
          <td width="17%">
          <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null) { %>
            <a href="element/node.jsp?node=<%=notice.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
          <% } else { %>
            &nbsp;
          <% } %>
          </td>
          
          <td align="left" valign="top" bgcolor="#999999"  width="15%"><b>Interface</b></td>
          <td width="17%">
          <%if (NetworkElementFactory.getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null) { %>
            <a href="element/interface.jsp?node=<%=notice.getNodeId()%>&intf=<%=notice.getIpAddress()%>"><%=notice.getIpAddress()%></a>
          <% } else if (notice.getIpAddress()!=null) { %>
            <%=notice.getIpAddress()%>
          <% } else { %>
            &nbsp;
          <% } %>
          </td>
          
          <td align="left" valign="top" bgcolor="#999999"  width="15%"><b>Service</b></td>
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
          <tr>
            <td align="left" valign="top" colspan="6">
              <a href="outage/list?filter=node%3D<%=notice.getNodeId()%>">See outages for <%=NetworkElementFactory.getNodeLabel(notice.getNodeId())%></a>
            </td>
          </tr>
        <% } %>
      </table>
      
      <br>
      
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">
        <%if (notice.getNumericMessage()!=null) { %>
          <tr>
            <td align="left" valign="top" width="10%" bgcolor="#999999"><b>Numeric Message</b></td>
          </tr>
          <tr>
           <td align="left" valign="top"><%=notice.getNumericMessage()%></td>
          </tr>
        <% } %>
        
        <%if (notice.getTextMessage()!=null) { %>
          <tr>
            <td align="left" valign="top" width="10%" bgcolor="#999999"><b>Text Message</b></td>
          </tr>
          <tr>
            <td align="left" valign="top"><%=notice.getTextMessage()%></td>
          </tr>
        <% } %>
      </table>
      
      <br>
      
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">
        <tr>
          <td bgcolor="#999999"><b>Sent To</b></td>
          <td bgcolor="#999999"><b>Sent At</b></td>
          <td bgcolor="#999999"><b>Media</b></td>
          <td bgcolor="#999999"><b>Contact Info</b></td>
        </tr>
        <% List sentToList = notice.getSentTo();
           for (int i=0; i < sentToList.size(); i++) {
              NoticeSentTo sentTo = (NoticeSentTo)sentToList.get(i);
         %>
        <tr>
          <td><%=sentTo.getUserId()%></a></td>
          <td><%=org.opennms.netmgt.EventConstants.formatToUIString(sentTo.getTime())%></td>
          <td><% if (sentTo.getMedia()!=null && !sentTo.getMedia().trim().equals("")) { %>
                <%=sentTo.getMedia()%>
              <% } else { %>
                &nbsp;
              <% } %>
          </td>
          <td><% if (sentTo.getContactInfo()!=null && !sentTo.getContactInfo().trim().equals("")) { %>
                <%=sentTo.getContactInfo()%>
              <% } else { %>
                &nbsp;
              <% } %>
          </td>
        </tr>
        <% } %>
      </table>
      
      <br>
      
      <%if (notice.getTimeReplied()==null) { %>
        <FORM METHOD="POST" NAME="acknowledge" ACTION="notification/acknowledge">
          <input type="hidden" name="notices" value="<%=notice.getId()%>"/>
          <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
          <input type="button" value="Acknowledge" onClick="javascript:acknowledgeNotice()"/>
        </FORM>
      <% } %>
      
    </td>
    
    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/WEB-INF/jspf/footer.jspf" flush="false" />

</body>
</html>

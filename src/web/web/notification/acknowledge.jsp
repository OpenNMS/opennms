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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.notification.*,org.opennms.web.element.*" %>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String[] noticeIds = request.getParameterValues( "notices" );

    if( noticeIds == null || noticeIds.length == 0 ) {
        //ok, this is an extremely ugly way of doing this...
        //all the error checking will be cleaned up in the second
        //iteration where all the JSPs are split into JSPs and servlets
        throw new ServletException( "Must have notices specified." );
    }
    
    Notification[] notices = null;   

    //actually acknowledge the notices
    //need to handle the interim states of each of these updates;
    //for example, say we are going to acknowledge two notices,
    //the first one succeeds and the second one fails, I need to 
    //give that information to the user, right now if any fail,
    //all you get is a servlet exception (very, very ugly)

    String username = request.getRemoteUser();
    for( int i = 0; i < noticeIds.length; i++ ) {
        this.model.acknowledged( username, Integer.parseInt( noticeIds[i] ));
    }

    //here again, I'm assuming that all succeeded, really need
    //to address this in the second iteration
    notices = new Notification[noticeIds.length];

    for( int i = 0; i < noticeIds.length; i++ ) {
        notices[i] = this.model.getNoticeInfo( Integer.parseInt( noticeIds[i] ));
    }
%>
<html>
<head>
  <title>Notification Acknowledgment | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='notification/index.jsp'>Notification</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Acknowledge"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notifications Acknowledgment" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Acknowledgment Results</h3>
      
      <ul>
<%
    for( int i = 0; i < notices.length; i++ ) {
%>
        <li> Notice #<%=notices[i].getId()%> was successfully acknowledged.
<%
    }
%>
      </ul>


<%
    for( int i = 0; i < notices.length; i++ ) {
%>
      <h4> Notice <%=notices[i].getId()%> Summary </h4>
      <table WIDTH="100%" BORDER="1" cellspacing="0" CELLPADDING="2" bordercolor="#666666">
        <tr>
          <td BGCOLOR="#999999" width="15%"> <b>Notice ID</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> <a HREF="notification/detail.jsp?notice=<%=notices[i].getId()%>"><%=notices[i].getId()%></a></td>
          <td BGCOLOR="#999999" width="15%"> <b>Event ID</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> <a href="event/detail.jsp?id=<%=notices[i].getEventId()%>"><%=notices[i].getEventId()%></a></td>
          <td BGCOLOR="#999999" width="15%"> <b>Sent:</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> <%=notices[i].getTimeSent()%> </td>
        </tr>
        <tr>
          <td BGCOLOR="#999999" width="15%"> <b>Interface:</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> 
            <%if (NetworkElementFactory.getNodeLabel(notices[i].getNodeId())!=null && notices[i].getIpAddress()!=null) { %>
              <a href="element/interface.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>"><%=notices[i].getIpAddress()%></a>
            <% } else if (notices[i].getIpAddress()!=null) { %>
              <%=notices[i].getIpAddress()%>
            <% } else { %>
              &nbsp;
            <% } %>
            </td>
          <td BGCOLOR="#999999" width="15%"> <b>Service:</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> 
            <%if (NetworkElementFactory.getNodeLabel(notices[i].getNodeId())!=null && notices[i].getIpAddress()!=null && notices[i].getServiceName()!=null) { %>
              <a href="element/service.jsp?node=<%=notices[i].getNodeId()%>&intf=<%=notices[i].getIpAddress()%>&service=<%=notices[i].getServiceId()%>"><%=notices[i].getServiceName()%></a>
            <% } else if (notices[i].getServiceName()!=null) { %>
              <%=notices[i].getServiceName()%>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          
          <td BGCOLOR="#999999" width="15%"> <b>Acknowledged:</b> </td>
          <td BGCOLOR="#cccccc" width="15%"> <%=notices[i].getTimeReplied()%> </td>
        </tr>
        <%if (notices[i].getNumericMessage() != null) { %>
          <tr>
            <td COLSPAN="6" BGCOLOR="#cccccc">
              <%=notices[i].getNumericMessage()%>
            </td>
          <tr>
        <% } %>
        <%if (notices[i].getTextMessage() != null) { %>
          <tr>
            <td COLSPAN="6" BGCOLOR="#cccccc">
              <%=notices[i].getTextMessage()%>
            </td>
          </tr>
        <% } %>
      </table>
<%
    }
%>       
      
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

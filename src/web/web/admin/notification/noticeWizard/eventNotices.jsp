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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.netmgt.config.*,org.opennms.netmgt.config.notifications.*" %>

<%!
    public void init() throws ServletException {
        try {
            NotificationFactory.init();
            EventconfFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
    
    //EventconfFactory eventConfFactory = EventconfFactory.getInstance();
%>

<html>
<head>
  <title>Event Notifications | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function editNotice(name)
    {
        document.notices.userAction.value="edit";
        document.notices.notice.value=name;
        document.notices.submit();
    }
    
    function deleteNotice(name)
    {
        if (confirm("Are you sure you want to delete the notification " + name + "?"))
        {
          document.notices.userAction.value="delete";
          document.notices.notice.value=name;
          document.notices.submit();
        }
    }
    
    function setStatus(name, status)
    {
        document.notices.userAction.value=status;
        document.notices.notice.value=name;
        document.notices.submit();
    }
    
    function newNotice()
    {
        document.notices.userAction.value="new";
        document.notices.submit();
    }
    
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("admin/index.jsp") + "'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='" + java.net.URLEncoder.encode("admin/notification/index.jsp") +  "'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "Event Notifications"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Notifications" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h2>Event Notifications</h2>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
      <form METHOD="POST" NAME="notices" ACTION="admin/notification/noticeWizard/notificationWizard">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="notice" value=""/>
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTICES%>"/>
        <tr>
          <td><h3>Add a notification to an event or edit an existing event notification.</h3></td>
        </tr>
        <tr>
          <td> <input type="button" value="Add New Event Notification" onclick="javascript:newNotice()"/>
        </tr>
        <tr>
          <td valign="top">
            <h4>Event Notifications</h4>
            <table width="100%" cellspacing="2" cellpadding="2" border="1">
              <tr bgcolor="#999999">
                <td colspan="3">
                    <b>Actions</b>
                </td>
                <td>
                    <b>Notification</b>
                </td>
                <td>
                    <b>Event</b>
                </td>
              </tr>
            <% Map noticeMap = new TreeMap(NotificationFactory.getInstance().getNotifications());
               Iterator iterator = noticeMap.keySet().iterator();
               while(iterator.hasNext()) 
               { 
                 String key = (String)iterator.next();
                 Notification curNotif = (Notification)noticeMap.get(key);
            %>
                <tr>
                  <td>
                    <input type="button" value="Edit" onclick="javascript:editNotice('<%=key%>')"/>
                  </td>
                  <td>
                    <input type="button" value="Delete"  onclick="javascript:deleteNotice('<%=key%>')"/>
                  </td>
                  <td>
                    <%if (curNotif.getStatus().equals("on")) { %>
                      <input type="button" value="Turn Off" onclick="javascript:setStatus('<%=key%>','off')"/>
                    <% } else { %>
                      <input type="button" value="Turn On" onclick="javascript:setStatus('<%=key%>','on')"/>
                    <% } %>
                  </td>
                  <td>
                    <%=key%>
                  </td>
                  <td>
                    <%=EventconfFactory.getInstance().getEventLabel(curNotif.getUei())%>
                  </td>
                </tr>
            <% } %>
            </table>
          </td>
        </tr>
      </form>
      </table>
    
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%!
  public String stripUei(String uei)
    {
        int index = 0;
        String leftover = uei;
        
        for (int i = 0; i < 3; i++)
        {
            leftover = leftover.substring(leftover.indexOf('/')+1);
        }
        
        return leftover;
     }
%>

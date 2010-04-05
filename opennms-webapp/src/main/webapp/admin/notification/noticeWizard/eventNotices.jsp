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
// 2008 Jan 16: Use EventConfDao. - dj@opennms.org
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
		org.opennms.web.admin.notification.noticeWizard.*,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.notifications.*
	"
%>

<%!
    EventConfDao m_eventconfFactory;
    NotificationFactory m_notificationFactory;

    public void init() throws ServletException {
        try {
            NotificationFactory.init();
        } catch (Throwable t) {
            throw new ServletException("Could not initialize "
				       + "NotificationFactory: "
				       + t.getMessage(), t);
        }

        try {
            EventconfFactory.init();
        } catch (Throwable t) {
            throw new ServletException("Could not initialize "
				       + "EventconfFactory: "
				       + t.getMessage(), t);
        }

        m_eventconfFactory = EventconfFactory.getInstance();
        m_notificationFactory = NotificationFactory.getInstance();
    }
    
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Notifications" />
  <jsp:param name="headTitle" value="Event Notifications" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Event Notifications" />
</jsp:include>

<script type="text/javascript" >

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

<h2>Event Notifications</h2>


<h3>Add a notification to an event or edit an existing event notification</h3>
<form method="post" name="notices" action="admin/notification/noticeWizard/notificationWizard">
  <input type="hidden" name="userAction" value=""/>
  <input type="hidden" name="notice" value=""/>
  <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTICES%>"/>
  <table>
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
          <% Map<String, Notification> noticeMap = new TreeMap<String, Notification>(m_notificationFactory.getNotifications());
             for(String key : noticeMap.keySet()) {
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
                <input type="radio" value="Off" onclick="javascript:setStatus('<%=key%>','off')"/>Off
                <input type="radio" value="On" CHECKED onclick="javascript:setStatus('<%=key%>','on')"/>On
              <% } else { %>
                <input type="radio" value="Off" CHECKED onclick="javascript:setStatus('<%=key%>','off')"/>Off
                <input type="radio" value="On" onclick="javascript:setStatus('<%=key%>','on')"/>On
              <% } %>
            </td>
            <td>
              <%=key%>
            </td>
            <td>
              <%=m_eventconfFactory.getEventLabel(curNotif.getUei())%>
            </td>
          </tr>
          <% } %>
        </table>
      </td>
    </tr>
  </table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
  public String stripUei(String uei)
    {
        String leftover = uei;
        
        for (int i = 0; i < 3; i++)
        {
            leftover = leftover.substring(leftover.indexOf('/')+1);
        }
        
        return leftover;
     }
%>

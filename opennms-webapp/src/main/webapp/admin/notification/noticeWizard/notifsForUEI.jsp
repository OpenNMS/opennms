<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jun 10: Clean up table layout.  - jeffg@opennms.org
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
    public void init() throws ServletException {
        try {
            NotificationFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
	String uei=request.getParameter("uei");
	Map<String, Notification> allNotifications=NotificationFactory.getInstance().getNotifications();
	List<Notification> notifsForUEI=new ArrayList<Notification>();
	for(String key : allNotifications.keySet()) {
	    Notification notif=allNotifications.get(key);
		if(notif.getUei().equals(uei)) {
		    notifsForUEI.add(notif);
		}
	}
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Event" />
  <jsp:param name="headTitle" value="Choose Event" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/noticeWizard/eventNotices.jsp'>Event Notifications</a>" />
  <jsp:param name="breadcrumb" value="Existing notifications for UEI" />
</jsp:include>

<script type="text/javascript" >

    function next()
    {
        if (document.events.uei.selectedIndex==-1)
        {
            alert("Please select a uei to associate with this notification.");
        }
        else
        {
            document.events.submit();
        }
    }
	function submitEditForm(noticeName) {
		document.getElementById("notice").value=noticeName;
		document.editForm.submit();
	}

</script>
<!-- Hidden form that will cause the notification to be edited -->
<form action="admin/notification/noticeWizard/notificationWizard"  method="POST" name="editForm">
	<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTIFS_FOR_UEI%>"/>
	<input type="hidden" name="userAction" value="edit"/>
	<input type="hidden" id="notice" name="notice" value=""/>
</form>

<form action="admin/notification/noticeWizard/notificationWizard"  method="POST" name="newNotificationForm">
	<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTIFS_FOR_UEI%>"/>
	<input type="hidden" name="userAction" value="new"/>
	<input type="hidden" name="uei" value="<%=uei%>"/>
</form>

<h2>Existing Notifications for UEI <%=uei%></h2>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
      	 <tr><th>Name</th><th>Description</th><th>Rule</th><th>Destination path</th><th>Varbinds</th><th>Actions</th></tr>
      <% for(Notification notif : notifsForUEI) { 
          	String varbindDescription="";
          	Varbind varbind=notif.getVarbind();
          	if(varbind!=null) {
          		varbindDescription=varbind.getVbname()+"="+varbind.getVbvalue();
          	}
      		%>
	        <tr>
	        	<td><%=notif.getName()%></td>
	        	<td><%=notif.getDescription()!=null?notif.getDescription():""%></td>
	        	<td><%=notif.getRule()%></td>
	        	<td><%=notif.getDestinationPath()%></td>
	        	<td><%=varbindDescription%></td>
	        	<td><a href="javascript: void submitEditForm('<%=notif.getName()%>');">Edit</a></td>
			</tr>
<% } %>
		<tr><td colspan="6"><a href="javascript: document.newNotificationForm.submit()">Create a new notification</a></td></tr>
      </table>
<jsp:include page="/includes/footer.jsp" flush="false" />

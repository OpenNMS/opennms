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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.netmgt.config.*,org.opennms.netmgt.config.notifications.*,org.opennms.core.utils.BundleLists,org.opennms.netmgt.ConfigFileConstants,java.io.*" %>

<%!
    public void init() throws ServletException {
        try {
            EventconfFactory.init();
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<%
    HttpSession user = request.getSession(true);
    Notification newNotice = (Notification)user.getAttribute("newNotice");
%>

<html>
<head>
  <title>Choose Event | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

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

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "<a href='admin/notification/noticeWizard/eventNotices.jsp'>Event Notifications</a>"; %>
<% String breadcrumb4 = "Choose Event"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Event" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td>
    <h2><%=(newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() + "<br>" : "")%></h2>
    <h3>Choose the event uei that will trigger this notification.</h3>
    <form METHOD="POST" NAME="events" ACTION="admin/notification/noticeWizard/notificationWizard" >
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_UEIS%>"/>
      <table width="50%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top" align="left">
            <h4>Events</h4>
            <select NAME="uei" SIZE="20" >
             <%=buildEventSelect(newNotice)%>
            </select>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <input type="reset"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a HREF="javascript:next()">Next &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%!
    public String buildEventSelect(Notification notice)
      throws IOException, FileNotFoundException
    {
        List events = EventconfFactory.getInstance().getEventsByLabel();
        StringBuffer buffer = new StringBuffer();
        
        List excludeList = getExcludeList();

        Iterator i = events.iterator();

        while(i.hasNext()) //for (int i = 0; i < events.size(); i++)
        {
            Event e = (Event)i.next();
            String uei = e.getUei();
            System.out.println(uei);

            String label = e.getEventLabel();
            System.out.println(label);

            String trimmedUei = stripUei(uei);
            System.out.println(trimmedUei);
            
            if (!excludeList.contains(trimmedUei))
            {
            if (uei.equals(notice.getUei()))
            {
                    buffer.append("<option selected VALUE=" + uei + ">" + label + "</option>");
            }
            else
            {
                    buffer.append("<option value=" + uei + ">" + label + "</option>");
                }
            }
        }
        
        return buffer.toString();
    }
    
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
     
     public List getExcludeList()
      throws IOException, FileNotFoundException
     {
        List excludes = new ArrayList();
        
        Properties excludeProperties = new Properties();
	excludeProperties.load( new FileInputStream( ConfigFileConstants.getFile(ConfigFileConstants.EXCLUDE_UEI_FILE_NAME )));
        String[] ueis = BundleLists.parseBundleList( excludeProperties.getProperty( "excludes" ));
        
        for (int i = 0; i < ueis.length; i++)
        {
            excludes.add(ueis[i]);
        }
        
        return excludes;
     }
%>

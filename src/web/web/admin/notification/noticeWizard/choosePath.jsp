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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.netmgt.config.notifications.*,org.opennms.netmgt.config.*" %>

<%!
    public void init() throws ServletException {
        try {
            DestinationPathFactory.init();
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
  <title>Choose Path | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script LANGUAGE="JAVASCRIPT" >
  
    function trimString(str) 
    {
        while (str.charAt(0)==" ")
        {
          str = str.substring(1);
        }
        while (str.charAt(str.length - 1)==" ")
        {
          str = str.substring(0, str.length - 1);
        }
        return str;
    }
    
    function finish()
    {
        trimmedName = trimString(document.info.name.value);
        trimmedText = trimString(document.info.textMsg.value);
        if (trimmedName=="")
        {
            alert("Please give this notification a name.");
        }
        else if (trimmedText=="")
        {
            alert("Please enter a text message for this notification.");
        }
        else if (document.info.path.selectedIndex==-1)
        {
            alert("Please select a destination path for this notification.");
        }
        else
        {
            document.info.submit();
        }
    }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/notification/index.jsp'>Configure Notifications</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Choose Path"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Choose Path" />
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
      <h2><%=(newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() + "<br>" : "")%></h2>
      <h3>Choose the destination path and enter the information to send via the notification</h3>
      <form METHOD="POST" NAME="info" ACTION="admin/notification/noticeWizard/notificationWizard">
      <input type="hidden" name="userAction" value=""/>
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH%>"/>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td width="10%" valign="top" align="left">
            Name:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="name" value='<%=(newNotice.getName()!=null ? newNotice.getName() : "")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            Description:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="description" value='<%=(newNotice.getDescription()!=null ? newNotice.getDescription() : "")%>'/>
          </td>
        </tr>
        <tr>
          <td width="10%" valign="top" align="left">
            Choose A Path:
          </td>
          <td valign="top" align="left">
            <%=buildPathSelect(newNotice.getDestinationPath())%>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Text Message:
          </td>
          <td valign="top" align="left">
            <textarea rows="3" cols="100" name="textMsg"><%=(newNotice.getTextMessage()!=null ? newNotice.getTextMessage() : "")%></textarea>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Email Subject:
          </td>
          <td valign="top" align="left">
            <input type="text" size="100" name="subject" value='<%=(newNotice.getSubject()!=null ? newNotice.getSubject() : "")%>'/>
          </td>
         </tr>
         <tr>
          <td width="10%" valign="top" align="left">
            Special Values:
          </td>
          <td valign="top" align="left">
            <table width="100%" border="0" cellspacing="0" cellpadding="1">
              <tr>
                <td colspan="3">Can be used in both the text message and email subject:</td>
              </tr>
              <tr>
                <td>%noticeid% = notification ID number</td>
                <td>%time% = time sent</td>
                <td>%severity% = event severity</td>                          
              </tr>
              <tr>
                <td>%interface% = IP address, may be empty</td>
                <td>%service% = service name, may be empty</td>
                <td>&nbsp;</td>                                                    
              </tr>
            </table>
          </td>
         </tr>
         
        <tr>
          <td colspan="2">
            <a HREF="javascript:finish()">Finish</a>
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
    public String buildPathSelect(String currentPath)
      throws ServletException
    {
         StringBuffer buffer = new StringBuffer("<select NAME=\"path\">");
         
         Map pathsMap = null;
         
         try {
            pathsMap = new TreeMap(DestinationPathFactory.getInstance().getPaths());
         Iterator iterator = pathsMap.keySet().iterator();
         while(iterator.hasNext())
         { 
                 String key = (String)iterator.next();
                 if (key.equals(currentPath))
                 {
                    buffer.append("<option SELECTED VALUE=" + key + ">" + key + "</option>");
                 }
                 else
                 {
                    buffer.append("<option VALUE=" + key + ">" + key + "</option>");
                 }
            }
         } catch (Exception e)
         {
            throw new ServletException("couldn't get destination path list.", e);
         }
         buffer.append("</select>");
         
         return buffer.toString();
    }
%>

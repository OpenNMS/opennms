<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 13: Added the ability NOT to notify on a service via the webUI.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.web.Util,org.opennms.netmgt.filter.Filter,org.opennms.netmgt.filter.FilterParseException,org.opennms.netmgt.config.*,org.opennms.netmgt.config.notifications.*" %>

<%!
    public void init() throws ServletException {
        try {
        }
        catch( Exception e ) {
            throw new ServletException( "Cannot load configuration file", e );
        }
    }
%>

<% HttpSession user = request.getSession(true);
   Notification newNotice = (Notification)user.getAttribute("newNotice");
   String newRule = (String)request.getParameter("newRule");
   String services[] = request.getParameterValues("services");
   if (services==null)
      services = new String[0];
   String notServices[] = request.getParameterValues("notServices");
   if (notServices==null)
      notServices = new String[0];
%>

<html>
<head>
  <title>Validate Rule | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
  
  function next()
  {
      document.addresses.userAction.value="next";
      document.addresses.submit();
  }
  
  function rebuild()
  {
      document.addresses.userAction.value="rebuild";
      document.addresses.submit();
  }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "Validate Rule"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Validate Rule" />
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
      <h3>Check the TCP/IP addresses below to ensure that the rule has given the expected results. If it hasn't click the
          'Rebuild' link below the table. If the results look good continue by clicking the 'Next' link also below the table.</h3>
      <table width="100%" cellspacing="0" cellpadding="0" border="0">
        <tr>
          <td width="10%">Current Rule:
          </td>
          <td align="left"> <%=newRule%>
          </td>
      </table>
      <br>
      <form METHOD="POST" NAME="addresses" ACTION="admin/notification/noticeWizard/notificationWizard">
        <%=Util.makeHiddenTags(request)%>
        <input type="hidden" name="userAction" value=""/>
        <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_VALIDATE%>"/>
        <table width="25%" cellspacing="2" cellpadding="2" border="1">
          <tr bgcolor="#999999">
            <td width="50%">
              <b>Interfaces</b>
            </td>
            <td width="50%">
              <b>Services Associated with the Interfaces</b>
            </td>
          </tr>
          <%=buildInterfaceTable(newRule, services, notServices)%>
        </table>
        <table 
        <table width="100%" cellspacing="2" cellpadding="2" border="0">
         <tr> 
          <td>
           <a HREF="javascript:rebuild()">&#060;&#060;&#060; Rebuild</a>&nbsp;&nbsp;&nbsp;
           <a HREF="javascript:next()">Next &#062;&#062;&#062;</a>
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
  public String buildInterfaceTable(String rule, String[] serviceList, String[] notServiceList)
      throws FilterParseException
  {
          StringBuffer buffer = new StringBuffer();
          Filter filter = new Filter();
          //return filter.getIPServiceMap(rule);
          
          Map interfaces = filter.getIPServiceMap(rule);
          
          Iterator i = interfaces.keySet().iterator();
          while(i.hasNext())
          {
              String key = (String)i.next();
              buffer.append("<tr><td width=\"50%\" valign=\"top\">").append(key).append("</td>");
              buffer.append("<td width=\"50%\">");
              
              if (serviceList.length!=0 || notServiceList.length!=0)
              {
                  Map services = (Map)interfaces.get(key);
                  Iterator j = services.keySet().iterator();
                  while(j.hasNext())
                  {
                      buffer.append((String)j.next()).append("<br>");
                  }
              }
              else
              {
                  buffer.append("All services");
              }
              
              buffer.append("</td>");
                  
              buffer.append("</tr>");
          }
          
          return buffer.toString();
  }
%>

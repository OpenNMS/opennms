<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Apr 25: Created file
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
		org.opennms.web.api.Util,
        org.opennms.netmgt.filter.FilterDaoFactory,
        org.opennms.netmgt.filter.FilterParseException
	"
%>


<%
   String newRule = request.getParameter("newRule");
   String criticalIp = request.getParameter("criticalIp");
   if (criticalIp == null) { criticalIp = ""; }
   String criticalSvc = request.getParameter("criticalSvc");
   String showNodes = request.getParameter("showNodes");
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Validate Path Outage" />
  <jsp:param name="headTitle" value="Validate Path Outage" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/noticeWizard/buildPathOutage.jsp?newRule=IPADDR+IPLIKE+*.*.*.*&showNodes=on'>Configure Path Outages</a>" />
  <jsp:param name="breadcrumb" value="Validate Path Outage" />
</jsp:include>

<script type="text/javascript" >
  
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


<h3>
<% if (showNodes != null && showNodes.equals("on")) { %>
    Check the nodes below to ensure that the rule has given the expected results.
    If it hasn't click the 'Rebuild' link below the table. If the results look good
    continue by clicking the 'Finish' link also below the table.
<% } else { %>
    The rule is valid. Click the 'Rebuild' link to change the rule or else continue
    by clicking the 'Finish' link.
<% } %>
</h3>


      Current Rule: <%=newRule%>
      <br/>critical path IP address = <%=criticalIp%>
      <br/>critical path service = <%=criticalSvc%>
      <br/>
      <br/>
      <form METHOD="POST" NAME="addresses" ACTION="admin/notification/noticeWizard/notificationWizard">
        <%=Util.makeHiddenTags(request)%>
        <input type="hidden" name="userAction" value=""/>
        <input type="hidden" name="newRule" value="<%=newRule%>"/>
        <input type="hidden" name="criticalIp" value="<%=criticalIp%>"/>
        <input type="hidden" name="criticalSvc" value="<%=criticalSvc%>"/>
        <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_VALIDATE_PATH_OUTAGE%>"/>
        <% if (showNodes != null && showNodes.equals("on")) { %>
          <table width="50%" cellspacing="2" cellpadding="2" border="1">
            <tr bgcolor="#999999">
              <td>
                <b>Node ID</b>
              </td>
              <td>
                <b>Node Label</b>
              </td>
            </tr>
            <%=buildNodeTable(newRule)%>
          </table>
        <% } %>
        <br/><br/>
        <% if (criticalIp.equals("")) { %>
          <p style="color:red">You have not selected a critical path IP.
             Clicking "Finish" will clear any critical paths previously set
             for nodes matching the rule: <%= newRule %></p>
          <br/><br/>
        <% } %>
           <a HREF="javascript:rebuild()">&#139;&#139;&#139; Rebuild</a>&nbsp;&nbsp;&nbsp;
           <a HREF="javascript:next()">Finish &#155;&#155;&#155;</a>
      </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
  private String buildNodeTable(String rule)
      throws FilterParseException
  {
          StringBuffer buffer = new StringBuffer();
          SortedMap nodes = FilterDaoFactory.getInstance().getNodeMap(rule);
          Iterator i = nodes.keySet().iterator();
          while(i.hasNext())
          {
              Integer key = (Integer)i.next();
              buffer.append("<tr><td width=\"50%\" valign=\"top\">").append(key).append("</td>");
              buffer.append("<td width=\"50%\">");
              buffer.append(nodes.get(key));
              buffer.append("</td>");
              buffer.append("</tr>");
          }
          return buffer.toString();
  }
%>

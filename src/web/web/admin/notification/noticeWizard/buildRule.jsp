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

<%@page language="java" contentType="text/html" session="true" import="java.util.*,java.sql.*,org.opennms.web.admin.notification.noticeWizard.*,org.opennms.netmgt.config.*,org.opennms.netmgt.config.notifications.*" %>

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
    String newRule = request.getParameter("newRule");
%>

<html>
<head>
  <title>Choose Event | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />

<script language="Javascript" type="text/javascript" >

    function next()
    {
        document.rule.nextPage.value="<%=NotificationWizardServlet.SOURCE_PAGE_VALIDATE%>";
        document.rule.submit();
    }
    
    function skipVerification()
    {
        document.rule.nextPage.value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH%>";
        document.rule.submit();
    }

</script>
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/notification/index.jsp'>Configure Notifications</a>"; %>
<% String breadcrumb3 = "<a href='admin/notification/noticeWizard/eventNotices.jsp'>Event Notifications</a>"; %>
<% String breadcrumb4 = "Build Rule"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Build Rule" />
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
    <h3><% String mode = request.getParameter("mode");
           if ("failed".equals(mode)) { %>
              <font color="FF0000">The rule as entered is invalid, probably due to a malformed TCP/IP address. Please correct the rule to continue.</font>
           <% } else { %>
              Build the rule that determines if a notification is sent for this event based on the interface and service information contained in the event.
           <% } %>
    </h3>
    <h3>Current Rule: '<%=newRule%>'</h3>
    <form METHOD="POST" NAME="rule" ACTION="admin/notification/noticeWizard/notificationWizard" >
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_RULE%>"/>
      <input type="hidden" name="nextPage" value=""/>
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td valign="top" align="left">
            <p>Filtering on TCP/IP address uses a very flexible format, allowing you
               to separate the four octets (fields) of a TCP/IP address into specific
               searches.  An asterisk (*) in place of any octet matches any value for that
               octet. Ranges are indicated by two numbers separated by a dash (-), and
               commas are used for list demarcation.
            </p>
            <p>For example, the following fields are all valid and would each create
               the same result set--all TCP/IP addresses from 192.168.0.0 through
               192.168.255.255:
               <ul>
                  <li>192.168.*.*
                  <li>192.168.0-255.0-255
                  <li>192.168.0,1,2,3-255.*
               </ul>
            </p>
             TCP/IP Address like:<br>
             <input type="text" name="ipaddr" value='<%=getIpaddr(newRule)%>'/>
          </td>
        </tr>
        <tr>
          <td valign="top" align="left">
			<table>
				<tr>
					<td>
              			<p>Select each service you would like to filter on in conjunction with the TCP/IP address in the previous column.
               			   For example highlighting both HTTP and FTP will match TCP/IP addresses that support HTTP <b>OR</b> FTP.
             			</p>
             			Services:<br><select size="10" multiple name="services"><%=buildServiceOptions(newRule)%></select>
          			</td>
          			<td valign="top" align="left">
              			<p>Select each service you would like to do a NOT filter on in conjunction with the TCP/IP address. Highlighting
              			   multiple items ANDs them--for example, highlighting HTTP and FTP will match events (NOT on HTTP) AND (NOT on FTP).
              			</p>
              			"NOT" Services:<br><select size="10" multiple name="notServices"><%=buildNotServiceOptions(newRule)%></select>
          			</td>
        		</tr>
			</table>
			</td>
		</tr>
        <tr>
          <td colspan="2">
            <input type="reset" value="Reset Address and Services"/>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a HREF="javascript:next()">Validate rule results &#155;&#155;&#155;</a>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a HREF="javascript:skipVerification()">Skip results validation &#155;&#155;&#155;</a>
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
    public String buildServiceOptions(String rule)
        throws SQLException
    {
        List services = NotificationFactory.getInstance().getServiceNames();
        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i < services.size(); i++)
        {
            int serviceIndex = rule.indexOf((String)services.get(i));
            //check for !is<service name>
            if (serviceIndex>0 && rule.charAt(serviceIndex-3) != '!')
            {
                buffer.append("<option selected VALUE='" + services.get(i) + "'>" + services.get(i) + "</option>");
            }
            else
            {
                buffer.append("<option VALUE='" + services.get(i) + "'>" + services.get(i) + "</option>");
            }
        }
        
        return buffer.toString();
    }

    public String buildNotServiceOptions(String rule)
        throws SQLException
    {
        List services = NotificationFactory.getInstance().getServiceNames();
        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i < services.size(); i++)
        {
            //find services in the rule, but start looking after the first "!" (not), to avoid
            //the first service listing
            int serviceIndex = rule.indexOf((String)services.get(i), rule.indexOf("!"));
            //check for !is<service name>
            if (serviceIndex>0 && rule.charAt(serviceIndex-3) == '!')
            {
                buffer.append("<option selected VALUE='" + services.get(i) + "'>" + services.get(i) + "</option>");
            }
            else
            {
                buffer.append("<option VALUE='" + services.get(i) + "'>" + services.get(i) + "</option>");
            }
        }
        
        return buffer.toString();
    }

    public String getIpaddr(String rule)
        throws org.apache.regexp.RESyntaxException
    {
        org.apache.regexp.RE dirRegEx = null;
        dirRegEx = new org.apache.regexp.RE( ".+\\..+\\..+\\..+");
        
        if (dirRegEx == null)
        {
            return "*.*.*.*";
        }
        
        StringTokenizer tokens = new StringTokenizer(rule, " ");
        while(tokens.hasMoreTokens())
        {
            String nextToken = tokens.nextToken();
            if (dirRegEx.match( nextToken ))
            {
                return nextToken;
            }
        }
        
        return "*.*.*.*";
    }
%>

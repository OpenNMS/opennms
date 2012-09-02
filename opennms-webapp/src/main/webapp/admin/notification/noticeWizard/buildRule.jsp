<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		java.sql.*,
		org.opennms.web.admin.notification.noticeWizard.*,
		org.opennms.netmgt.config.*,
		org.opennms.netmgt.config.notifications.*
	"
%>

<%
    HttpSession user = request.getSession(true);
    Notification newNotice = (Notification)user.getAttribute("newNotice");
    String newRule = request.getParameter("newRule");
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Build Rule" />
  <jsp:param name="headTitle" value="Choose Target" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/noticeWizard/eventNotices.jsp'>Event Notifications</a>" />
  <jsp:param name="breadcrumb" value="Build Rule" />
</jsp:include>

<script type="text/javascript" >

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

    <h2><%=(newNotice.getName()!=null ? "Editing notice: " + newNotice.getName() + "<br/>" : "")%></h2>
    <h3><% String mode = request.getParameter("mode");
           if ("failed".equals(mode)) { %>
              <font color="FF0000">The rule as entered is invalid, possibly due to a malformed TCP/IP address or invalid
		      rule syntax. Please correct the rule to continue.</font>
           <% } else { %>
              Build the rule that determines if a notification is sent for this event based on the interface and service information contained in the event.
           <% } %>
    </h3>
<form method="post" name="rule"
      action="admin/notification/noticeWizard/notificationWizard" >
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
            <p>The following examples are all valid and yield the set of addresses from
	       192.168.0.0 through 192.168.3.255.
            </p>
            
               <ul>
                  <li>192.168.0-3.*
                  <li>192.168.0-3.0-255
                  <li>192.168.0,1,2,3.*
               </ul>
	    <p>To Use a rule based on TCP/IP addresses as described above, enter<br/><br/>
	       IPADDR IPLIKE *.*.*.*<br/><br/>in the Current Rule box below, substituting your
	       desired address fields for *.*.*.*.
	       <br/>Otherwise, you may enter any valid rule.
	    </p>
	    Current Rule:<br/>
	    <input type="text" size=100 name="newRule" value="<%=newRule%>"/>
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
             			Services:<br/><select size="10" multiple name="services"><%=buildServiceOptions(newRule)%></select>
          			</td>
          			<td valign="top" align="left">
              			<p>Select each service you would like to do a NOT filter on in conjunction with the TCP/IP address. Highlighting
              			   multiple items ANDs them--for example, highlighting HTTP and FTP will match events (NOT on HTTP) AND (NOT on FTP).
              			</p>
              			"NOT" Services:<br/><select size="10" multiple name="notServices"><%=buildNotServiceOptions(newRule)%></select>
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
           <a href="javascript:next()">Validate rule results &#155;&#155;&#155;</a>
          </td>
        </tr>
        <tr>
          <td colspan="2">
           <a href="javascript:skipVerification()">Skip results validation &#155;&#155;&#155;</a>
          </td>
        </tr>
      </table>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

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

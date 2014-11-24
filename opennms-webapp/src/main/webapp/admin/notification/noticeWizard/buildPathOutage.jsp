<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
	import="org.opennms.web.admin.notification.noticeWizard.*"
%>

<%
    String newRule = request.getParameter("newRule");
    String criticalIp = request.getParameter("criticalIp");
    String showNodes = request.getParameter("showNodes");
    String returnTo = request.getParameter("returnTo");
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Path Outages" />
  <jsp:param name="headTitle" value="Configure Path Outages" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Configure Path Outages" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/ipv6.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>" />
</jsp:include>

<script type="text/javascript" >

    function next()
    {
        var ipElement = document.getElementById("cripIn");
        if (!isValidIPAddress(ipElement.value) && !(ipElement.value == "")) {
            alert (ipElement.value + " is not a valid IP address!");
        } else {
            document.crpth.nextPage.value="<%=NotificationWizardServlet.SOURCE_PAGE_VALIDATE_PATH_OUTAGE%>";
            document.crpth.submit();
        }
    }
    
</script>

<form method="post" name="crpth"
      action="admin/notification/noticeWizard/notificationWizard" >
      
      
    <% String mode = request.getParameter("mode");
       if (mode != null && mode.endsWith("failed")) { %>
       
        <h3 style="color:red"><%=mode%>. Please check the entry for errors and re-submit.</h3>
              
    <% } %>

    <h3>Define the Critical Path</h3>

    Enter the critical path IP address in xxx.xxx.xxx.xxx or xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format. (Or leave blank to clear previously set paths.)

    <br/><br/>

    <input id="cripIn" type="text" name="criticalIp" value = '<%= (criticalIp != null ? criticalIp : "") %>' size="57" maxlength="55" />

    <br/><br/>

    critical path service:

    <br/><br/>

    <select name="criticalSvc" value="ICMP" size="1">
        <option value="ICMP">ICMP</option>
    </select>
      <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH_OUTAGE%>"/>
      <input type="hidden" name="nextPage" value=""/>
      <input type="hidden" name="returnTo" value="<%= returnTo%>"/>
    <h3>Build the rule that determines which nodes will be subject to this critical path.</h3>
            <p>Filtering on TCP/IP address uses a very flexible format, allowing you
               to separate the four octets (fields) of a TCP/IP address into specific
               searches.  An asterisk (*) in place of any octet matches any value for that
               octet. Ranges are indicated by two numbers separated by a dash (-), and
               commas are used for list demarcation.
            </p>
            <p>The following examples are all valid and yield the set of addresses from
	       192.168.0.0 through 192.168.3.255.</p>
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
           <br/><br/>

	    Show matching node list:
            <% if (showNodes == null) { %>
            <input type="checkbox" name="showNodes" checked="checked" >
            <% } else { %>
            <input type="checkbox" name="showNodes">
            <% } %>
           <br/>

           <br/>
            <input type="reset" value="Reset"/>
           <br/><br/>
           <a href="javascript:next()">Validate rule results &#155;&#155;&#155;</a>
    </form>

<jsp:include page="/includes/footer.jsp" flush="false" />


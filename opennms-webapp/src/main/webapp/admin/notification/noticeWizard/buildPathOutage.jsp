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


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Configure Path Outages" />
  <jsp:param name="headTitle" value="Configure Path Outages" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Configure Path Outages" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="ipaddress-js" />
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
  <input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_PATH_OUTAGE%>"/>
  <input type="hidden" name="nextPage" value=""/>
  <input type="hidden" name="returnTo" value="<%= returnTo%>"/>

    <% String mode = request.getParameter("mode");
       if (mode != null && mode.endsWith("failed")) { %>
        <h3 class="text-danger"><%=mode%>. Please check the entry for errors and re-submit.</h3>
    <% } %>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Define the Critical Path</h3>
      </div>
      <div class="panel-body">
        <div class="form-group">
          <label for="cripIn">Critical Path IP Address</label>
          <input id="cripIn" type="text" class="form-control" name="criticalIp" value = '<%= (criticalIp != null ? criticalIp : "") %>' maxlength="55" />
          <p class="help-block">Enter the critical path IP address in xxx.xxx.xxx.xxx or xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format. (Or leave blank to clear previously set paths.)</p>
        </div>
        <div class="form-group">
          <label for="criticalSvc">Critical Path Service</label>
          <select id="criticalSvc" name="criticalSvc" value="ICMP">
              <option value="ICMP">ICMP</option>
          </select>
        </div>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Build the rule that determines which nodes will be subject to this critical path.</h3>
      </div>
      <div class="panel-body">
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

           <div class="form-group">
             <label for="newRule">Current Rule:</label>
	     <input type="text" class="form-control" name="newRule" value="<%=newRule%>"/>
           </div>

           <div class="form-group">
             <label for="showNodes">Show matching node list:</label>
            <% if (showNodes == null) { %>
            <input type="checkbox" name="showNodes" checked="checked" >
            <% } else { %>
            <input type="checkbox" name="showNodes">
            <% } %>
           </div>

           <div class="form-group">
             <input type="reset" class="btn btn-default" value="Reset"/>
           </div>
      </div> <!-- panel-body -->
      <div class="panel-footer">
        <a href="javascript:next()">Validate rule results &#155;&#155;&#155;</a>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


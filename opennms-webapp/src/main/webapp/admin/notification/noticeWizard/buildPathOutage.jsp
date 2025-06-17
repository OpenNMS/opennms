<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.admin.notification.noticeWizard.*,
                org.opennms.core.utils.WebSecurityUtils
        "
%>

<%
    String newRule = request.getParameter("newRule");
    String criticalIp = request.getParameter("criticalIp");
    String showNodes = request.getParameter("showNodes");
    String returnTo = request.getParameter("returnTo");
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Configure Path Outages")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Configure Path Outages")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
  <input type="hidden" name="returnTo" value="<%= WebSecurityUtils.sanitizeString(returnTo) %>"/>

    <% String mode = request.getParameter("mode");
       if (mode != null && mode.endsWith("failed")) { %>
        <h3 class="text-danger"><%=mode%>. Please check the entry for errors and re-submit.</h3>
    <% } %>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Define the Critical Path</span>
      </div>
      <div class="card-body">
        <div class="form-group">
          <label for="cripIn">Critical Path IP Address</label>
          <input id="cripIn" type="text" class="form-control" name="criticalIp" value = '<%= (criticalIp != null ? WebSecurityUtils.sanitizeString(criticalIp) : "") %>' maxlength="55" autocomplete="off" />
          <p class="form-text text-muted">Enter the critical path IP address in xxx.xxx.xxx.xxx or xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format. (Or leave blank to clear previously set paths.)</p>
        </div>
        <div class="form-group">
          <label for="criticalSvc">Critical Path Service</label>
          <select id="criticalSvc" name="criticalSvc" value="ICMP" class="form-control custom-select">
              <option value="ICMP">ICMP</option>
          </select>
        </div>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Build the rule that determines which nodes will be subject to this critical path.</span>
      </div>
      <div class="card-body">
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
	     <input type="text" class="form-control" name="newRule" value="<%=WebSecurityUtils.sanitizeString(newRule)%>"/>
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
             <input type="reset" class="btn btn-secondary" value="Reset"/>
           </div>
      </div> <!-- card-body -->
      <div class="card-footer">
          <a class="btn btn-secondary" href="javascript:next()">Validate rule results <i class="fa fa-arrow-right"></i></a>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


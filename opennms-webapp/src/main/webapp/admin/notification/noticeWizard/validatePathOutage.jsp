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
	import="java.util.*,
	    org.opennms.core.utils.InetAddressUtils,
		org.opennms.web.admin.notification.noticeWizard.*,
		org.opennms.web.api.Util,
		org.opennms.netmgt.filter.FilterDaoFactory,
		org.opennms.netmgt.filter.api.FilterParseException,
		org.opennms.core.utils.WebSecurityUtils
	"
%>


<%
   String newRule = WebSecurityUtils.sanitizeString(request.getParameter("newRule"));
   String criticalIp = InetAddressUtils.normalize(WebSecurityUtils.sanitizeString(request.getParameter("criticalIp")));
   if (criticalIp == null) { criticalIp = ""; }
   String criticalSvc = WebSecurityUtils.sanitizeString(request.getParameter("criticalSvc"));
   String showNodes = WebSecurityUtils.sanitizeString(request.getParameter("showNodes"));
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Validate Path Outage")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Configure Path Outages", "admin/notification/noticeWizard/buildPathOutage.jsp?newRule=IPADDR+IPLIKE+*.*.*.*&showNodes=on")
          .breadcrumb("Validate Path Outage")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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


<div class="card">
  <div class="card-header">
    <span>
    <% if (showNodes != null && showNodes.equals("on")) { %>
        Check the nodes below to ensure that the rule has given the expected results.
        If it hasn't click the 'Rebuild' link below the table. If the results look good
        continue by clicking the 'Finish' link also below the table.
    <% } else { %>
        The rule is valid. Click the 'Rebuild' link to change the rule or else continue
        by clicking the 'Finish' link.
    <% } %>
    </span>
  </div>
  <div class="card-body">
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
        <div class="row">
          <div class="col-md-6">
            <% if (showNodes != null && showNodes.equals("on")) { %>
              <table class="table table-sm table-striped">
                <tr>
                  <th>
                    Node ID
                  </th>
                  <th>
                    Node Label
                  </th>
                </tr>
                <%=buildNodeTable(newRule)%>
              </table>
            <% } %>
          </div> <!-- column -->
        </div> <!-- row -->
        <% if (criticalIp.equals("")) { %>
          <p class="text-danger">You have not selected a critical path IP.
             Clicking "Finish" will clear any critical paths previously set
             for nodes matching the rule: <%= newRule %></p>
        <% } %>
      </form>
  </div> <!-- card-body -->
  <div class="card-footer">
      <a class="btn btn-secondary" href="javascript:rebuild()"><i class="fas fa-arrow-left"></i> Rebuild</a>
      <a class="btn btn-secondary" href="javascript:next()">Next <i class="fas fa-arrow-right"></i></a>
  </div> <!-- card-footer -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
  private String buildNodeTable(String rule)
      throws FilterParseException
  {
          StringBuffer buffer = new StringBuffer();
          SortedMap<Integer,String> nodes = FilterDaoFactory.getInstance().getNodeMap(rule);
          for (Integer key : nodes.keySet()) {
              buffer.append("<tr><td width=\"50%\" valign=\"top\">").append(key).append("</td>");
              buffer.append("<td width=\"50%\">");
              buffer.append(nodes.get(key));
              buffer.append("</td>");
              buffer.append("</tr>");
          }
          return buffer.toString();
  }
%>

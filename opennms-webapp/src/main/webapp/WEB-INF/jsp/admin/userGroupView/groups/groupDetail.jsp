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
	import="
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.users.DutySchedule
	"
%>

<%@page import="org.opennms.web.group.WebGroup"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%
    WebGroup group = (WebGroup)request.getAttribute("group");
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Group Detail")
          .headTitle("Groups")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("Group List", "admin/userGroupView/groups/list.htm")
          .breadcrumb("Group Detail")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Details for Group: <%=WebSecurityUtils.sanitizeString(group.getName())%></span>
      </div>
      <table class="table table-sm">
        <tr>
          <th>Comments:</th>
          <td width="75%">
            <%=WebSecurityUtils.sanitizeString(group.getComments())%>
          </td>
        </tr>
        <tr>
          <th>Assigned Users:</th>
          <td width="75%">
            <% Collection<String> users = group.getUsers();
            if (users.size() < 1)
            { %>
              No users belong to this group.
            <% } else { %>
              <ul class="list-unstyled">
              <% for (String user : users) { %>
               <li> <%=WebSecurityUtils.sanitizeString(user)%> </li>
              <% } %>
              </ul>
            <% } %>
          </td>
        </tr>
      </table>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span class="card-title">Duty Schedules</span>
      </div>

      <% Collection<String> dutySchedules = group.getDutySchedules(); %>
      <% if (dutySchedules.isEmpty()) { %>
          <div class="card-body">No schedule(s) defined yet.</div>
      <% } else { %>

      <table class="table table-sm table-striped">
          <tr>
          <th>Mo</th>
          <th>Tu</th>
          <th>We</th>
          <th>Th</th>
          <th>Fr</th>
          <th>Sa</th>
          <th>Su</th>
          <th>Begin Time</th>
          <th>End Time</th>
          </tr>

        <%
          for (String dutySchedule : dutySchedules) {
          DutySchedule tmp = new DutySchedule(dutySchedule);
          Vector<Object> curSched = tmp.getAsVector();
        %>
        <tr>
          <% ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
             for (int j = 0; j < 7; j++)
             {
               Boolean curDay = (Boolean)curSched.get(j);
          %>
          <td width="5%">
            <%= (curDay.booleanValue() ? days.format(j) : "X")%>
          </td>
          <% } %>
          <td width="5%">
            <%=curSched.get(7)%>
          </td>
          <td width="5%">
            <%=curSched.get(8)%>
          </td>
        </tr>
        <% } %>
      </table>
      <% } %>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

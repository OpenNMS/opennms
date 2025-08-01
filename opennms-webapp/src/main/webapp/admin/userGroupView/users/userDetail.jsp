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
	import="org.opennms.netmgt.config.*,
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.users.*,
		org.opennms.web.servlet.MissingParameterException
	"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@ page import="java.util.stream.Collectors" %>

<%
	User user = null;
	UserManager userFactory = UserFactory.getInstance();
  	String userID = request.getParameter("userID");
	if (userID == null) {
		throw new MissingParameterException("userID");
	}

	try {
		UserFactory.init();
      		user = userFactory.getUser(userID);
  	} catch (Throwable e) {
      		throw new ServletException("Could not find user " + userID + " in user factory.", e);
  	}

	if (user == null) {
      		throw new ServletException("Could not find user " + userID);
	}
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("User List", "admin/userGroupView/users/list.jsp")
          .breadcrumb("User Detail")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Details for User: <%=WebSecurityUtils.sanitizeString(user.getUserId())%></span>
      </div>
      <table class="table table-sm">
        <tr>
          <th>
            Full Name:
          </th>
          <td width="75%" ng-non-bindable>
            <%=user.getFullName().orElse("")%>
          </td>
        </tr>
        <tr>
          <th>
            Security Roles:
          </th>
          <td width="75%" ng-non-bindable>
            <%=org.apache.commons.lang.StringUtils.join(user.getRoles().toArray(new String[user.getRoles().size()]), "<br/>")%>
          </td>
        </tr>
        <tr>
          <th>
            Comments:
          </th>
          <td width="75%" ng-non-bindable>
            <%=WebSecurityUtils.sanitizeString(user.getUserComments().orElse(""))%>
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
        <h2 class="card-title">Notification Information</h2>
      </div>
      <table class="table table-sm">
                   <tr>
                     <th>
                       Email:
                     </th>
                      <td width="75%" ng-non-bindable>
                        <%=userFactory.getEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        Pager Email:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getPagerEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        XMPP Address:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getXMPPAddress(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Numerical Service:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getNumericPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        Numerical Pin:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getNumericPin(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Text Service:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getTextPage(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Text Pin:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getTextPin(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Work Phone:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getWorkPhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Mobile Phone:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getMobilePhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Home Phone:
                      </th>
                      <td ng-non-bindable>
                        <%=userFactory.getHomePhone(userID)%>
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
        <span>Duty Schedules</span>
      </div>
      <% Collection<String> dutySchedules = user.getDutySchedules(); %>
      <% if (dutySchedules.isEmpty()) { %>
      <div class="card-body">No schedule(s) defined yet.</div>
      <% } else { %>
      <table class="table table-sm table-striped">
        <thead>
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
        </thead>

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

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

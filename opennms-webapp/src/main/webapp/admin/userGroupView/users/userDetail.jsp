<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
	import="org.opennms.netmgt.config.*,
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.users.*,
		org.opennms.web.servlet.MissingParameterException
	"
%>

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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="User Detail" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/users/list.jsp'>User List</a>" />
  <jsp:param name="breadcrumb" value="User Detail" />
</jsp:include>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Details for User: <%=user.getUserId()%></span>
      </div>
      <table class="table table-sm">
        <tr>
          <th>
            Full Name:
          </th>
          <td width="75%">
            <%=user.getFullName().orElse("")%>
          </td>
        </tr>
        <tr>
          <th>
            Security Roles:
          </th>
          <td width="75%">
            <%=org.apache.commons.lang.StringUtils.join(user.getRoles().toArray(new String[user.getRoles().size()]), "<br/>")%>
          </td>
        </tr>
        <tr>
          <th>
            Comments:
          </th>
          <td width="75%">
            <%=user.getUserComments().orElse("")%>
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
                      <td width="75%">
                        <%=userFactory.getEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        Pager Email:
                      </th>
                      <td>
                        <%=userFactory.getPagerEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        XMPP Address:
                      </th>
                      <td>
                        <%=userFactory.getXMPPAddress(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Numerical Service:
                      </th>
                      <td>
                        <%=userFactory.getNumericPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <th>
                        Numerical Pin:
                      </th>
                      <td>
                        <%=userFactory.getNumericPin(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Text Service:
                      </th>
                      <td>
                        <%=userFactory.getTextPage(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Text Pin:
                      </th>
                      <td>
                        <%=userFactory.getTextPin(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Work Phone:
                      </th>
                      <td>
                        <%=userFactory.getWorkPhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Mobile Phone:
                      </th>
                      <td>
                        <%=userFactory.getMobilePhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <th>
                        Home Phone:
                      </th>
                      <td>
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

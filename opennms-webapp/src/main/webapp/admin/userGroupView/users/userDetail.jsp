<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Detail" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/users/list.jsp'>User List</a>" />
  <jsp:param name="breadcrumb" value="User Detail" />
</jsp:include>

    <table width="100%" border="0" cellspacing="0" cellpadding="2" >
      <tr>
        <td>
          <h2>Details for User: <%=user.getUserId()%> <%= user.isReadOnly()? "(Read Only)":"" %></h2>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top">
                <b>Full Name:</b>
              </td>
              <td width="90%" valign="top">
                <%=user.getFullName()%>
              </td>
            </tr>
            
            <tr>
              <td width="10%" valign="top"> 
                <b>Comments:</b>
              </td>
              <td width="90%" valign="top">
                <%=user.getUserComments()%>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>
          
            <table width="100%" border="0" cellspacing="0" cellpadding="2" >
              
              <tr>
                <td>
                  <table>
                    <tr>
                      <td>
                        <b>Notification Information</b>
                      </td>
                    </tr>
                    <tr>
                      <td width="10%" valign="top">
                        <b>Email:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Pager Email:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getPagerEmail(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>XMPP Address:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getXMPPAddress(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <td width="10%" valign="top">
                        <b>Numerical Service:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getNumericPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Numerical Pin:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getNumericPin(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Text Service:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getTextPage(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Text Pin:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getTextPin(userID)%>
                      </td>
                    </tr>
                    
                    <tr>
                      <td width="10%" valign="top">
                        <b>Work Phone:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getWorkPhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <td width="10%" valign="top">
                        <b>Mobile Phone:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getMobilePhone(userID)%>
                      </td>
                    </tr>

                    <tr>
                      <td width="10%" valign="top">
                        <b>Home Phone:</b>
                      </td>
                      <td width="90%" valign="top">
                        <%=userFactory.getHomePhone(userID)%>
                      </td>
                    </tr>

                  </table>
                </td>
              </tr>
              
              <tr>
                <td>
                <b>Duty Schedules:</b>
                  
                      <table width="50%" border="1" cellspacing="0" cellpadding="2" >
			<% Collection dutySchedules = user.getDutyScheduleCollection(); %>
                        <%
                                int i =0;
                                Iterator iter = dutySchedules.iterator();
                                while(iter.hasNext())
                                {  
                                        DutySchedule tmp = new DutySchedule((String)iter.next());
                                        Vector curSched = tmp.getAsVector();        
					i++;
                              
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
                </td>
              </tr>
        
          </table>
      </table>
 

<jsp:include page="/includes/footer.jsp" flush="false" />

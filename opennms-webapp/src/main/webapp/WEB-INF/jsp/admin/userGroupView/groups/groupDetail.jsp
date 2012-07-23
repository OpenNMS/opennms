<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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
	import="org.opennms.netmgt.config.*,
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.groups.*,
		org.opennms.netmgt.config.users.DutySchedule,
                org.opennms.web.servlet.MissingParameterException
	"
%>

<%@page import="org.opennms.web.group.WebGroup"%>

<%

    WebGroup group = (WebGroup)request.getAttribute("group");


%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Group Detail" />
  <jsp:param name="headTitle" value="Group Detail" />
  <jsp:param name="headTitle" value="Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/groups/list.htm'>Group List</a>" />
  <jsp:param name="breadcrumb" value="Group Detail" />
</jsp:include>

<h2>Details for Group: <%=group.getName()%></h2>

    <table width="100%" border="0" cellspacing="0" cellpadding="2" >
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top">
                <b>Comments:</b>
              </td>
              <td width="90%" valign="top">
                <%=group.getComments()%>
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr>
        <td>
          <table width="100%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td width="10%" valign="top">
                <b>Default Map:</b>
              </td>
              <td width="90%" valign="top">
                <%=group.getDefaultMap()%>
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
                <b>Assigned Users:</b>
                <% Collection users = group.getUsers();
                if (users.size() < 1)
                { %>
                  <table width="50%" border="0" cellspacing="0" cellpadding="2" >
                    <tr>
                      <td>
                        No users belong to this group.
                      </td>
                    </tr>
                  </table>
                <% }
                else { %>
                  <table width="50%" border="1" cellspacing="0" cellpadding="2" >
                    <% 	Iterator usersIter = (Iterator)users.iterator(); 
			while (usersIter != null && usersIter.hasNext()) { %>
                      <tr>
                        <td>
                          <%=(String)usersIter.next()%>
                        </td>
                      </tr>
                    <% } %>
                  </table>
                <% } %>
              </td>
            </tr>
            <tr>
              <td>
              <b>Duty Schedules:</b>
                    <table width="50%" border="1" cellspacing="0" cellpadding="2" >
                      <% Collection dutySchedules = group.getDutySchedules(); %>
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
        </td>
      </tr>
    </table>

<jsp:include page="/includes/footer.jsp" flush="false"/>

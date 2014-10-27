<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
%>
<%@page import="java.util.*" %>
<%@page import="org.opennms.netmgt.config.*" %>
<%@page import="org.opennms.netmgt.config.users.*" %>
<%
	UserManager userFactory;
  	Map<String,User> users = null;
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Throwable e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Users" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="User List" />
</jsp:include>

<link rel="stylesheet" href="css/font-awesome-4.0.3/css/font-awesome.min.css">

<script type="text/javascript" >

    function addNewUser()
    {
        document.allUsers.action="admin/userGroupView/users/newUser.jsp?action=new";
        document.allUsers.submit();
        
    }
    
    function detailUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/userDetail.jsp?userID=" + userID;
        document.allUsers.submit();
    }
    
    function deleteUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/deleteUser";
        document.allUsers.userID.value=userID;
        document.allUsers.submit();
    }
    
    function modifyUser(userID)
    {
        document.allUsers.action="admin/userGroupView/users/modifyUser";
        document.allUsers.userID.value=userID;
        document.allUsers.submit();
    }
    
    function renameUser(userID)
    {
        document.allUsers.userID.value=userID;
        var newID = prompt("Enter new name for user.", userID);
        
        if (newID != null && newID != "")
        {
          document.allUsers.newID.value = newID;
          document.allUsers.action="admin/userGroupView/users/renameUser";
          document.allUsers.submit();
        }
    }
    
</script>


<form method="post" name="allUsers">
<input type="hidden" name="redirect"/>
<input type="hidden" name="userID"/>
<input type="hidden" name="newID"/>
<input type="hidden" name="password"/>

<h3>User Configuration</h3>

<p>
  Click on the <i>User ID</i> link to view detailed information about a
  user.
</p>

<p>
  <a id="doNewUser" href="javascript:addNewUser()">
    <i class="fa fa-plus-circle fa-2x"></i> Add new user
  </a>
</p>

     <table width="100%" border="1" bordercolor="black">

         <tr bgcolor="#999999">
          <td width="5%"><b>Delete</b></td>
          <td width="5%"><b>Modify</b></td>
          <td width="5%"><b>Rename</b></td>
          <td width="5%"><b>User ID</b></td>
          <td width="15%"><b>Full Name</b></td>
          <td width="15%"><b>Email</b></td>
          <td width="15%"><b>Pager Email</b></td>
          <td width="15%"><b>XMPP Address</b></td>
          <!--
          <td width="10%"><b>Num Service</b></td>
          <td width="10%"><b>Num PIN</b></td>
          <td width="15%"><b>Text Service</b></td>
          <td width="15%"><b>Text PIN</b></td>
          -->
        </tr>
        <% 
           int row = 0;
           for (User curUser : users.values()) {
	      String userid = curUser.getUserId();
	      String email = userFactory.getEmail(userid);
	      String pagerEmail = userFactory.getPagerEmail(userid);
	      String xmppAddress = userFactory.getXMPPAddress(userid);
	      String numericService = userFactory.getNumericPage(userid);
	      String textService = userFactory.getTextPage(userid);
	      String numericPin = userFactory.getNumericPin(userid);
	      String textPin = userFactory.getTextPin(userid);
         %>
         <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%> id="user-<%= userid %>">
          <% if (!curUser.getUserId().equals("admin")) { %>
          <td width="5%" rowspan="2" align="center"> 
            <a id="<%= "users("+curUser.getUserId()+").doDelete" %>" href="javascript:deleteUser('<%=curUser.getUserId()%>')" onclick="return confirm('Are you sure you want to delete the user <%=curUser.getUserId()%>?')"><i class="fa fa-trash-o fa-2x"></i></a> 
          </td>
          <% } else { %>
          <td width="5%" rowspan="2" align="center">
            <i class="fa fa-trash-o fa-2x" onclick="alert('Sorry, the admin user cannot be deleted.')"></i>
          </td>
          <% } %>
          <td width="5%" rowspan="2" align="center">
            <a id="<%= "users("+curUser.getUserId()+").doModify" %>" href="javascript:modifyUser('<%=curUser.getUserId()%>')"><i class="fa fa-edit fa-2x"></i></a>
          </td>
          <td width="5%" rowspan="2" align="center">
            <% if ( !curUser.getUserId().equals("admin")) { %>
                <input id="<%= "users("+curUser.getUserId()+").doRename" %>" type="button" name="rename" value="Rename" onclick="renameUser('<%=curUser.getUserId()%>')">
              <% } else { %>
                <input id="<%= "users("+curUser.getUserId()+").doRename" %>" type="button" name="rename" value="Rename" onclick="alert('Sorry, the admin user cannot be renamed.')">
              <% } %>
          </td>
          <td width="5%">
            <a id="<%= "users("+curUser.getUserId()+").doDetails" %>" href="javascript:detailUser('<%=curUser.getUserId()%>')"><%=curUser.getUserId()%></a>
          </td>
          <td width="15%">
           <div id="<%= "users("+curUser.getUserId()+").fullName" %>">
	    <% if(curUser.getFullName() != null){ %>
		    <%= (curUser.getFullName().equals("") ? "&nbsp;" : curUser.getFullName()) %>
	    <% } %>
	      </div>
          </td>
          <td width="15%">
            <div id="<%= "users("+curUser.getUserId()+").email" %>">
            <%= ((email == null || email.equals("")) ? "&nbsp;" : email) %>
            </div>
          </td>
          <td width="15%">
           <div id="<%= "users("+curUser.getUserId()+").pagerEmail" %>">
            <%= ((pagerEmail == null || pagerEmail.equals("")) ? "&nbsp;" : pagerEmail) %>
            </div>
          </td>
          <td width="15">
           <div id="<%= "users("+curUser.getUserId()+").xmppAddress" %>">
            <%= ((xmppAddress == null || xmppAddress.equals("")) ? "&nbsp;" : xmppAddress) %>
           </div>
          </td>
          <!--
          <td width="10%">
            <div id="<%= "users("+curUser.getUserId()+").numericService" %>">
            <%= ((numericService == null || numericService.equals("")) ? "&nbsp;" : numericService) %>
            </div>
          </td>
          <td width="10%">
            <div id="<%= "users("+curUser.getUserId()+").numericPin" %>">
            <%= ((numericPin == null || numericPin.equals("")) ? "&nbsp;" : numericPin) %>
            </div>
          </td>
          <td width="15%">
           <div id="<%= "users("+curUser.getUserId()+").textService" %>">
            <%= ((textService == null || textService.equals("")) ? "&nbsp;" : textService) %>
            </div>
          </td>
          <td width="15%">
           <div id="<%= "users("+curUser.getUserId()+").textPin" %>">
            <%= ((textPin == null || textPin.equals("")) ? "&nbsp;" : textPin) %>
           </div>
          </td>
          -->
          </tr>
          <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
            <td colspan="5">
             <div id="<%= "users("+curUser.getUserId()+").userComments" %>">
	      <% if(curUser.getUserComments() != null){ %>
		      <%= (curUser.getUserComments().equals("") ? "No Comments" : curUser.getUserComments()) %>
		   
	      <% } %>
	        </div>
            </td>
          </tr>
         <% row++;
            } %>
     </table>

</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

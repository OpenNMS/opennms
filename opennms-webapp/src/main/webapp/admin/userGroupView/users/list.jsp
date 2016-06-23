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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Users" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="User List" />
</jsp:include>

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

<p>
  Click on the <i>User ID</i> link to view detailed information about a
  user.
</p>

<p>
  <a id="doNewUser" href="javascript:addNewUser()">
    <i class="fa fa-plus-circle fa-2x"></i> Add new user
  </a>
</p>

   <div class="panel panel-default">
     <table class="table table-condensed table-bordered">
        <thead>
          <tr>
          <th width="5%">Delete</th>
          <th width="5%">Modify</th>
          <th width="5%">Rename</th>
          <th width="5%">User ID</th>
          <th width="15%">Full Name</th>
          <th width="15%">Email</th>
          <th width="15%">Pager Email</th>
          <th width="15%">XMPP Address</th>
          </tr>
        </thead>
        <tbody>
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
         <tr id="user-<%= userid %>">
          <% if (!curUser.getUserId().equals("admin")) { %>
          <td rowspan="2" class="text-center"> 
            <a id="<%= "users("+curUser.getUserId()+").doDelete" %>" href="javascript:deleteUser('<%=curUser.getUserId()%>')" onclick="return confirm('Are you sure you want to delete the user <%=curUser.getUserId()%>?')"><i class="fa fa-trash-o fa-2x"></i></a> 
          </td>
          <% } else { %>
          <td rowspan="2" class="text-center">
            <i class="fa fa-trash-o fa-2x" onclick="alert('Sorry, the admin user cannot be deleted.')"></i>
          </td>
          <% } %>
          <td rowspan="2" class="text-center">
            <a id="<%= "users("+curUser.getUserId()+").doModify" %>" href="javascript:modifyUser('<%=curUser.getUserId()%>')"><i class="fa fa-edit fa-2x"></i></a>
          </td>
          <td rowspan="2" class="text-center">
            <% if ( !curUser.getUserId().equals("admin")) { %>
                <button id="<%= "users("+curUser.getUserId()+").doRename" %>" class="btn btn-default"  name="rename" onclick="renameUser('<%=curUser.getUserId()%>')">Rename</button>
              <% } else { %>
                <button id="<%= "users("+curUser.getUserId()+").doRename" %>" class="btn btn-default"  name="rename" onclick="alert('Sorry, the admin user cannot be renamed.')">Rename</button>
              <% } %>
          </td>
          <td>
            <a id="<%= "users("+curUser.getUserId()+").doDetails" %>" href="javascript:detailUser('<%=curUser.getUserId()%>')"><%=curUser.getUserId()%></a>
          </td>
          <td>
           <div id="<%= "users("+curUser.getUserId()+").fullName" %>">
	    <% if(curUser.getFullName() != null){ %>
		    <%= (curUser.getFullName().equals("") ? "&nbsp;" : curUser.getFullName()) %>
	    <% } %>
	      </div>
          </td>
          <td>
            <div id="<%= "users("+curUser.getUserId()+").email" %>">
            <%= ((email == null || email.equals("")) ? "&nbsp;" : email) %>
            </div>
          </td>
          <td>
           <div id="<%= "users("+curUser.getUserId()+").pagerEmail" %>">
            <%= ((pagerEmail == null || pagerEmail.equals("")) ? "&nbsp;" : pagerEmail) %>
            </div>
          </td>
          <td>
           <div id="<%= "users("+curUser.getUserId()+").xmppAddress" %>">
            <%= ((xmppAddress == null || xmppAddress.equals("")) ? "&nbsp;" : xmppAddress) %>
           </div>
          </td>
          </tr>
          <tr>
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
       </tbody>
     </table>
  </div> <!-- panel -->
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

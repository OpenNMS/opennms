<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*,org.opennms.netmgt.config.users.*,org.opennms.web.admin.users.parsers.NotificationInfo,java.util.*" %>
<%
	UserFactory userFactory;
  	Map users = null;
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Exception e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}
%>
<html>
<head>
  <title>List | User Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

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

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"; %>
<% String breadcrumb3 = "User List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<FORM METHOD="POST" NAME="allUsers">
<input type="hidden" name="redirect"/>
<input type="hidden" name="userID"/>
<input type="hidden" name="newID"/>
<input type="hidden" name="password"/>

<br>	
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
    <h3>User Configuration</h3>

    <p>Click on the <i>User ID</i> link to view detailed information about a user.</p>

    <p><table><tr>
      <td valign="center"><a href="javascript:addNewUser()"><img src="images/add1.gif" alt="Add new user" border="0"></a></td>
      <td valign="center"><a href="javascript:addNewUser()">Add New User</a></td>
    </tr></table></p>

     <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">

         <tr bgcolor="#999999">
          <td width="5%"><b>Delete</b></td>
          <td width="5%"><b>Modify</b></td>
          <td width="5%"><b>Rename</b></td>
          <td width="5%"><b>User ID</b></td>
          <td width="15%"><b>Full Name</b></td>
          <td width="15%"><b>Email</b></td>
          <td width="15%"><b>Pager Email</b></td>
          <!--
          <td width="10%"><b>Num Service</b></td>
          <td width="10%"><b>Num Pin</b></td>
          <td width="15%"><b>Text Service</b></td>
          <td width="15%"><b>Text Pin</b></td>
          -->
        </tr>
        <% Iterator i = users.keySet().iterator();
           int row = 0;
           while(i.hasNext()) 
           {
              User curUser = (User)users.get(i.next());
	      String userid = curUser.getUserId();
	      String email = userFactory.getEmail(userid);
	      String pagerEmail = userFactory.getPagerEmail(userid);
	      String numericService = userFactory.getNumericPage(userid);
	      String textService = userFactory.getTextPage(userid);
	      String numericPin = userFactory.getNumericPin(userid);
	      String textPin = userFactory.getTextPin(userid);
         %>
         <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
          <% if (!curUser.getUserId().equals("admin")) { %>
          <td width="5%" rowspan="2" align="center"> 
            <a href="javascript:deleteUser('<%=curUser.getUserId()%>')" onclick="return confirm('Are you sure you want to delete the user <%=curUser.getUserId()%>')"><img src="images/trash.gif" alt="<%="Delete " + curUser.getUserId()%>"></a> 
          </td>
          <% } else { %>
          <td width="5%" rowspan="2" align="center">
            <img src="images/trash.gif" alt="Cannot delete admin user">
          </td>
          <% } %>
          <td width="5%" rowspan="2" align="center">
            <a href="javascript:modifyUser('<%=curUser.getUserId()%>')"><img src="images/modify.gif"></a>
          </td>
          <td width="5%" rowspan="2" align="center">
            <% if ( !curUser.getUserId().equals("admin")) { %>
                <input type="button" name="rename" value="Rename" onclick="renameUser('<%=curUser.getUserId()%>')">
              <% } else { %>
                <input type="button" name="rename" value="Rename" onclick="alert('Sorry, the admin user cannot be renamed.')">
              <% } %>
          </td>
          <td width="5%">
            <a href="javascript:detailUser('<%=curUser.getUserId()%>')"><%=curUser.getUserId()%></a>
          </td>
          <td width="15%">
	    <% if(curUser.getFullName() != null){ %>
		    <%= (curUser.getFullName().equals("") ? "&nbsp;" : curUser.getFullName()) %>
	    <% } %>
          </td>
          <td width="33%">
            <%= ((email == null || email.equals("")) ? "&nbsp;" : email) %>
          </td>
          <td width="33%">
            <%= ((pagerEmail == null || pagerEmail.equals("")) ? "&nbsp;" : pagerEmail) %>
          </td>
          <!--
          <td width="10%">
            <%= ((numericService == null || numericService.equals("")) ? "&nbsp;" : numericService) %>
          </td>
          <td width="10%">
            <%= ((numericPin == null || numericPin.equals("")) ? "&nbsp;" : numericPin) %>
          </td>
          <td width="15%">
            <%= ((textService == null || textService.equals("")) ? "&nbsp;" : textService) %>
          </td>
          <td width="15%">
            <%= ((textPin.equals("") || textPin == null) ? "&nbsp;" : textPin) %>
          </td>
          -->
          </tr>
          <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
            <td colspan="4">
	      <% if(curUser.getUserComments() != null){ %>
		      <%= (curUser.getUserComments().equals("") ? "No Comments" : curUser.getUserComments()) %>
	      <% } %>
            </td>
          </tr>
         <% row++;
            } %>
     </table>
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>

</FORM>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

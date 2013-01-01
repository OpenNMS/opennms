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

<%@page language="java" contentType="text/html" session="true"
	import="org.opennms.netmgt.config.*,
		java.util.*,
		org.opennms.netmgt.config.users.*
	"
%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="New User" />
	<jsp:param name="headTitle" value="New" />
	<jsp:param name="headTitle" value="Users" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/users/list.jsp'>User List</a>" />
	<jsp:param name="breadcrumb" value="New User" />
</jsp:include>

<script type="text/javascript">
  function validateFormInput() 
  {
    var id = new String(document.newUserForm.userID.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The user ID '" + document.newUserForm.userID.value + "' cannot be used. It may be confused with the administration user ID 'admin'.");
        return false;
    }
    
    if (document.newUserForm.pass1.value == document.newUserForm.pass2.value) 
    {
      document.newUserForm.action="admin/userGroupView/users/addNewUser";
      return true;
    } 
    else
    {
      alert("The two password fields do not match!");
      document.newUserForm.pass1.value = "";
      document.newUserForm.pass2.value = "";
      return false;
    }
  }    
  function cancelUser()
  {
      document.newUserForm.action="admin/userGroupView/users/list.jsp";
      document.newUserForm.submit();
  }

</script>

<%if ("redo".equals(request.getParameter("action"))) { %>
  <h3>The user <%=request.getParameter("userID")%> already exists.
    Please type in a different user ID.</h3>
<%} else { %>
  <h3>Please enter a user ID and password below</h3>
<%}%>

<form id="newUserForm" method="post" name="newUserForm" onsubmit="return validateFormInput();">
  <table>
    <tr>
      <td width="10%"><label id="userIDLabel" for="userID">User ID:</label></td>
      <td width="100%"><input id="userID" type="text" name="userID"/></td>
    </tr>

    <tr>
      <td width="10%"><label id="pass1Label" for="password1">Password:</label></td>
      <td width="100%"><input id="pass1" type="password" name="pass1"/></td>
    </tr>

    <tr>
      <td width="10%"><label id="pass2Label" for="password2">Confirm Password:</label></td>
      <td width="100%"><input id="pass2" type="password" name="pass2"/></td>
    </tr>

    <tr>
      <td><input id="doOK" type="submit" value="OK"/></td>
      <td><input id="doCancel" type="button" value="Cancel" onclick="cancelUser()"/></td>
    </tr>
</table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

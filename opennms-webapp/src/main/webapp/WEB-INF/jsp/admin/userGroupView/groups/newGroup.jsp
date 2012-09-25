<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="New Group" />
	<jsp:param name="headTitle" value="New" />
	<jsp:param name="headTitle" value="Groups" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/groups/list.htm'>Group List</a>" />
	<jsp:param name="breadcrumb" value="New Group" />
</jsp:include>

<script type="text/javascript">
  function validateFormInput() 
  {
    var id = new String(document.newGroupForm.groupName.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The group ID '" + document.newGroupForm.groupName.value + "' cannot be used. It may be confused with the administration group ID 'Admin'.");
        return false;
    }
    
    document.newGroupForm.action="admin/userGroupView/groups/modifyGroup";
    document.newGroupForm.operation.value="addGroup";
    return true;
  }    
  function cancelGroup()
  {
      document.newGroupForm.action="admin/userGroupView/groups/modifyGroup";
      document.newGroupForm.operation.value="cancel";
      document.newGroupForm.submit();
  }

</script>

<%if ("redo".equals(request.getParameter("action"))) { %>
  <h3>The group <%=request.getParameter("groupName")%> already exists.
    Please type in a different group ID.</h3>
<%} else { %>
  <h3>Please enter a group ID below.</h3>
<%}%>

<form id="newGroupForm" method="post" name="newGroupForm" onsubmit="return validateFormInput();">
  <input type="hidden" name="operation" />
  <table>
    <tr>
      <td width="10%"><label id="groupNameLabel" for="groupName">Group Name:</label></td>
      <td width="100%"><input id="groupName" type="text" name="groupName"/></td>
    </tr>
    <tr>
      <td width="10%"><label id="groupCommentLabel" for="groupComment">Comment:</label></td>
      <td width="100%"><input id="groupComment" type="text" name="groupComment"/></td>
    </tr>

    <tr>
      <td><input id="doOK" type="submit" value="OK"/></td>
      <td><input id="doCancel" type="button" value="Cancel" onclick="cancelGroup()"/></td>
    </tr>
</table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

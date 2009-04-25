<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

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

<script language="JavaScript">
  function validateFormInput() 
  {
    var id = new String(document.newGroupForm.groupName.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The group ID '" + document.newGroupForm.groupName.value + "' cannot be used. It may be confused with the administration group ID 'Admin'.");
        return;
    }
    
    document.newGroupForm.action="admin/userGroupView/groups/modifyGroup";
    document.newGroupForm.operation.value="addGroup";
    document.newGroupForm.submit();
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

<form id="newGroupForm" method="post" name="newGroupForm">
  <input type="hidden" name="operation" />
  <table>
    <tr>
      <td width="10%"><label id="groupNameLabel" for="groupName">Group Name:</label></td>
      <td width="100%"><input id="groupName" type="text" name="groupName"></td>
    </tr>
    <tr>
      <td width="10%"><label id="groupCommentLabel" for="groupComment">Comment:</label></td>
      <td width="100%"><input id="groupComment" type="text" name="groupComment"></td>
    </tr>

    <tr>
      <td><input id="doOK" type="submit" value="OK" onClick="validateFormInput()"></td>
      <td><input id="doCancel" type="button" value="Cancel" onClick="cancelGroup()"></td>
    </tr>
</table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

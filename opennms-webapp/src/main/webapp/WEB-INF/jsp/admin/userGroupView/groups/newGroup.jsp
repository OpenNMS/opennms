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

<%@page language="java" contentType="text/html" session="true" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
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
    document.newGroupForm.operation.value="addGroup";
    return true;
  }    
  function cancelGroup()
  {
      document.newGroupForm.operation.value="cancel";
      document.newGroupForm.submit();
  }
</script>

<div class="card">
  <div class="card-header">
    <%if ("redo".equals(request.getParameter("action"))) { %>
      <span>The group <%=request.getParameter("groupName")%> already exists.
        Please type in a different group ID.</span>
    <%} else { %>
      <span>Please enter a group ID below.</span>
    <%}%>
  </div>
  <div class="card-body">
    <form role="form" class="form" id="newGroupForm" method="post" name="newGroupForm" onsubmit="return validateFormInput();" action="admin/userGroupView/groups/modifyGroup">
      <input type="hidden" name="operation" />

      <div class="form-group">
        <label for="groupName" class="">Group Name</label>
      <input class="form-control" id="groupName" type="text" name="groupName"/>
      </div>

      <div class="form-group">
        <label for="groupComment" class="">Comment</label>
      <input class="form-control" id="groupComment" type="text" name="groupComment"/>
      </div>

      <button type="submit" class="btn btn-secondary mr-2">OK</button>
      <button type="button" class="btn btn-secondary" onclick="cancelGroup()">Cancel</button>
    </form>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

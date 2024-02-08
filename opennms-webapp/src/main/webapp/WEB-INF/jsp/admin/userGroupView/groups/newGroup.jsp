<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("New")
          .headTitle("Groups")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("Group List", "admin/userGroupView/groups/list.htm")
          .breadcrumb("New Group")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript">
  function validateFormInput()
  {
    var id = new String(document.newGroupForm.groupName.value);
    if (id.toLowerCase()=="admin")
    {
        alert("The group ID '" + document.newGroupForm.groupName.value + "' cannot be used. It may be confused with the administration group ID 'Admin'.");
        return false;
    }

    if (/.*[&<>"`']+.*/.test(id)) {
        alert("The group ID must not contain any HTML markup.");
        return false;
    }

    var comment = new String(document.newGroupForm.groupComment.value);
    if (/.*[&<>"`']+.*/.test(comment)) {
        alert("The group comment must not contain any HTML markup.");
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
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

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

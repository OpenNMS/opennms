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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("List")
          .headTitle("Groups")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users and Groups", "admin/userGroupView/index.jsp")
          .breadcrumb("Group List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >
    function addNewGroup()
    {
        document.allGroups.action="admin/userGroupView/groups/modifyGroup";
        document.allGroups.operation.value="create";
        document.allGroups.submit();
    }
    
    function detailGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/modifyGroup";
        document.allGroups.operation.value="show";
        document.allGroups.groupName.value=groupName;
        document.allGroups.submit();
    }
    
    function deleteGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/modifyGroup";
        document.allGroups.operation.value="delete";
        document.allGroups.groupName.value=groupName;
        document.allGroups.submit();
    }
    
    function modifyGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/modifyGroup";
        document.allGroups.operation.value="edit"
        document.allGroups.groupName.value=groupName;
        document.allGroups.submit();
    }

    function renameGroup(groupName)
    {
        var newName = prompt("Enter new name for group.", groupName);

        if (newName != null && newName != "") {
          if (/.*[&<>"`']+.*/.test(newName)) {
            alert("The group ID must not contain any HTML markup.");
            return;
          }
          document.allGroups.newName.value = newName;
          document.allGroups.groupName.value=groupName;
          document.allGroups.operation.value="rename";
          document.allGroups.action="admin/userGroupView/groups/modifyGroup";
          document.allGroups.submit();
        }
    }
</script>

<p>
  Click on the <i>Group Name</i> link to view detailed information about a group.
</p>

<p>
  <a href="javascript:addNewGroup()">
    <i class="fa fa-plus-circle fa-2x"></i> Add new group
  </a>
</p>

<form method="post" name="allGroups">
  <input type="hidden" name="operation"/>
  <input type="hidden" name="groupName"/>
  <input type="hidden" name="newName"/>
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

  <div class="card">
    <table class="table table-sm table-bordered">
         <tr>
          <th>Delete</th>
          <th>Modify</th>
          <th>Rename</th>
          <th>Group Name</th>
          <th>Comments</th>
        </tr>
         <c:forEach var="group" varStatus="groupStatus" items="${groups}">
         <tr class="divider ${groupStatus.index % 2 == 0 ?  'even' : 'odd'}" id="group-${fn:escapeXml(group.name)}">
          <td width="5%" class="text-center">
            <c:choose>
              <c:when test='${fn:escapeXml(group.name) != "Admin"}'>
                <a id="${group.name}.doDelete" href="javascript:deleteGroup('${fn:escapeXml(group.name)}')" onclick="return confirm('Are you sure you want to delete the group ${fn:escapeXml(group.name)}?')"><i class="fa fa-trash-o fa-2x"></i></a>
              </c:when>
              <c:otherwise>
                <i class="fa fa-trash-o fa-2x" onclick="alert('Sorry, the ${fn:escapeXml(group.name)} group cannot be deleted.')"></i>
              </c:otherwise>
            </c:choose>
          </td>
          <td width="5%" class="text-center">
            <a id="${fn:escapeXml(group.name)}.doModify" href="javascript:modifyGroup('${fn:escapeXml(group.name)}')"><i class="fa fa-edit fa-2x"></i></a>
          </td>
          <td width="5%" class="text-center">
            <c:choose>
              <c:when test='${group.name != "Admin"}'>
                <button id="${fn:escapeXml(group.name)}.doRename" type="button" class="btn btn-secondary" name="rename" onclick="renameGroup('${fn:escapeXml(group.name)}')">Rename</button>
              </c:when>
              <c:otherwise>
                <button id="${fn:escapeXml(group.name)}.doRename" type="button" class="btn btn-secondary" name="rename" onclick="alert('Sorry, the Admin group cannot be renamed.')">Rename</button>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <a href="javascript:detailGroup('${fn:escapeXml(group.name)}')">${fn:escapeXml(group.name)}</a>
          </td>
            <td>
              <c:choose>
                <c:when test="${group.comments.isPresent()}">
                  ${fn:escapeXml(group.comments.get())}
                </c:when>
                
                <c:otherwise>
                  No Comments
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
     </table>
   </div>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

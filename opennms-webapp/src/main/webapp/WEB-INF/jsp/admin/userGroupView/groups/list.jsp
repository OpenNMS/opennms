<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Group Configuration" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="Group List" />
</jsp:include>

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

        if (newName != null && newName != "")
        {
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

  <div class="panel panel-default">
    <table class="table table-condensed table-bordered">
         <tr>
          <th>Delete</th>
          <th>Modify</th>
          <th>Rename</th>
          <th>Group Name</th>
          <th>Comments</th>
        </tr>
         <c:forEach var="group" varStatus="groupStatus" items="${groups}">
         <tr class="divider ${groupStatus.index % 2 == 0 ?  'even' : 'odd'}" id="group-${group.name}">
          <td width="5%" class="text-center">
            <c:choose>
              <c:when test='${group.name != "Admin"}'>
                <a id="${group.name}.doDelete" href="javascript:deleteGroup('${group.name}')" onclick="return confirm('Are you sure you want to delete the group ${group.name}?')"><i class="fa fa-trash-o fa-2x"></i></a>              
              </c:when>
              <c:otherwise>
                <i class="fa fa-trash-o fa-2x" onclick="alert('Sorry, the ${group.name} group cannot be deleted.')"></i>
              </c:otherwise>
            </c:choose>
          </td>
          <td width="5%" class="text-center">
            <a id="${group.name}.doModify" href="javascript:modifyGroup('${group.name}')"><i class="fa fa-edit fa-2x"></i></a>
          </td>
          <td width="5%" class="text-center">
            <c:choose>
              <c:when test='${group.name != "Admin"}'>
                <button id="${group.name}.doRename" type="button" class="btn btn-default" name="rename" onclick="renameGroup('${group.name}')">Rename</button>
              </c:when>
              <c:otherwise>
                <button id="${group.name}.doRename" type="button" class="btn btn-default" name="rename" onclick="alert('Sorry, the Admin group cannot be renamed.')">Rename</button>
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <a href="javascript:detailGroup('${group.name}')">${group.name}</a>
          </td>
            <td>
              <c:choose>
                <c:when test="${group.comments.isPresent()}">
                  ${group.comments.get()}
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

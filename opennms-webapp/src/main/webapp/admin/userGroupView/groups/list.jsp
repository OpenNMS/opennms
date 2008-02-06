<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.*,
		java.util.*,
		org.opennms.netmgt.config.groups.*
	"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    String pageAction = request.getParameter("action");
%>

<%!
    private GroupManager m_groupFactory = null;

    public void init() throws ServletException {
    	try {
            GroupFactory.init();
            m_groupFactory = GroupFactory.getInstance();
    	} catch (Exception e) {
    		throw new ServletException(e);
    	}
    }    
%>

<%
	Map<String, Group> groups = m_groupFactory.getGroups();
    pageContext.setAttribute("groups", groups.values());
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Group Configuration" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="Group List" />
</jsp:include>

<script language="Javascript" type="text/javascript" >

    function detailGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/groupDetail.jsp?groupName=" + groupName;
        document.allGroups.submit();
    }
    
    function deleteGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/deleteGroup";
        document.allGroups.groupName.value=groupName;
        document.allGroups.submit();
    }
    
    function modifyGroup(groupName)
    {
        document.allGroups.action="admin/userGroupView/groups/modifyGroup";
        document.allGroups.groupName.value=groupName;
        document.allGroups.submit();
    }

    function renameGroup(groupName)
    {
        document.allGroups.groupName.value=groupName;
        var newName = prompt("Enter new name for group.", groupName);

        if (newName != null && newName != "")
        {
          document.allGroups.newName.value = newName;
          document.allGroups.action="admin/userGroupView/groups/renameGroup";
          document.allGroups.submit();
        }
    }
    
</script>

<h3>Group Configuration</h3>

<form method="post" name="allGroups">
  <input type="hidden" name="redirect"/>
  <input type="hidden" name="groupName"/>
  <input type="hidden" name="newName"/>

<br />
       <a href="admin/userGroupView/groups/newGroup.jsp"> <img <% if (pageAction != null && pageAction.equals("cantrename")) { %> onload="alert('Unable to rename group -- new name already in use, please choose a different one.')" <% } %> src="images/add1.gif" alt="Add new group"> Add new group</a>
<br />
<br />

  <table>

         <tr>
          <th>Delete</th>
          <th>Modify</th>
          <th>Rename</th>
          <th>Group Name</th>
          <th>Comments</th>
        </tr>
         <c:forEach var="group" varStatus="groupStatus" items="${groups}">
         <tr class="divider ${groupStatus.index % 2 == 0 ?  'even' : 'odd'}" >
          <td width="5%" align="center">
            <c:choose>
              <c:when test='${group.name != "Admin"}'>
                <a href="javascript:deleteGroup('${group.name}')" onclick="return confirm('Are you sure you want to delete the group ${group.name}?')"><img src="images/trash.gif"></a>              
              </c:when>
              <c:otherwise>
                <img src="images/trash.gif" title="Cannot delete ${group.name} group">
              </c:otherwise>
            </c:choose>
          </td>
          <td width="5%" align="center">
            <a href="javascript:modifyGroup('${group.name}')"><img src="images/modify.gif"></a>
          </td>
          <td width="5%" align="center">
            <c:choose>
              <c:when test='${group.name != "Admin"}'>
                <input id="${group.name}.doRename" type="button" name="rename" value="Rename" onclick="renameGroup('${group.name}')">
              </c:when>
              <c:otherwise>
                <input id="${group.name}.doRename" type="button" name="rename" value="Rename" onclick="alert('Sorry, the Admin group cannot be renamed.')">
              </c:otherwise>
            </c:choose>
          </td>
          <td>
            <a href="javascript:detailGroup('${group.name}')">${group.name}</a>
          </td>
            <td>
              <c:choose>
                <c:when test="${!empty group.comments}">
                  ${group.comments}
                </c:when>
                
                <c:otherwise>
                  No Comments
                </c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
     </table>
</form>
<p>
  Click on the <i>Group Name</i> link to view detailed information about
  a group.
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />

<!--

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

-->

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,org.opennms.netmgt.config.groups.*"%>
<%
	GroupManager groupFactory = null;
	Map groups = null;
	
  	try
  	{
		GroupFactory.init();
		groupFactory = GroupFactory.getInstance();
      		groups = groupFactory.getGroups();
	}
	catch(Exception e)
	{
	  	throw new ServletException("GroupFactory:initializer " + e.getMessage());
	}
%>

<html>
<head>
<title>List | Group Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

    function addNewGroup()
    {
        newUserWin = window.open("admin/userGroupView/groups/newGroup.jsp", "", "fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=500,height=300");
    }
    
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

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"; %>
<% String breadcrumb3 = "Group List"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Group Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<FORM METHOD="POST" NAME="allGroups">
<input type="hidden" name="redirect"/>
<input type="hidden" name="groupName"/>
<input type="hidden" name="newName"/>

<br>
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
    <h3>Group Configuration</h3>

    <p>Click on the <i>Group Name</i> link to view detailed information about a group.</p>
    <!--<a href="javascript:addNewGroup()"> <img src="images/add1.gif" alt="Add new group"> Add new group</a>-->
     <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">

         <tr bgcolor="#999999">
          <td width="5%"><b>Delete</b></td>
          <td width="5%"><b>Modify</b></td>
          <td width="5%"><b>Rename</b></td>
          <td width="5%"><b>Group Name</b></td>
        </tr>
        <% Iterator i = groups.keySet().iterator();
           int row = 0;
           while(i.hasNext())
           {
              Group curGroup = (Group)groups.get(i.next());
         %>
         <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
          <!--
            <%--
          <% if (!curGroup.getName().equals("Network/Systems") &&
                 !curGroup.getName().equals("Desktops") &&
                 !curGroup.getName().equals("Security") &&
                 !curGroup.getName().equals("Management") ) { %>
          <td width="5%" rowspan="2" align="center">
            <a href="javascript:deleteGroup('<%=curGroup.getName()%>')" onclick="return confirm('Are you sure you want to delete the group <%=curGroup.getName()%>')"><img src="images/trash.gif" alt="<%="Delete " + curGroup.getName()%>"></a>
          </td>
          <% } else { %>
          --%>
              -->
          <td width="5%" rowspan="2" align="center">
            <img src="images/trash.gif" alt="Cannot delete <%=curGroup.getName()%> group">
          </td>
          <!--<%--<% } %> --%>-->
          <td width="5%" rowspan="2" align="center">
            <a href="javascript:modifyGroup('<%=curGroup.getName()%>')"><img src="images/modify.gif"></a>
          </td>
          <td width="5%" rowspan="2" align="center">
            <!--
            <%--
            <% if ( !curGroup.getName().equals("Network/Systems") &&
                    !curGroup.getName().equals("Desktops") &&
                    !curGroup.getName().equals("Security") &&
                    !curGroup.getName().equals("Management") ) { %>
                <input type="button" name="rename" value="Rename" onclick="renameGroup('<%=curGroup.getName()%>')">
              <% } else { %>
              --%>
              -->
                <input type="button" name="rename" value="Rename" onclick="alert('Sorry, the <%=curGroup.getName()%> group cannot be renamed.')">
              <!--<%--<% } %> --%>-->
          </td>
          <td width="5%">
            <a href="javascript:detailGroup('<%=curGroup.getName()%>')"><%=curGroup.getName()%></a>
          </td></tr>
          <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
            <td width="100%" colspan="1">
              <%= (curGroup.getComments()!=null && !curGroup.getComments().equals("") ? curGroup.getComments() : "No Comments") %>
            </td>
          </tr>
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

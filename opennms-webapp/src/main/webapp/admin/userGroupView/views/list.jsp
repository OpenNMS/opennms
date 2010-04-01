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
		org.opennms.netmgt.config.views.*
	"
%>

<%
	ViewFactory viewFactory = null;
	Map views = null;
	
  	try {
		ViewFactory.init();
		viewFactory = ViewFactory.getInstance();
		views = viewFactory.getViews();
	} catch(Exception e) {
	  	throw new ServletException("View:change " + e.getMessage());
	}
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="View Configuration" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Views" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups, and Views</a>" />
  <jsp:param name="breadcrumb" value="View List" />
</jsp:include>

<script type="text/javascript" >

    function addNewView()
    {
        newUserWin = window.open("admin/userGroupView/views/newView.jsp", "", "fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=yes,resizable=yes,directories=no,location=no,width=500,height=300");
    }
    
    function detailView(viewName)
    {
        document.allViews.action="admin/userGroupView/views/viewDetail.jsp?viewName=" + viewName;
        document.allViews.submit();
    }
    
    function deleteView(viewName)
    {
        document.allViews.action="admin/userGroupView/views/deleteView";
        document.allViews.viewName.value=viewName;
        document.allViews.submit();
    }
    
    function modifyView(viewName)
    {
        document.allViews.action="admin/userGroupView/views/modifyView";
        document.allViews.viewName.value=viewName;
        document.allViews.submit();
    }
    
    function renameView(viewName)
    {
        document.allViews.viewName.value=viewName;
        var newName = prompt("Enter new name for view.", viewName);
        
        if (newName != null && newName != "")
        {
          document.allViews.newName.value = newName;
          document.allViews.action="admin/userGroupView/views/renameView";
          document.allViews.submit();
        }
    }
    
</script>

<h3>View Configuration</h3>

<form method="post" NAME="allViews">
  <input type="hidden" name="redirect"/>
  <input type="hidden" name="viewName"/>
  <input type="hidden" name="newName"/>

    <!--<a href="javascript:addNewView()"> <img src="images/add1.gif" alt="Add new view"> Add new view</a>-->
     <p>Click on the <i>View Name</i> link to see detailed information about a view.</p>
     <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">

         <tr bgcolor="#999999">
          <td width="5%"><b>Delete</b></td>
          <td width="5%"><b>Modify</b></td>
          <td width="5%"><b>Rename</b></td>
          <td width="5%"><b>View Name</b></td>
          <td width="5%"><b>View Title</b></td>
        </tr>
        <% Iterator i = views.keySet().iterator();
           int row = 0;
           while(i.hasNext())
           {
              View curView = (View)views.get(i.next());
         %>
         <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
          <td width="5%" rowspan="2" align="center">
            <img src="images/trash.gif" alt="Cannot delete <%=curView.getName()%> view">
          </td>

          <td width="5%" rowspan="2" align="center">
            <a href="javascript:modifyView('<%=curView.getName()%>')"><img src="images/modify.gif"></a>
          </td>
          <td width="5%" rowspan="2" align="center">
            <input type="button" name="rename" value="Rename" onclick="alert('Sorry, the <%=curView.getName()%> view cannot be renamed.')">
          </td>
          <td width="10%">
            <a href="javascript:detailView('<%=curView.getName()%>')"><%=curView.getName()%></a>
          </td>
          <td width="100%">
            <%=curView.getTitle()%>
          </td></tr>
          <tr bgcolor=<%=row%2==0 ? "#ffffff" : "#cccccc"%>>
            <td width="100%" colspan="2">
              <%= (curView.getComment()!=null && !curView.getComment().equals("") ? curView.getComment() : "No Comments") %>
            </td>
          </tr>
         <% row++;
            } %>
     </table>

</form>

<jsp:include page="/includes/footer.jsp" flush="false"/>

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,org.opennms.netmgt.config.groups.*"%>
<%
	GroupFactory groupFactory = null;
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
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
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

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/userGroupView/index.jsp'>Users and Groups</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Group List"); %>
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
<jsp:include page="/includes/footer.jsp" flush="false" >
</jsp:include>
</body>
</html>

<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType = "text/html" session = "true"  import="org.opennms.netmgt.config.*, java.util.*,java.text.*,org.opennms.netmgt.config.groups.*,org.opennms.netmgt.config.users.*"%>
<%
	HttpSession userSession = request.getSession(false);
  	Group group = null;
  
	if (userSession != null)
  	{
		group = (Group)userSession.getAttribute("group.modifyGroup.jsp");
  	}

%>
<html>
<head>
<title>Modify Group | User Admin | OpenNMS Web Console</title>
<base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
<link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >
    
    function validate()
    {
        return true;
    }
    
    function addUsers() 
    {
        m1len = m1.length ;
        for ( i=0; i<m1len ; i++)
        {
            if (m1.options[i].selected == true ) 
            {
                m2len = m2.length;
                m2.options[m2len]= new Option(m1.options[i].text);
            }
        }
        
        for ( i = (m1len -1); i>=0; i--)
        {
            if (m1.options[i].selected == true ) 
            {
                m1.options[i] = null;
            }
        }
    }
    
    function removeUsers() 
    {
        m2len = m2.length ;
        for ( i=0; i<m2len ; i++)
        {
            if (m2.options[i].selected == true ) 
            {
                m1len = m1.length;
                m1.options[m1len]= new Option(m2.options[i].text);
            }
        }
        for ( i=(m2len-1); i>=0; i--) 
        {
            if (m2.options[i].selected == true ) 
            {
                m2.options[i] = null;
            }
        }
    }
    
    function selectAllAvailable()
    {
        for (i=0; i < m1.length; i++) 
        {
            m1.options[i].selected = true;
        }
    }
    
    function selectAllSelected()
    {
        for (i=0; i < m2.length; i++) 
        {
            m2.options[i].selected = true;
        }
    }
    
    function move(incr)
    {
        var i = m2.selectedIndex;	// current selection
        if( i < 0 ) return;
        var j = i + incr;		// where it will move to
        if( j < 0 || j >= m2.length ) return;
        var temp = m2.options[i].text;	// swap them
        m2.options[i].text = m2.options[j].text;
        m2.options[j].text = temp;
        m2.selectedIndex = j;		// make new location selected
    }
    
    function saveGroup()
    {
        var ok = validate();

        if(ok)
        {
            //we need to select all the users in the selectedUsers select list so the
            //request object will have all the users
            selectAllSelected();
            
            document.modifyGroup.redirect.value="/admin/userGroupView/groups/saveGroup";
            document.modifyGroup.action="admin/userGroupView/groups/updateGroup";
            document.modifyGroup.submit();
        }
    }
    
    function cancelGroup()
    {
        document.modifyGroup.action="admin/userGroupView/groups/list.jsp";
        document.modifyGroup.submit();
    }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("admin/index.jsp") + "'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='" + java.net.URLEncoder.encode("admin/userGroupView/index.jsp") + "'>Users and Groups</a>"; %>
<% String breadcrumb3 = "<a href='" + java.net.URLEncoder.encode("admin/userGroupView/groups/list.jsp") + "'>Group List</a>"; %>
<% String breadcrumb4 = "Modify Group"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Group" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>

<FORM METHOD="POST" NAME="modifyGroup">
<input type="hidden" name="groupName" value="<%=group.getName()%>"/>
<input type="hidden" name="redirect"/>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="2" >
        <tr>
          <td>
            <h3>Modifying Group: <%=group.getName()%></h3>
            <tr>
              <td>
                Assign and unassign users to the group using the select lists below. Also, change the ordering of
                the selected users by highlighting a user in the "Currently in Group" list and click the "Move Up" and "Move Down" buttons.
                The ordering of the users in the group will affect the order that the users are notified if this group is used in a notification.
                <br>
              </td>
            </tr>
            <!--<table width="100%" border="0" cellspacing="0" cellpadding="2">
              <tr>
                <td width="10%" valign="top">
                  <b>Comments:</b>
                </td>
                <td width="90%" valign="top">
                  <textarea rows="5" cols="50" name="comments"><%=group.getComments()%></textarea>
                </td>
              </tr>
            </table>-->
          </td>
        </tr>

        <tr>
          <td align="left">
            <table bgcolor="white" border="1" cellpadding="5" cellspacing="2">
              <tr>
                <td colspan="3" align="center">
                  <b>Assign/Unassign Users</b>
                </td>
              </tr>
              <tr>
                <td align="center">
                  Available Users <br>
                  <%=getAllUsersMinusInGroup(group)%><br>
                  <p align=center>
                  <input type="button" name="availableAll" onClick="selectAllAvailable()" value="Select All"><br>
                  <input type="button" onClick="addUsers()" value="&nbsp;&gt;&gt;&nbsp;"></p>
                </td>
                <td align="center">
                  Currently in Group <br>
                  <%=getUsersList(group)%><br>
                  <p align=center>
                  <input type="button" name="selectedAll" onClick="selectAllSelected()" value="Select All"><br>
                  <input type="button" onClick="removeUsers()" value="&nbsp;&lt;&lt;&nbsp;" ></p>
                </td>
                <td>
                  <input type="button" value="  Move Up   " onclick="move(-1)"> <br>
                  <input type="button" value="Move Down" onclick="move(1)">
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
    
    <td>&nbsp;</td>
  
  </tr>

  </table>

<!-- finish and discard buttons -->
  <table>
    <tr>
      <td> &nbsp; </td>
        <table>
          <tr>
            <td>
              <input type="button" name="finish" value="Finish" onclick="saveGroup()">
              <input type="button" name="cancel" value="Cancel" onclick="cancelGroup()">
            </td>
          </tr>
        </table>
      <td> &nbsp; </td>
    </tr>
  </table>
  
</FORM>
  
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
</jsp:include>

<script language="JavaScript">
  // shorthand for refering to menus
  // must run after document has been created
  // you can also change the name of the select menus and
  // you would only need to change them in one spot, here
  var m1 = document.modifyGroup.availableUsers;
  var m2 = document.modifyGroup.selectedUsers;
</script>

</body>
</html>

<%!
    private String getUsersList(Group group)
    {
        StringBuffer buffer = new StringBuffer("<select WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"selectedUsers\" size=\"10\">");
        
        Enumeration users = group.enumerateUser();
        
        while (users != null && users.hasMoreElements())
        {
            buffer.append("<option>" + (String)users.nextElement() + "</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
    
    private String getAllUsersMinusInGroup(Group group)
        throws ServletException
    {
        StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"availableUsers\" size=\"10\">");
        
        Enumeration userEnum = group.enumerateUser();
	List users = new ArrayList();
	while(userEnum.hasMoreElements())
	{
		users.add((String)userEnum.nextElement());
	}
        
        try
        {
	  UserFactory.init();
          UserFactory userFactory = UserFactory.getInstance();
          List userNames = userFactory.getUserNames();
          
          for (int i = 0; i < userNames.size(); i++)
          {
              String curUser = (String)userNames.get(i);
              
              if (!users.contains(curUser))
              {
                  buffer.append("<option>" + curUser + "</option>");
              }
          }
        }
        catch(Exception e)
        {
            throw new ServletException("Couldn't open UserFactory", e);
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>

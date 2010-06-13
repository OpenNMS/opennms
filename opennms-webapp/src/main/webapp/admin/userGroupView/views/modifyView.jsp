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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.config.*,
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.views.*
	"
%>

<%
	HttpSession userSession = request.getSession(false);
	View view = null;
  
	if (userSession != null) {
		view = (View)userSession.getAttribute("view.modifyView.jsp");
  	}

	if (view == null) {
		throw new ServletException("User session attribute "
					   + "view.modifyView.jsp is not set");
	}

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify View" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Views" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups, and Views</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/views/list.jsp'>Views List</a>" />
  <jsp:param name="breadcrumb" value="Modify View" />
</jsp:include>


<script type="text/javascript" >
    
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
    
    function addGroups() 
    {
        m3len = m3.length ;
        for ( i=0; i<m3len ; i++)
        {
            if (m3.options[i].selected == true ) 
            {
                m4len = m4.length;
                m4.options[m4len]= new Option(m3.options[i].text);
            }
        }
        
        for ( i = (m3len -1); i>=0; i--)
        {
            if (m3.options[i].selected == true ) 
            {
                m3.options[i] = null;
            }
        }
    }
    
    function removeGroups() 
    {
        m4len = m4.length ;
        for ( i=0; i<m4len ; i++)
        {
            if (m4.options[i].selected == true ) 
            {
                m3len = m3.length;
                m3.options[m3len]= new Option(m4.options[i].text);
            }
        }
        for ( i=(m4len-1); i>=0; i--) 
        {
            if (m4.options[i].selected == true ) 
            {
                m4.options[i] = null;
            }
        }
    }
    
    function selectAllAvailableUsers()
    {
        for (i=0; i < m1.length; i++) 
        {
            m1.options[i].selected = true;
        }
    }
    
    function selectAllSelectedUsers()
    {
        for (i=0; i < m2.length; i++) 
        {
            m2.options[i].selected = true;
        }
    }
    
    function selectAllAvailableGroups()
    {
        for (i=0; i < m3.length; i++) 
        {
            m3.options[i].selected = true;
        }
    }
    
    function selectAllSelectedGroups()
    {
        for (i=0; i < m4.length; i++) 
        {
            m4.options[i].selected = true;
        }
    }
    
    function saveView()
    {
        var ok = validate();
        
        if(ok)
        {
            //we need to select all the users and groups
            selectAllSelectedUsers();
            selectAllSelectedGroups();
            
            document.modifyView.redirect.value="/admin/userGroupView/views/saveView";
            document.modifyView.action="admin/userGroupView/views/updateView";
            document.modifyView.submit();
        }
    }
    
    function cancelView()
    {
        document.modifyView.action="admin/userGroupView/views/list.jsp";
        document.modifyView.submit();
    }

</script>

<h3>Modifying View: <%=view.getName()%></h3>

<p>
  Assign and unassign users and groups to the view using the select lists
  below.
</p>

<form method="post" name="modifyView">
  <input type="hidden" name="viewName" value="<%=view.getName()%>"/>
  <input type="hidden" name="redirect"/>

      <table width="100%" border="0" cellspacing="0" cellpadding="2" >
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
                  Available Users <br/>
                  <%=getAllUsersMinusInView(view)%><br/>
                  <p align=center>
                  <input type="button"  onClick="selectAllAvailableUsers()" value="Select All"><br/>
                  <input type="button" onClick="addUsers()" value="&nbsp;&#155;&#155;&nbsp;"></p>
                </td>
                <td align="center">
                  Users Currently in View <br/>
                  <%=getUsersList(view)%><br/>
                  <p align=center>
                  <input type="button" onClick="selectAllSelectedUsers()" value="Select All"><br/>
                  <input type="button" onClick="removeUsers()" value="&nbsp;&#139;&#139;&nbsp;" ></p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <tr>
          <td align="left">
            <table bgcolor="white" border="1" cellpadding="5" cellspacing="2">
              <tr>
                <td colspan="3" align="center">
                  <b>Assign/Unassign Groups</b>
                </td>
              </tr>
              <tr>
                <td align="center">
                  Available Groups <br/>
                  <%=getAllGroupsMinusInView(view)%><br/>
                  <p align=center>
                  <input type="button" onClick="selectAllAvailableGroups()" value="Select All"><br/>
                  <input type="button" onClick="addGroups()" value="&nbsp;&#155;&#155;&nbsp;"></p>
                </td>
                <td align="center">
                  Groups Currently in View <br/>
                  <%=getGroupsList(view)%><br/>
                  <p align=center>
                  <input type="button" onClick="selectAllSelectedGroups()" value="Select All"><br/>
                  <input type="button" onClick="removeGroups()" value="&nbsp;&#139;&#139;&nbsp;" ></p>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
      </table>

<!-- finish and discard buttons -->
  <table>
    <tr>
      <td> &nbsp; </td>
      <td>
        <table>
          <tr>
            <td>
              <input type="submit" name="finish" value="Finish" onclick="saveView()">
              <input type="button" name="cancel" value="Cancel" onclick="cancelView()">
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</form>

<script type="text/javascript">
  // shorthand for refering to menus
  // must run after document has been created
  // you can also change the name of the select menus and
  // you would only need to change them in one spot, here
  var m1 = document.modifyView.availableUsers;
  var m2 = document.modifyView.selectedUsers;
  var m3 = document.modifyView.availableGroups;
  var m4 = document.modifyView.selectedGroups;
</script>

<jsp:include page="/includes/footer.jsp" flush="false"/>

<%!
    private String getUsersList(View view)
    {
        StringBuffer buffer = new StringBuffer("<select WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"selectedUsers\" size=\"10\">");
        
        Membership membership = view.getMembership();
        if(membership != null)
        {
            Collection collmem = membership.getMemberCollection();
            if(collmem != null)
            {
                Iterator iterator = (Iterator)collmem.iterator();
                while(null != iterator && iterator.hasNext())
                {
                    Member member = (Member) iterator.next();
                    if(member.getType().equals("user"))
                    {
                        buffer.append("<option>" + (String)member.getContent() + "</option>");
                    }
                }
            }
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }

    private String getAllUsersMinusInView(View view)
        throws ServletException
    {
        StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"availableUsers\" size=\"10\">");
        
        List users = new ArrayList();
        Membership membership = view.getMembership();
        if(membership != null)
        {
            Collection collmem = membership.getMemberCollection();
            if(collmem != null)
            {
                Iterator iterator = (Iterator)collmem.iterator();
                while(null != iterator && iterator.hasNext())
                {
                    Member member = (Member) iterator.next();
                    if(member != null && member.getType().equals("user"))
                    {
                        users.add((String)member.getContent());
                    }
                }
            }
        }
        
        try
        {
            UserFactory.init();
            UserManager userFactory = UserFactory.getInstance();
            List userNames = userFactory.getUserNames();
            
            if(userNames != null)
            {
                for (int i = 0; i < userNames.size(); i++)
                {
                    String curUser = (String)userNames.get(i);
                    
                    if (!users.contains(curUser))
                    {
                        buffer.append("<option>" + curUser + "</option>");
                    }
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
    
    private String getGroupsList(View view)
    {
        StringBuffer buffer = new StringBuffer("<select WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"selectedGroups\" size=\"10\">");
        
        List groups = new ArrayList();
        Membership membership = view.getMembership();
        if(membership != null)
        {
	          Collection collmem = membership.getMemberCollection();
            Iterator iterator = (Iterator)collmem.iterator();
            while(iterator.hasNext())
            {
                	Member member = (Member) iterator.next();
	                if(member.getType().equals("group"))
        	        {
                	        groups.add((String)member.getContent());
                	}
            }
        }                                                    
        
        for (int i = 0; i < groups.size(); i++)
        {
            buffer.append("<option>" + (String)groups.get(i) + "</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
    
    private String getAllGroupsMinusInView(View view)
        throws ServletException
    {
        StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"availableGroups\" size=\"10\">");
        
        List groups = new ArrayList();
        Membership membership = view.getMembership();
        if(membership != null)
        {
	          Collection collmem = membership.getMemberCollection();
            if(collmem != null)
            {
                Iterator iterator = (Iterator)collmem.iterator();
                while(iterator.hasNext())
                {
                    Member member = (Member) iterator.next();
                    if(member.getType().equals("group"))
                    {
                        groups.add((String)member.getContent());
                    }
                }
        	  }
        }
        else
        {
            System.out.println("groups are null in view");
        }
        
        try
        {
            GroupFactory.init();
            GroupManager groupFactory = GroupFactory.getInstance();
            List groupNames = groupFactory.getGroupNames();
            
            for (int i = 0; i < groupNames.size(); i++)
            {
                 String curGroup = (String)groupNames.get(i);
                 
                 if (!groups.contains(curGroup))
                 {
                     buffer.append("<option>" + curGroup + "</option>");
                 }
            }
        }
        catch(Exception e)
        {
            throw new ServletException("Couldn't open GroupFactory", e);
        }
        
        buffer.append("</select>");
        
        return buffer.toString();
    }
%>

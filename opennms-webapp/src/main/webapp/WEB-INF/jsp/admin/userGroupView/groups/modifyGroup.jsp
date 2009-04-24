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
		org.opennms.netmgt.config.groups.*,
		org.opennms.netmgt.config.users.*,
		org.opennms.netmgt.dao.CategoryDao,
		org.opennms.netmgt.model.OnmsCategory
	"
%>

<%
  	Group group = (Group)request.getAttribute("group");
	String[] categoryListNotInGroup = (String[])request.getAttribute("categoryListNotInGroup");
	String[] categoryListInGroup = (String[]) request.getAttribute("categoryListInGroup");

	if (group == null) {
		throw new ServletException("Could not get session attribute "
					   + "group");
	}
	
	
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Modify Group" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/groups/list.jsp'>Group List</a>" />
  <jsp:param name="breadcrumb" value="Modify Group" />
</jsp:include>

<script language="Javascript" type="text/javascript" >
    
    function validate()
    {
        for (var c = 0; c < document.modifyGroup.dutySchedules.value; c++)
        {
            var beginName= "duty" + c + "Begin";
            var endName  = "duty" + c + "End";

            var beginValue = new Number(document.modifyGroup.elements[beginName].value);
            var endValue = new Number(document.modifyGroup.elements[endName].value);

            if (!document.modifyGroup.elements["deleteDuty"+c].checked)
            {
                if (isNaN(beginValue))
                {
                    alert("The begin time of duty schedule " + (c+1) + " must be expressed in military time with no other characters, such as 800, not 8:00");
                    return false;
                }
                if (isNaN(endValue))
                {
                    alert("The end time of duty schedule " + (c+1) + " must be expressed in military time with no other characters, such as 800, not 8:00");
                    return false;
                }
                if (beginValue > endValue)
                {
                    alert("The begin value for duty schedule " + (c+1) + " must be less than the end value.");
                    return false;
                }
                if (beginValue < 0 || beginValue > 2359)
                {
                    alert("The begin value for duty schedule " + (c+1) + " must be greater than 0 and less than 2400");
                    return false;
                }
                if (endValue < 0 || endValue > 2359)
                {
                    alert("The end value for duty schedule " + (c+1) + " must be greater than 0 and less than 2400");
                    return false;
                }
            }
        }
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

    function addGroupDutySchedules()
    {
        var ok = validate();

        if(ok)
        {
            selectAllSelected();
            document.modifyGroup.redirect.value="/admin/userGroupView/groups/addGroupDutySchedules";
            document.modifyGroup.action="admin/userGroupView/groups/updateGroup";
            document.modifyGroup.submit();
        }
    }

    function removeGroupDutySchedules()
    {
        var ok = validate();

        if(ok)
        {
            selectAllSelected();
            document.modifyGroup.redirect.value="/admin/userGroupView/groups/modifyGroup.jsp";
            document.modifyGroup.action="admin/userGroupView/groups/updateGroup";
            document.modifyGroup.submit();
        }
    }
    
    function saveGroup()
    {
        var ok = validate();

        if(ok)
        {
            //we need to select all the users in the selectedUsers select list so the
            //request object will have all the users
            selectAllSelected();
            selectAllSelectedGroups();
            alert("I am about to save");
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

    //Group functions
    function addGroup(){
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

    function removeGroup(){
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

    function selectAllAvailableGroups(){
    	for (i=0; i < m3.length; i++){
            m3.options[i].selected = true;
        }
    }

    function selectAllSelectedGroups(){
    	for (i=0; i < m4.length; i++){
            m4.options[i].selected = true;
        }
    }

</script>
<h3>Modifying Group: <%=group.getName()%></h3>

<form method="post" name="modifyGroup">
  <input type="hidden" name="groupName" value="<%=group.getName()%>"/>
  <input type="hidden" name="redirect"/>

      <table width="100%" border="0" cellspacing="0" cellpadding="2" >
        <tr>
          <td>
                Assign and unassign users to the group using the select lists below. Also, change the ordering of
                the selected users by highlighting a user in the "Currently in Group" list and click the "Move Up" and "Move Down" buttons.
                The ordering of the users in the group will affect the order that the users are notified if this group is used in a notification.
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
                  <p align="center">
                  <input type="button" name="availableAll" onClick="selectAllAvailable()" value="Select All"><br>
                  <input type="button" onClick="addUsers()" value="&nbsp;&gt;&gt;&nbsp;"></p>
                </td>
                <td align="center">
                  Currently in Group <br>
                  <%=getUsersList(group)%><br>
                  <p align="center">
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

      
	      <tr>
	          <td align="left">
	            <table bgcolor="white" border="1" cellpadding="5" cellspacing="2">
	              <tr>
	                <td colspan="3" align="center">
	                  <b>Assign/Unassign Categories</b>
	                </td>
	              </tr>
	              <tr>
	                <td align="center">
	                  Available Categories <br>
	                  <%=createAvailableGroupSelectList(categoryListNotInGroup)%><br>
	                  <p align="center">
	                  
	                  <input type="button" name="availableAll" onClick="selectAllAvailable()" value="Select All"><br>
	                  <input type="button" onClick="addGroup()" value="&nbsp;&gt;&gt;&nbsp;"></p>
	                </td>
	                <td align="center">
	                  Currently in Group <br>
	                  <%=createGroupSelectList(categoryListInGroup)%><br>
	                  <p align="center">
	                  <input type="button" name="selectedAll" onClick="selectAllSelected()" value="Select All"><br>
	                  <input type="button" onClick="removeGroup()" value="&nbsp;&lt;&lt;&nbsp;" ></p>
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
      
      <p><b>Duty Schedules</b></p>
      <table width="100%" border="1" cellspacing="0" cellpadding="2" >
        <tr bgcolor="#999999">
          <td>&nbsp;</td>
          <td><b>Delete</b></td>
          <td><b>Mo</b></td>
          <td><b>Tu</b></td>
          <td><b>We</b></td>
          <td><b>Th</b></td>
          <td><b>Fr</b></td>
          <td><b>Sa</b></td>
          <td><b>Su</b></td>
          <td><b>Begin Time</b></td>
          <td><b>End Time</b></td>
        </tr>
                    <%
                            Collection dutySchedules = group.getDutyScheduleCollection(); %>
                            <input type="hidden" name="dutySchedules" value="<%=group.getDutyScheduleCount()%>">
                    <%
                            int i =0;
                            Iterator iter = dutySchedules.iterator();
                            while(iter.hasNext())
                            {
                                    DutySchedule tmp = new DutySchedule((String)iter.next());
                                    Vector curSched = tmp.getAsVector();
                    %>
                    <tr>
                      <td width="1%"><%=(i+1)%></td>
                      <td width="1%">
                        <input type="checkbox" name="deleteDuty<%=i%>">
                      </td>
                      <% ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
                         for (int j = 0; j < 7; j++)
                         {
                            Boolean curDay = (Boolean)curSched.get(j);
                      %>
                      <td width="5%">
                        <input type="checkbox" name="duty<%=i+days.format(j)%>" <%= (curDay.booleanValue() ? "checked" : "")%>>
                      </td>
                      <% } %>
                      <td width="5%">
                        <input type="text" size="4" name="duty<%=i%>Begin" value="<%=curSched.get(7)%>">
                      </td>
                      <td width="5%">
                        <input type="text" size="4" name="duty<%=i%>End" value="<%=curSched.get(8)%>">
                      </td>
                    </tr>
                    <% i++; } %>
      </table>

  <p><input type="button" name="addSchedule" value="Add This Many Schedules" onclick="addGroupDutySchedules()">
    <select name="numSchedules" value="3" size="1">
      <option value="1">1</option>
      <option value="2">2</option>
      <option value="3">3</option>
      <option value="4">4</option>
      <option value="5">5</option>
      <option value="6">6</option>
      <option value="7">7</option>
    </select>
  </p>

  <p><input type="button" name="addSchedule" value="Remove Checked Schedules" onclick="removeGroupDutySchedules()"></p>


<!-- finish and discard buttons -->
  <table>
    <tr>
      <td> &nbsp; </td>
      <td>
        <table>
          <tr>
            <td>
              <input type="submit" name="finish" value="Finish" onclick="saveGroup()">
              <input type="button" name="cancel" value="Cancel" onclick="cancelGroup()">
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</form>
  
<script language="JavaScript">
  // shorthand for refering to menus
  // must run after document has been created
  // you can also change the name of the select menus and
  // you would only need to change them in one spot, here
  var m1 = document.modifyGroup.availableUsers;
  var m2 = document.modifyGroup.selectedUsers;
  var m3 = document.modifyGroup.availableGroups;
  var m4 = document.modifyGroup.selectedGroups;
</script>

<jsp:include page="/includes/footer.jsp" flush="false" />



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
          UserManager userFactory = UserFactory.getInstance();
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
    
    private String createAvailableGroupSelectList(String[] categories){
    	StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"availableGroups\" size=\"10\">");
    	for(String category : categories){
    		buffer.append("<option>" + category + "</option>");
    	}
    	buffer.append("</select>");
    	
    	return buffer.toString();
    }
    
    private String createGroupSelectList(String[] categories){
    	StringBuffer buffer = new StringBuffer("<select  WIDTH=\"200\" STYLE=\"width: 200px\" multiple name=\"selectedGroups\" size=\"10\">");
    	for(String category : categories){
    		buffer.append("<option>" + category + "</option>");
    	}
    	buffer.append("</select>");
    	
    	return buffer.toString();
    }
%>

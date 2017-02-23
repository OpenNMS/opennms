<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="
		java.util.*,
		java.text.*,
		org.opennms.netmgt.config.users.*
	"
%>
<%@page import="org.opennms.web.group.WebGroup"%>

<%
  	WebGroup group = (WebGroup)session.getAttribute("group.modifyGroup.jsp");
    if (group == null) {
        throw new ServletException("Could not get session attribute group");
    }
    String[] allCategories = (String[])session.getAttribute("allCategories.modifyGroup.jsp");
    String[] allUsers = (String[])session.getAttribute("allUsers.modifyGroup.jsp");
	String[] categoryListInGroup = group.getAuthorizedCategories().toArray(new String[0]);
    String[] categoryListNotInGroup = group.getUnauthorizedCategories(Arrays.asList(allCategories)).toArray(new String[0]);
    String[] selectedUsers = group.getUsers().toArray(new String[0]);
    String[] availableUsers = group.getRemainingUsers(Arrays.asList(allUsers)).toArray(new String[0]);
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Modify Group" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Groups" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users and Groups</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/userGroupView/groups/list.htm'>Group List</a>" />
  <jsp:param name="breadcrumb" value="Modify Group" />
</jsp:include>

<script type="text/javascript" >
    
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
        var i = m2.selectedIndex;   // current selection
        if( i < 0 ) return;
        var j = i + incr;       // where it will move to
        if( j < 0 || j >= m2.length ) return;
        var temp = m2.options[i].text;  // swap them
        m2.options[i].text = m2.options[j].text;
        m2.options[j].text = temp;
        m2.selectedIndex = j;       // make new location selected
    }

    function moveCat(incr)
    {
        var i = m4.selectedIndex;   // current selection
        if( i < 0 ) return;
        var j = i + incr;       // where it will move to
        if( j < 0 || j >= m4.length ) return;
        var temp = m4.options[i].text;  // swap them
        m4.options[i].text = m4.options[j].text;
        m4.options[j].text = temp;
        m4.selectedIndex = j;       // make new location selected
    }

    function addGroupDutySchedules()
    {
        var ok = validate();

        if(ok)
        {
            selectAllSelected();
            selectAllSelectedCategories();
            document.modifyGroup.operation.value="addDutySchedules";
            document.modifyGroup.submit();
        }
    }

    function removeGroupDutySchedules()
    {
        var ok = validate();

        if(ok)
        {
            selectAllSelected();
            selectAllSelectedCategories();
            document.modifyGroup.operation.value="removeDutySchedules";
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
            selectAllSelectedCategories();
            document.modifyGroup.operation.value="save";
            document.modifyGroup.submit();
        }
    }
    
    function cancelGroup()
    {
        document.modifyGroup.operation.value="cancel";
        document.modifyGroup.submit();
    }

    //Group functions
    function addCategories(){
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

    function removeCategories(){
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

    function selectAllAvailableCategories(){
    	for (i=0; i < m3.length; i++){
            m3.options[i].selected = true;
        }
    }

    function selectAllSelectedCategories(){
    	for (i=0; i < m4.length; i++){
            m4.options[i].selected = true;
        }
    }
</script>

<form role="form" class="form-horizontal" method="post" id="modifyGroup" name="modifyGroup">
  <input type="hidden" name="groupName" value="<%=group.getName()%>"/>
  <input type="hidden" name="operation"/>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Assignments</h3>
      </div>
      <div class="panel-body">
        <p>Assign and unassign users to the group using the select lists below. Also, change the ordering of the selected users by highlighting a user in the "Currently in Group" list and click the "Move Up" and "Move Down" buttons. The ordering of the users in the group will affect the order that the users are notified if this group is used in a notification.</p>
        <div class="row">
          <div class="col-md-6">
            <table class="table" id="modifyGroupUsers">
              <tr>
                <td colspan="3" align="center">
                  <b>Assign/Unassign Users</b>
                </td>
              </tr>
              <tr>
                <td align="center">
                  <label class="control-label">Available Users</label>
                  <%=createSelectList("availableUsers", availableUsers)%><br/>
                  <div class="btn-group-vertical" align="center">
                    <button type="button" class="btn btn-default" name="availableAll" onClick="javascript:selectAllAvailable()">Select All</button>
                    <button type="button" class="btn btn-default" id="users.doAdd" onClick="javascript:addUsers()">&nbsp;&#155;&#155;&nbsp;</button>
                  </div>
                </td>
                <td align="center">
                  <label class="control-label">Currently in Group</label>
                  <%=createSelectList("selectedUsers", selectedUsers)%><br/>
                  <div class="btn-group-vertical" align="center">
                    <button type="button" class="btn btn-default" name="selectedAll" onClick="javascript:selectAllSelected()">Select All</button>
                    <button type="button" class="btn btn-default" id="users.doRemove" onClick="javascript:removeUsers()">&nbsp;&#139;&#139;&nbsp;</button>
                  </div>
                </td>
                <td>
                  <div class="btn-group-vertical">
                    <button type="button" class="btn btn-default" onClick="javascript:move(-1)">Move Up</button>
                    <button type="button" class="btn btn-default" onClick="javascript:move(1)">Move Down</button>
                  </div>
                </td>
              </tr>
            </table>
          </div> <!-- column -->
          <div class="col-md-6">
	            <table class="table" id="modifyGroupCategories">
	              <tr>
	                <td colspan="3" align="center">
	                  <b>Assign/Unassign Categories</b>
	                </td>
	              </tr>
	              <tr>
	                <td align="center">
	                  <label class="control-label">Available Categories</label>
	                  <%=createSelectList("availableCategories", categoryListNotInGroup)%><br/>
                    <div class="btn-group-vertical" align="center">
  	                  <button type="button" class="btn btn-default" name="availableAll" onClick="javascript:selectAllAvailableCategories()">Select All</button>
  	                  <button type="button" class="btn btn-default" id="categories.doAdd" onClick="javascript:addCategories()">&nbsp;&#155;&#155;&nbsp;</button>
                    </div>
	                </td>
	                <td align="center">
	                  <label class="control-label">Currently in Group</label>
	                  <%=createSelectList("selectedCategories", categoryListInGroup)%><br/>
                    <div class="btn-group-vertical" align="center">
  	                  <button type="button" class="btn btn-default" name="selectedAll" onClick="javascript:selectAllSelectedCategories()">Select All</button>
  	                  <button type="button" class="btn btn-default" id="categories.doRemove" onClick="javascript:removeCategories()">&nbsp;&#139;&#139;&nbsp;</button>
                    </div>
	                </td>
	                <td>
                    <div class="btn-group-vertical">
  	                  <button type="button" class="btn btn-default" onClick="javascript:moveCat(-1)">Move Up</button>
  	                  <button type="button" class="btn btn-default" onClick="javascript:moveCat(1)">Move Down</button>
                    </div>
	                </td>
	              </tr>
	            </table>
          </div> <!-- column -->
        </div> <!-- row -->
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->
      
<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Duty Schedules</h3>
      </div>
      <table class="table table-condensed table-striped table-bordered">
            <tr>
              <th>&nbsp;</th>
              <th>Delete</th>
              <th>Mo</th>
              <th>Tu</th>
              <th>We</th>
              <th>Th</th>
              <th>Fr</th>
              <th>Sa</th>
              <th>Su</th>
              <th>Begin Time</th>
              <th>End Time</th>
            </tr>
                <%
                           int i = 0;
                           for(String dutySchedSpec : group.getDutySchedules()) {
                               DutySchedule tmp = new DutySchedule(dutySchedSpec);
                               Vector<Object> curSched = tmp.getAsVector();
                        %>
                        <tr>
                          <td width="1%"><%=(i+1)%></td>
                          <td width="1%">
                            <input type="checkbox" name="deleteDuty<%=i%>"/>
                          </td>
                          <% ChoiceFormat days = new ChoiceFormat("0#Mo|1#Tu|2#We|3#Th|4#Fr|5#Sa|6#Su");
                             for (int j = 0; j < 7; j++)
                             {
                                Boolean curDay = (Boolean)curSched.get(j);
                          %>
                          <td width="5%">
                            <input type="checkbox" name="duty<%=i+days.format(j)%>" <%= (curDay.booleanValue() ? "checked" : "")%>/>
                          </td>
                          <% } %>
                          <td width="5%">
                            <input type="text" class="form-control" size="4" name="duty<%=i%>Begin" value="<%=curSched.get(7)%>"/>
                          </td>
                          <td width="5%">
                            <input type="text" class="form-control" size="4" name="duty<%=i%>End" value="<%=curSched.get(8)%>"/>
                          </td>
                        </tr>
                        <% i++; } %>
      </table>

      <div class="form-group top-buffer">
        <div class="col-sm-12">
          <input type="hidden" name="dutySchedules" value="<%=group.getDutySchedules().size()%>"/>
          <button id="addSchedule" class="btn btn-default" name="addSchedule" onclick="addGroupDutySchedules()">Add This Many Schedules</button>
          <select name="numSchedules" class="btn btn-default">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3" selected="selected">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <div class="col-sm-12">
          <button type="button" name="addSchedule" class="btn btn-default" onclick="removeGroupDutySchedules()">Remove Checked Schedules</button>
        </div>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <button type="button" class="btn btn-default" name="finish" onclick="saveGroup()">Finish</button>
    <button type="button" class="btn btn-default" name="cancel" onclick="cancelGroup()">Cancel</button>
  </div> <!-- column -->
</div> <!-- row -->

</form>

<script type="text/javascript">
  // shorthand for refering to menus
  // must run after document has been created
  // you can also change the name of the select menus and
  // you would only need to change them in one spot, here
  var m1 = document.modifyGroup.availableUsers;
  var m2 = document.modifyGroup.selectedUsers;
  var m3 = document.modifyGroup.availableCategories;
  var m4 = document.modifyGroup.selectedCategories;
</script>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    private String createSelectList(String name, String[] categories) {
        StringBuffer buffer = new StringBuffer("<select class=\"form-control\" multiple=\"multiple\" name=\""+name+"\" size=\"10\">");
        for(String category : categories){
            buffer.append("<option>" + category + "</option>");
        }
        buffer.append("</select>");
        
        return buffer.toString();
    }
    
%>

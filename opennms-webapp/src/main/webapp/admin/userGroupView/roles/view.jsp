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
	import="org.opennms.netmgt.config.users.*,
	        org.opennms.netmgt.config.*,
		java.util.*"
%>

<%
	UserManager userFactory;
  	Map users = null;
	HashMap usersHash = new HashMap();
	String curUserName = null;
	
	try
    	{
		UserFactory.init();
		userFactory = UserFactory.getInstance();
      		users = userFactory.getUsers();
	}
	catch(Exception e)
	{
		throw new ServletException("User:list " + e.getMessage());
	}

	Iterator i = users.keySet().iterator();
	while (i.hasNext()) {
		User curUser = (User)users.get(i.next());
		usersHash.put(curUser.getUserId(), curUser.getFullName());
	}

%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="headTitle" value="View" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and Roles</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/roles'>Role List</a>" />
	<jsp:param name="breadcrumb" value="View Role" />
</jsp:include>


<!--  swiped this and images/new.gif from webcalendar.sf.net -->
<style type="text/css">

.new {
  border-width: 0px;
  float: right;
}

.date {
  border-width: 0px;
  float: left;
}

</style>

<script type="text/javascript" >

	function changeDisplay() {
		document.displayForm.submit();
	}
	
	function prevMonth() {
		document.prevMonthForm.submit();
	}
	
	function nextMonth() {
		document.nextMonthForm.submit();
	}
	
	function addEntry(date) {
		document.addEntryForm.date.value = date;
		document.addEntryForm.submit();
		
	}
	
	function editEntry(schedIndex, timeIndex) {
		document.editEntryForm.schedIndex.value = schedIndex;
		document.editEntryForm.timeIndex.value = timeIndex;
		document.editEntryForm.submit();
	}

</script>

<h3>View Role</h3>

<table>
  <tr>
    <th>Name</th>
	<td>${role.name}</td>
    <th>Currently On Call</th>
	<td>
	  <c:forEach var="scheduledUser" items="${role.currentUsers}">
		<c:set var="fullName"><%= usersHash.get(pageContext.getAttribute("scheduledUser").toString()) %></c:set>
		<span title="${fullName}">${scheduledUser}</span>
	  </c:forEach>	
	</td>
  </tr>
  
  <tr>
    <th>Supervisor</th>
	<td>
	  <c:set var="supervisorUser">${role.defaultUser}</c:set>
	  <c:set var="fullName"><%= usersHash.get(pageContext.getAttribute("supervisorUser").toString()) %></c:set>
	  <span title="${fullName}">${role.defaultUser}</span></td>
    <th>Membership Group</th>
	<td>${role.membershipGroup}</td>
  </tr>
  
  <tr>
    <th>Description</th>
	<td colspan="3">${role.description}</td>
  </tr>
</table>


		<form action="<c:url value='${reqUrl}'/>" method="post" name="editForm">
			<input type="hidden" name="operation" value="editDetails"/>
			<input type="hidden" name="role" value="${role.name}"/>
			<input type="submit" value="Edit Details" />
		</form>

		<form action="<c:url value='${reqUrl}'/>" method="post" name="doneForm">
			<input type="submit" value="Done" />
		</form>

<h3>Role Schedule</h3>


				<form action="<c:url value='${reqUrl}'/>" method="post" name="prevMonthForm">
					<input type="hidden" name="operation" value="view"/>
					<input type="hidden" name="role" value="${role.name}"/>
					<input type="hidden" name="month" value="<fmt:formatDate value='${calendar.previousMonth}' type='date' pattern='MM-yyyy'/>"/>
				</form>
				<form action="<c:url value='${reqUrl}'/>" method="post" name="nextMonthForm">
					<input type="hidden" name="operation" value="view"/>
					<input type="hidden" name="role" value="${role.name}"/>
					<input type="hidden" name="month" value="<fmt:formatDate value='${calendar.nextMonth}' type='date' pattern='MM-yyyy'/>"/>
				</form>
				<form action="<c:url value='${reqUrl}'/>" method="post" name="addEntryForm">
					<input type="hidden" name="operation" value="addEntry"/>
					<input type="hidden" name="role" value="${role.name}"/>
					<input type="hidden" name="date"/>
				</form>
				<form action="<c:url value='${reqUrl}'/>" method="post" name="editEntryForm">
					<input type="hidden" name="operation" value="editEntry"/>
					<input type="hidden" name="role" value="${role.name}"/>
					<input type="hidden" name="schedIndex"/>
					<input type="hidden" name="timeIndex"/>
				</form>

			<table>
			  <caption>
				<a href="javascript:prevMonth()">&#139;&#139;&#139;</a>&nbsp;
				<b>${calendar.monthAndYear}</b>&nbsp;
				<a href="javascript:nextMonth()">&#155;&#155;&#155;</a>
			  </caption>
				<tr>
				  <c:forEach var="day" items="${calendar.weeks[0].days}">
				    <th>${day.dayOfWeek}</th>
				  </c:forEach>
				</tr>
				<c:forEach var="week" items="${calendar.weeks}">
				  <tr>
					<c:forEach var="day" items="${week.days}">
					  <td>
					    <c:if test="${calendar.month == day.month}">
						  <c:set var="newHref">javascript:addEntry('<fmt:formatDate value='${day.date}' type='date' pattern='MM-dd-yyyy'/>')</c:set>
						  <b class="date"><c:out value="${day.dayOfMonth}"/></b><a class="new" href="<c:out value='${newHref}' escapeXml='false'/>"><img border=0 src="images/new.gif"/></a>
						  <br/>
						  <c:forEach var="entry" items="${day.entries}">
							<fmt:formatDate value="${entry.startTime}" type="time" pattern="HH:mm"/>:<c:forEach var="owner" items="${entry.labels}"><c:set var="curUserName"><c:out value="${owner.user}"/></c:set><c:set var="fullName"><%= usersHash.get((String)pageContext.getAttribute("curUserName")) %></c:set><c:set var="editHref">javascript:editEntry(<c:out value="${owner.schedIndex}"/>,<c:out value="${owner.timeIndex}"/>)</c:set>&nbsp;<c:choose><c:when test="${owner.supervisor}">unscheduled</c:when><c:otherwise><a href="<c:out value='${editHref}' escapeXml='false'/>" title="<c:out value='${fullName}'/>"><c:out value="${owner.user}"/></a></c:otherwise></c:choose></c:forEach><br/>
						  </c:forEach>
					    </c:if>
					  </td>
					</c:forEach>
				  </tr>
				</c:forEach>
			</table>

		<form action="<c:url value='${reqUrl}'/>" method="post" name="doneForm">
			<input type="submit" value="Done" />
		</form>


<jsp:include page="/includes/footer.jsp" flush="false" />

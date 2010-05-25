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
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Role Configuration" />
	<jsp:param name="headTitle" value="View" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="breadcrumb" value="<a href='roles'>Role List</a>" />
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

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
	         <tr>
    		    		<td bgcolor="#999999"><b>Name</b></td>
				<td><c:out value="${role.name}"/></td>
    		    		<td bgcolor="#999999"><b>Currently On Call</b></td>
				<td>
					<c:forEach var="scheduledUser" items="${role.currentUsers}">
						<c:out value="${scheduledUser}"/>
					</c:forEach>	
				</td>
          	</tr>
	         <tr>
    		    		<td bgcolor="#999999"><b>Supervisor</b></td>
				<td><c:out value="${role.defaultUser}"/></td>
    		    		<td bgcolor="#999999"><b>Membership Group</b></td>
				<td><c:out value="${role.membershipGroup}"/></td>
          	</tr>
          	<tr>
    		    		<td bgcolor="#999999"><b>Description</b></td>
				<td colspan="3"><c:out value="${role.description}"/></td>
          	</tr>
		</table>


<h3>Role Schedule</h3>
  <table>
	<tr>
		<td>&nbsp;
				<form action="<c:url value='${reqUrl}'/>" method="post" name="prevMonthForm">
					<input type="hidden" name="operation" value="view"/>
					<input type="hidden" name="role" value="<c:out value='${role.name}'/>"/>
					<input type="hidden" name="month" value="<fmt:formatDate value='${calendar.previousMonth}' type='date' pattern='MM-yyyy'/>"/>
				</form>
				<form action="<c:url value='${reqUrl}'/>" method="post" name="nextMonthForm">
					<input type="hidden" name="operation" value="view"/>
					<input type="hidden" name="role" value="<c:out value='${role.name}'/>"/>
					<input type="hidden" name="month" value="<fmt:formatDate value='${calendar.nextMonth}' type='date' pattern='MM-yyyy'/>"/>
				</form>
			</td>
		<td colspan="4">
			<table  border="1" cellspacing="0" cellpadding="2" bordercolor="black">
			<caption>
				<a href="javascript:prevMonth()">&#139;&#139;&#139;</a>&nbsp;
				<B><c:out value="${calendar.monthAndYear}"/></B>&nbsp;
				<a href="javascript:nextMonth()">&#155;&#155;&#155;</a>
			</caption>
				<tr>
				<c:forEach var="day" items="${calendar.weeks[0].days}">
				<th bgcolor="#999999">
					<b><c:out value="${day.dayOfWeek}"/></b>
				</th>
				</c:forEach>
				</tr>
				<c:forEach var="week" items="${calendar.weeks}">
				<tr>
					<c:forEach var="day" items="${week.days}">
					<td>
					<c:if test="${calendar.month == day.month}">
						<b class="date"><c:out value="${day.dayOfMonth}"/></b>
						<c:forEach var="entry" items="${day.entries}">
							<fmt:formatDate value="${entry.startTime}" type="time" pattern="h:mm'&nbsp;'a"/>:<c:forEach var="owner" items="${entry.labels}">&nbsp;<c:choose><c:when test="${owner.supervisor}">unscheduled</c:when><c:otherwise><c:out value="${owner.user}"/></c:otherwise></c:choose></c:forEach><br/>
						</c:forEach>
					</c:if>
					</td>
					</c:forEach>
				</tr>
				</c:forEach>
			</table>
		</td>				
	</tr>
	<tr align="right">
		<td>&nbsp;</td>
		<td>
		<table border="0">
		<tr>
		<td>
		<form action="<c:url value='${reqUrl}'/>" method="post" name="doneForm">
			<input type="submit" value="Done" />
		</form>
		</td>
		</tr>
		</table>

<jsp:include page="/includes/footer.jsp" flush="false" />

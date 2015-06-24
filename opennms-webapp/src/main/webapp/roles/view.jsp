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
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="" />
	<jsp:param name="headTitle" value="${role.name}" />
	<jsp:param name="headTitle" value="Roles" />
	<jsp:param name="breadcrumb" value="<a href='roles'>Role List</a>" />
	<jsp:param name="breadcrumb" value="${role.name}" />
</jsp:include>


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

<div class="panel panel-default">
	<div class="panel-heading">
		<h3 class="panel-title">
			<c:out value="${role.name}" />
		</h3>
	</div>

	<table class="table table-condensed severity">
		<tr>
			<th class="col-md-1">Name</th>
			<td class="col-md-5"><c:out value="${role.name}" /></td>
			<th class="col-md-1">Currently&nbsp;On&nbsp;Call</th>
			<td class="col-md-5">
			<c:forEach var="scheduledUser" items="${role.currentUsers}">
				<c:out value="${scheduledUser}" />
			</c:forEach></td>
		</tr>
		<tr>
			<th>Supervisor</th>
			<td><c:out value="${role.defaultUser}" /></td>
			<th>Membership&nbsp;Group</th>
			<td><c:out value="${role.membershipGroup}" /></td>
		</tr>
		<tr>
			<th>Description</th>
			<td colspan="3"><c:out value="${role.description}" /></td>
		</tr>
	</table>
</div>

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

<div class="panel panel-default">
	<div class="panel-heading">
		<h3 class="panel-title">Role Schedule</h3>
	</div>

	<table class="table table-condensed table-bordered severity">
			<caption class="text-center">
				<button class="btn btn-default" onclick="prevMonth()">&laquo;</button>
				&nbsp;
				<strong><c:out value="${calendar.monthAndYear}"/></strong>
				&nbsp;
				<button class="btn btn-default" onclick="nextMonth()">&raquo;</button>
			</caption>
				<tr>
				<c:forEach var="day" items="${calendar.weeks[0].days}">
				<th>
					<b><c:out value="${day.dayOfWeek}"/></b>
				</th>
				</c:forEach>
				</tr>
				<c:forEach var="week" items="${calendar.weeks}">
				<tr>
					<c:forEach var="day" items="${week.days}">
					<td>
					<c:if test="${calendar.month == day.month}">
						<p><strong><c:out value="${day.dayOfMonth}"/></strong></p>
						<c:forEach var="entry" items="${day.entries}">
							<fmt:formatDate value="${entry.startTime}" type="time" pattern="hh:mm'&nbsp;'a"/>:
							<c:forEach var="owner" items="${entry.labels}">&nbsp;
								<c:choose>
									<c:when test="${owner.supervisor}">
										<div class="label label-default">Unscheduled</div><br/>
									</c:when>
									<c:otherwise>
										<div class="label label-primary"><c:out value="${owner.user}"/></div><br/>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</c:forEach>
					</c:if>
					</td>
					</c:forEach>
				</tr>
				</c:forEach>
	</table>
</div>

<form class="form-inline" action="<c:url value='${reqUrl}'/>" method="post" name="doneForm">
	<input class="form-control" type="submit" value="Done" />
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

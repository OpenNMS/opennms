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
	import="java.util.*,
		java.text.DecimalFormat,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.alarm.*,
		org.opennms.netmgt.model.OnmsSeverity
		"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/form" prefix="form" %>

<%
    //get the service names, in alpha order
    Map<String,Integer> serviceNameMap = new TreeMap<String,Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    Set<String> serviceNameSet = serviceNameMap.keySet();
%>

<jsp:useBean id="now" class="java.util.Date" />
<c:set var="months" value="Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec" />
<fmt:formatDate var="nowYear" value="${now}" pattern="yyyy" />
<fmt:formatDate var="nowMonth" value="${now}" pattern="M" />
<fmt:formatDate var="nowDate" value="${now}" pattern="d" />
<fmt:formatDate var="nowHour" value="${now}" pattern="h" />
<fmt:formatDate var="nowMinute" value="${now}" pattern="m" />
<fmt:formatDate var="formattedNowMinute" value="${now}" pattern="mm" />
<fmt:formatDate var="nowAmPm" value="${now}" pattern="a" />
<c:set var="amPmText">
  <c:choose>
    <c:when test="${nowAmPm == 'AM' && nowHour == 12}">Midnight</c:when>
    <c:when test="${nowAmPm == 'AM' && nowHour != 12}">AM</c:when>
    <c:when test="${nowAmPm == 'PM' && nowHour == 12}">Noon</c:when>
    <c:when test="${nowAmPm == 'PM' && nowHour != 12}">PM</c:when>
  </c:choose>
</c:set>

<form action="alarm/query" method="get">

	<div class="form-group col-sm-6">
		<label for="msgsub">Alarm Text Contains:</label>
		<input class="form-control" type="text" name="msgsub" />
	</div>

	<div class="form-group col-sm-6">
		<label for="iplike">TCP/IP Address Like:</label>
		<input class="form-control" type="text" name="iplike" value="" />
	</div>

	<div class="form-group col-sm-6">
		<label for="nodenamelike">Node Label Contains:</label>
		<input class="form-control" type="text" name="nodenamelike" />
	</div>

	<div class="form-group col-sm-6">
		<label for="severity">Severity:</label>
		<select class="form-control" name="severity">
			<option selected="selected"><%=AlarmUtil.ANY_SEVERITIES_OPTION%></option>

			<% for (OnmsSeverity severity : OnmsSeverity.values()) { %>
			<option value="<%=severity.getId()%>">
				<%=severity.getLabel()%>
			</option>
			<% } %>
		</select>
	</div>

	<!-- Use clear:left to make sure that this column breaks onto a new row -->
	<div class="form-group col-sm-4" style="clear: left;">
		<label for="service">Service:</label>
		<select class="form-control" name="service">
			<option selected><%=AlarmUtil.ANY_SERVICES_OPTION%></option>

			<% for (String name : serviceNameSet) { %>
			<option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
			<% } %>
		</select>
	</div>

	<div class="form-group col-sm-4">
		<label for="sortby">Sort By:</label>
		<select class="form-control" name="sortby">
			<option value="id">Alarm ID (Descending)</option>
			<option value="rev_id">Alarm ID (Ascending)</option>
			<option value="severity">Severity (Descending)</option>
			<option value="rev_severity">Severity (Ascending)</option>
			<option value="lasteventtime">Time (Descending)</option>
			<option value="rev_lasteventtime">Time (Ascending)</option>
			<option value="node">Node (Ascending)</option>
			<option value="rev_node">Node (Descending)</option>
			<option value="interface">Interface (Ascending)</option>
			<option value="rev_interface">Interface (Descending)</option>
			<option value="service">Service (Ascending)</option>
			<option value="rev_service">Service (Descending)</option>
		</select>
	</div>

	<div class="form-group col-sm-4">
		<label for="limit">Number of Alarms Per Page:</label>
		<select class="form-control" name="limit">
			<option value="10">10 alarms</option>
			<option value="20">20 alarms</option>
			<option value="30">30 alarms</option>
			<option value="50">50 alarms</option>
			<option value="100">100 alarms</option>
			<option value="-1">All alarms</option>
		</select>
	</div>

	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="useafterfirsteventtime" value="1" /> Alarm's First Event After:
		</label>
		<!-- 
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br />
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="afterfirsteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterfirsteventtimeminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-sm-2">
				<select class="form-control" name="afterfirsteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="afterfirsteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterfirsteventtimedate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterfirsteventtimeyear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>

	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="usebeforefirsteventtime" value="1" /> Alarm's First Event Before:
		</label>
		<!-- 
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br />
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="beforefirsteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforefirsteventtimeminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-sm-2">
				<select class="form-control" name="beforefirsteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="beforefirsteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforefirsteventtimedate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforefirsteventtimeyear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>

	<div class="col-sm-12">
		<br />
	</div>

	<div class="col-sm-6">
		<label> <input type="checkbox" name="useafterlasteventtime"
			value="1" /> Alarm's Last Event After:
		</label>
		<!-- 
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br />
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="afterlasteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterlasteventtimeminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-sm-2">
				<select class="form-control" name="afterlasteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="afterlasteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterlasteventtimedate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="afterlasteventtimeyear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>

	<div class="col-sm-6">
		<label> <input type="checkbox" name="usebeforelasteventtime"
			value="1" /> Alarm Last Event Before:
		</label>
		<!-- 
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br />
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="beforelasteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforelasteventtimeminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-sm-2">
				<select class="form-control" name="beforelasteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-2">
				<select class="form-control" name="beforelasteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforelasteventtimedate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-sm-2">
				<input class="form-control" type="text" name="beforelasteventtimeyear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>

	<div class="col-sm-12">
		<br />
	</div>

	<div class="form-group col-sm-12">
		<button class="btn btn-default" type="submit">Search</button>
	</div>
</form>

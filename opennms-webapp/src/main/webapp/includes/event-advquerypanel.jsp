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

<%@page language="java" contentType="text/html" session="true"
  import="java.util.*,
    org.opennms.web.element.NetworkElementFactory,
    org.opennms.netmgt.model.OnmsSeverity
  "
%>
<%@ page import="org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation" %>
<%@ page import="org.opennms.netmgt.model.OnmsMonitoringSystem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/form" prefix="form" %>
<%
    //get the service names, in alpha order
    Map<String, Integer> serviceNameMap = new TreeMap<String, Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    Set<String> serviceNameSet = serviceNameMap.keySet();

	List<OnmsMonitoringLocation> monitoringLocations = NetworkElementFactory.getInstance(getServletContext()).getMonitoringLocations();
	List<OnmsMonitoringSystem> monitoringSystems = NetworkElementFactory.getInstance(getServletContext()).getMonitoringSystems();
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

<form action="event/query" method="get">

	<div class="row">
	<div class="form-group col-sm-6">
		<label for="msgsub">Event ID:</label>
		<input class="form-control" type="text" name="eventid" />
	</div>
	<div class="form-group col-sm-6">
		<label for="nodenamelike">Node Label Contains:</label>
		<input class="form-control" type="text" name="nodenamelike" />
	</div>
	</div>

	<div class="row">
	<div class="form-group col-sm-6">
		<label for="msgsub">Event Text Contains:</label>
		<input class="form-control" type="text" name="msgsub" />
	</div>

	<div class="form-group col-sm-6">
		<label for="iplike">TCP/IP Address Like:</label>
		<input class="form-control" type="text" name="iplike" value="" />
	</div>
	</div>

	<div class="row">
		<div class="form-group col-sm-6">
			<label for="nodelocation">Node Location:</label>
			<select class="form-control" name="nodelocation">
				<option selected="selected">Any</option>
				<% for (OnmsMonitoringLocation onmsMonitoringLocation : monitoringLocations ) { %>
				<option value="<%= onmsMonitoringLocation.getLocationName() %>">
					<%= onmsMonitoringLocation.getLocationName() %>
				</option>
				<% } %>
			</select>
		</div>
		<div class="form-group col-sm-6">
			<label for="systemId">System-ID:</label>
			<select class="form-control" name="systemId">
				<option selected>Any</option>
				<% for (OnmsMonitoringSystem onmsMonitoringSystem : monitoringSystems ) { %>
				<option value="<%= onmsMonitoringSystem.getId() %>">
					<%= onmsMonitoringSystem.getId() %>
				</option>
				<% } %>
			</select>
		</div>
	</div>

	<div class="row">
	<div class="form-group col-sm-6">
		<label for="severity">Severity:</label>
		<select class="form-control" name="severity">
			<option selected="selected">Any</option>
			<% for (OnmsSeverity severity : OnmsSeverity.values() ) { %>
			<option value="<%= severity.getId() %>">
				<%= severity.getLabel() %>
			</option>
			<% } %>
		</select>
	</div>
	<div class="form-group col-sm-6">
		<label for="service">Service:</label>
		<select class="form-control" name="service">
			<option selected>Any</option>
			<% for (String name : serviceNameSet) { %>
				<option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
			<% } %>
		</select>
	</div>
	</div>

	<div class="row">
	<div class="form-group col-sm-12">
		<label for="exactuei">Exact Event UEI:</label>
		<input class="form-control" type="text" name="exactuei" size="64" maxsize="128" />
	</div>
	</div>

	<div class="row">
		<div class="form-group col-sm-4">
			<label for="relativetime">Relative Time:</label> <select class="form-control"
				name="relativetime">
				<option value="0" selected>Any</option>
				<option value="1">Last hour</option>
				<option value="2">Last 4 hours</option>
				<option value="3">Last 8 hours</option>
				<option value="4">Last 12 hours</option>
				<option value="5">Last day</option>
				<option value="6">Last week</option>
				<option value="7">Last month</option>
			</select>
		</div>
		<div class="form-group col-sm-4">
			<label class="control-label" for="sortby">Sort By:</label> <select
				class="form-control" name="sortby">
				<option value="id">Event ID (Descending)</option>
				<option value="rev_id">Event ID (Ascending)</option>
				<option value="severity">Severity (Descending)</option>
				<option value="rev_severity">Severity (Ascending)</option>
				<option value="time">Time (Descending)</option>
				<option value="rev_time">Time (Ascending)</option>
				<option value="node">Node (Ascending)</option>
				<option value="rev_node">Node (Descending)</option>
				<option value="interface">Interface (Ascending)</option>
				<option value="rev_interface">Interface (Descending)</option>
				<option value="service">Service (Ascending)</option>
				<option value="rev_service">Service (Descending)</option>
			</select>
		</div>

		<div class="form-group col-sm-4">
			<label for="limit">Number&nbsp;of&nbsp;Events&nbsp;Per&nbsp;Page:</label>
			<select class="form-control" name="limit">
				<option value="10">10 events</option>
				<option value="20">20 events</option>
				<option value="30">30 events</option>
				<option value="50">50 events</option>
				<option value="100">100 events</option>
				<option value="1000">1000 events</option>
			</select>
		</div>
	</div>

	<div class="row">
	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="useaftertime" value="1" /> Events After:
		</label>
		<!-- 
		<input type="date" name="afterdate"/>
		<input type="time" name="aftertime"/>
		-->
		<br/>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="afterhour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="afterampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="aftermonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterdate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afteryear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>

	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="usebeforetime" value="1" /> Events Before:
		</label>
		<!-- 
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br/>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="beforehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforeminute" maxlength="2" value="${formattedNowMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="beforeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control" name="beforemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}"
							selected="${status.count == nowMonth}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforedate" maxlength="2" value="${nowDate}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforeyear" maxlength="4" value="${nowYear}" />
			</div>
		</div>
	</div>
	</div>

	<br/>

	<button class="btn btn-default" type="submit">Search</button>

</form>

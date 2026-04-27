<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java" contentType="text/html" session="true"
  import="java.util.*,
    org.opennms.web.element.NetworkElementFactory,
    org.opennms.netmgt.model.OnmsSeverity
  "
%>
<%@ page import="org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation" %>
<%@ page import="org.opennms.netmgt.model.OnmsMonitoringSystem" %>
<%@ page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@ page import="org.opennms.web.filter.Filter" %>
<%@ page import="org.opennms.web.event.filter.SeverityOrFilter" %>
<%@ page import="org.opennms.web.event.filter.SeverityFilter" %>
<%@ page import="org.opennms.web.event.filter.ServiceOrFilter" %>
<%@ page import="org.opennms.web.event.filter.ServiceFilter" %>
<%@ page import="org.opennms.web.event.filter.NodeNameLikeFilter" %>
<%@ page import="org.opennms.web.event.filter.EventTextFilter" %>
<%@ page import="org.opennms.web.event.filter.IPAddrLikeFilter" %>
<%@ page import="org.opennms.web.event.filter.EventIdFilter" %>
<%@ page import="org.opennms.web.event.filter.ExactUEIFilter" %>
<%@ page import="org.opennms.web.event.filter.NodeLocationFilter" %>
<%@ page import="org.opennms.web.event.filter.SystemIdFilter" %>
<%@ page import="org.opennms.web.event.filter.AfterDateFilter" %>
<%@ page import="org.opennms.web.event.filter.BeforeDateFilter" %>
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

<%
    // Build pre-population state from current filters in parms
    NormalizedQueryParameters advParms = (NormalizedQueryParameters) request.getAttribute("parms");

    Set<Integer> checkedSeverities = new HashSet<>();
    Set<Integer> checkedServices = new HashSet<>();
    String prefillEventId = "";
    String prefillNodeName = "";
    String prefillEventText = "";
    String prefillIpLike = "";
    String prefillExactUei = "";
    String prefillNodeLocation = "";
    String prefillSystemId = "";
    boolean useAfterTime = false;
    boolean useBeforeTime = false;

    // Date field defaults from JSTL-computed "now" values
    int afterHourVal = Integer.parseInt((String) pageContext.getAttribute("nowHour"));
    String afterFormattedMinute = (String) pageContext.getAttribute("formattedNowMinute");
    int afterMonthCount = Integer.parseInt((String) pageContext.getAttribute("nowMonth"));
    int afterDayVal = Integer.parseInt((String) pageContext.getAttribute("nowDate"));
    int afterYearVal = Integer.parseInt((String) pageContext.getAttribute("nowYear"));
    String rawAmPmText = (String) pageContext.getAttribute("amPmText");
    String afterAmPmText = rawAmPmText != null ? rawAmPmText.trim() : "AM";

    int beforeHourVal = afterHourVal;
    String beforeFormattedMinute = afterFormattedMinute;
    int beforeMonthCount = afterMonthCount;
    int beforeDayVal = afterDayVal;
    int beforeYearVal = afterYearVal;
    String beforeAmPmText = afterAmPmText;

    String prefillSortStyle = "id";
    int prefillLimit = 20;

    if (advParms != null) {
        String sortStyle = advParms.getSortStyleShortName();
        if (sortStyle != null && !sortStyle.isEmpty()) {
            prefillSortStyle = sortStyle;
        }
        if (advParms.getLimit() > 0) {
            prefillLimit = advParms.getLimit();
        }

        for (Filter filter : advParms.getFilters()) {
            if (filter instanceof SeverityOrFilter) {
                for (Filter sf : ((SeverityOrFilter) filter).getFilters()) {
                    checkedSeverities.add(((SeverityFilter) sf).getSeverity());
                }
            } else if (filter instanceof ServiceOrFilter) {
                for (Filter sf : ((ServiceOrFilter) filter).getFilters()) {
                    checkedServices.add(((ServiceFilter) sf).getServiceId());
                }
            } else if (filter instanceof NodeNameLikeFilter) {
                prefillNodeName = ((NodeNameLikeFilter) filter).getValue();
            } else if (filter instanceof EventTextFilter) {
                prefillEventText = ((EventTextFilter) filter).getValue();
            } else if (filter instanceof IPAddrLikeFilter) {
                prefillIpLike = ((IPAddrLikeFilter) filter).getValue();
            } else if (filter instanceof EventIdFilter) {
                prefillEventId = String.valueOf(((EventIdFilter) filter).getValue());
            } else if (filter instanceof ExactUEIFilter) {
                prefillExactUei = ((ExactUEIFilter) filter).getUEI();
            } else if (filter instanceof NodeLocationFilter) {
                prefillNodeLocation = ((NodeLocationFilter) filter).getValue();
            } else if (filter instanceof SystemIdFilter) {
                prefillSystemId = ((SystemIdFilter) filter).getValue();
            } else if (filter instanceof AfterDateFilter) {
                useAfterTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((AfterDateFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                afterHourVal = (calHour == 0) ? 12 : calHour;
                afterFormattedMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                afterMonthCount = cal.get(Calendar.MONTH) + 1;
                afterDayVal = cal.get(Calendar.DATE);
                afterYearVal = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && afterHourVal == 12) afterAmPmText = "Midnight";
                else if (isAm) afterAmPmText = "AM";
                else if (!isAm && afterHourVal == 12) afterAmPmText = "Noon";
                else afterAmPmText = "PM";
            } else if (filter instanceof BeforeDateFilter) {
                useBeforeTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((BeforeDateFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                beforeHourVal = (calHour == 0) ? 12 : calHour;
                beforeFormattedMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                beforeMonthCount = cal.get(Calendar.MONTH) + 1;
                beforeDayVal = cal.get(Calendar.DATE);
                beforeYearVal = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && beforeHourVal == 12) beforeAmPmText = "Midnight";
                else if (isAm) beforeAmPmText = "AM";
                else if (!isAm && beforeHourVal == 12) beforeAmPmText = "Noon";
                else beforeAmPmText = "PM";
            }
        }
    }

    // Expose to EL for use in selects
    pageContext.setAttribute("prefillSortStyle", prefillSortStyle);
    pageContext.setAttribute("prefillLimit", prefillLimit);
    pageContext.setAttribute("afterHourVal", afterHourVal);
    pageContext.setAttribute("afterFormattedMinute", afterFormattedMinute);
    pageContext.setAttribute("afterMonthCount", afterMonthCount);
    pageContext.setAttribute("afterDayVal", afterDayVal);
    pageContext.setAttribute("afterYearVal", afterYearVal);
    pageContext.setAttribute("afterAmPmText", afterAmPmText);
    pageContext.setAttribute("beforeHourVal", beforeHourVal);
    pageContext.setAttribute("beforeFormattedMinute", beforeFormattedMinute);
    pageContext.setAttribute("beforeMonthCount", beforeMonthCount);
    pageContext.setAttribute("beforeDayVal", beforeDayVal);
    pageContext.setAttribute("beforeYearVal", beforeYearVal);
    pageContext.setAttribute("beforeAmPmText", beforeAmPmText);
%>

<script type="text/javascript">
function resetAdvancedSearch() {
    var form = document.querySelector('#advancedSearchModal form');

    // Clear text inputs
    ['eventid', 'nodenamelike', 'eventtext', 'iplike', 'exactuei'].forEach(function(name) {
        form.querySelector('[name="' + name + '"]').value = '';
    });

    // Uncheck all severity and service checkboxes
    form.querySelectorAll('[name^="severity-"], [name^="service-"]').forEach(function(cb) {
        cb.checked = false;
    });

    // Reset location/system dropdowns to "Any"
    form.querySelector('[name="nodelocation"]').selectedIndex = 0;
    form.querySelector('[name="systemId"]').selectedIndex = 0;

    // Reset other dropdowns to defaults
    form.querySelector('[name="relativetime"]').value = '0';
    form.querySelector('[name="sortby"]').value = 'id';
    form.querySelector('[name="limit"]').value = '20';

    // Uncheck time filter checkboxes
    form.querySelector('[name="useaftertime"]').checked = false;
    form.querySelector('[name="usebeforetime"]').checked = false;

    // Reset date/time fields to current server time
    var nowHour = '${nowHour}';
    var nowMin = '${formattedNowMinute}';
    var nowAmpm = '${nowAmPm}' === 'AM' ? 'am' : 'pm';
    var nowMonthIdx = parseInt('${nowMonth}') - 1;
    var nowDay = '${nowDate}';
    var nowYear = '${nowYear}';

    ['after', 'before'].forEach(function(prefix) {
        form.querySelector('[name="' + prefix + 'hour"]').value = nowHour;
        form.querySelector('[name="' + prefix + 'minute"]').value = nowMin;
        form.querySelector('[name="' + prefix + 'ampm"]').value = nowAmpm;
        form.querySelector('[name="' + prefix + 'month"]').value = nowMonthIdx;
        form.querySelector('[name="' + prefix + 'date"]').value = nowDay;
        form.querySelector('[name="' + prefix + 'year"]').value = nowYear;
    });
}
</script>

<form action="event/query" method="post">
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

	<div class="row">
	<div class="form-group col-sm-6">
		<label for="eventid">Event ID</label>
		<input class="form-control" type="text" name="eventid" value="<%= prefillEventId %>" />
	</div>
	<div class="form-group col-sm-6">
		<label for="nodenamelike">Node Label Contains</label>
		<input class="form-control" type="text" name="nodenamelike" value="<%= prefillNodeName %>" />
	</div>
	</div>

	<div class="row">
	<div class="form-group col-sm-6">
		<label for="eventtext">Event Text Contains</label>
		<input class="form-control" type="text" name="eventtext" value="<%= prefillEventText %>" />
	</div>

	<div class="form-group col-sm-6">
		<label for="iplike">TCP/IP Address Like</label>
		<input class="form-control" type="text" name="iplike" value="<%= prefillIpLike %>" />
	</div>
	</div>

	<div class="row">
		<div class="form-group col-sm-6">
			<label for="nodelocation">Node Location</label>
			<select class="form-control custom-select" name="nodelocation">
				<option <%= prefillNodeLocation.isEmpty() ? "selected=\"selected\"" : "" %>>Any</option>
				<% for (OnmsMonitoringLocation onmsMonitoringLocation : monitoringLocations ) { %>
				<option value="<%= onmsMonitoringLocation.getLocationName() %>"
				        <%= onmsMonitoringLocation.getLocationName().equals(prefillNodeLocation) ? "selected=\"selected\"" : "" %>>
					<%= onmsMonitoringLocation.getLocationName() %>
				</option>
				<% } %>
			</select>
		</div>

		<div class="form-group col-sm-6">
			<label for="systemId">System-ID</label>
			<select class="form-control custom-select" name="systemId">
				<option <%= prefillSystemId.isEmpty() ? "selected" : "" %>>Any</option>
				<% for (OnmsMonitoringSystem onmsMonitoringSystem : monitoringSystems ) { %>
				<option value="<%= onmsMonitoringSystem.getId() %>"
				        <%= onmsMonitoringSystem.getId().equals(prefillSystemId) ? "selected" : "" %>>
					<%= onmsMonitoringSystem.getId() %>
				</option>
				<% } %>
			</select>
		</div>
	</div>

	<div class="row">
        <div class="form-group col-sm-6">
            <label for="severity">Severity</label>
            <% for (OnmsSeverity severity : OnmsSeverity.values()) { %>
                <div>
                    <label>
                        <input type="checkbox" name="severity-<%=severity.getId()%>" value="1"
                               <%=checkedSeverities.contains(severity.getId()) ? "checked" : ""%> /> <%=severity.getLabel()%>
                    </label>
                </div>
            <% } %>
        </div>

        <div class="form-group col-sm-6">
            <label for="service">Service</label>
            <% for (String name : serviceNameSet) { %>
                <div>
                    <label>
                        <input type="checkbox" name="service-<%=serviceNameMap.get(name)%>" value="1"
                               <%=checkedServices.contains(serviceNameMap.get(name)) ? "checked" : ""%> /> <%=name%>
                    </label>
                </div>
            <% } %>
        </div>
	</div>

	<div class="row">
	<div class="form-group col-sm-12">
		<label for="exactuei">Exact Event UEI</label>
		<input class="form-control" type="text" name="exactuei" size="64" maxsize="128" value="<%= prefillExactUei %>" />
	</div>
	</div>

	<div class="row">
		<div class="form-group col-sm-4">
			<label for="relativetime">Relative Time</label> <select class="form-control custom-select"
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
			<label class="col-form-label" for="sortby">Sort By</label>
			<select class="form-control custom-select" name="sortby">
				<option value="id" ${prefillSortStyle == 'id' ? 'selected' : ''}>Event ID (Descending)</option>
				<option value="rev_id" ${prefillSortStyle == 'rev_id' ? 'selected' : ''}>Event ID (Ascending)</option>
				<option value="severity" ${prefillSortStyle == 'severity' ? 'selected' : ''}>Severity (Descending)</option>
				<option value="rev_severity" ${prefillSortStyle == 'rev_severity' ? 'selected' : ''}>Severity (Ascending)</option>
				<option value="time" ${prefillSortStyle == 'time' ? 'selected' : ''}>Time (Descending)</option>
				<option value="rev_time" ${prefillSortStyle == 'rev_time' ? 'selected' : ''}>Time (Ascending)</option>
				<option value="node" ${prefillSortStyle == 'node' ? 'selected' : ''}>Node (Ascending)</option>
				<option value="rev_node" ${prefillSortStyle == 'rev_node' ? 'selected' : ''}>Node (Descending)</option>
				<option value="interface" ${prefillSortStyle == 'interface' ? 'selected' : ''}>Interface (Ascending)</option>
				<option value="rev_interface" ${prefillSortStyle == 'rev_interface' ? 'selected' : ''}>Interface (Descending)</option>
				<option value="service" ${prefillSortStyle == 'service' ? 'selected' : ''}>Service (Ascending)</option>
				<option value="rev_service" ${prefillSortStyle == 'rev_service' ? 'selected' : ''}>Service (Descending)</option>
			</select>
		</div>

		<div class="form-group col-sm-4">
			<label for="limit">Number&nbsp;of&nbsp;Events&nbsp;Per&nbsp;Page</label>
			<select class="form-control custom-select" name="limit">
				<option value="10" ${prefillLimit == 10 ? 'selected' : ''}>10 events</option>
				<option value="20" ${prefillLimit == 20 ? 'selected' : ''}>20 events</option>
				<option value="30" ${prefillLimit == 30 ? 'selected' : ''}>30 events</option>
				<option value="50" ${prefillLimit == 50 ? 'selected' : ''}>50 events</option>
				<option value="100" ${prefillLimit == 100 ? 'selected' : ''}>100 events</option>
				<option value="1000" ${prefillLimit == 1000 ? 'selected' : ''}>1000 events</option>
			</select>
		</div>
	</div>

	<div class="row">
	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="useaftertime" value="1" <%= useAfterTime ? "checked" : "" %> /> Events After
		</label>
		<!--
		<input type="date" name="afterdate"/>
		<input type="time" name="aftertime"/>
		-->
		<br/>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterhour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${afterHourVal==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterminute" maxlength="2" value="${afterFormattedMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==afterAmPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="aftermonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == afterMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterdate" maxlength="2" value="${afterDayVal}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afteryear" maxlength="4" value="${afterYearVal}" />
			</div>
		</div>
	</div>

	<div class="col-sm-6">
		<label>
			<input type="checkbox" name="usebeforetime" value="1" <%= useBeforeTime ? "checked" : "" %> /> Events Before:
		</label>
		<!--
		<input type="date" name="beforedate"/>
		<input type="time" name="beforetime"/>
		-->
		<br/>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${beforeHourVal==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforeminute" maxlength="2" value="${beforeFormattedMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==beforeAmPmText}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == beforeMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforedate" maxlength="2" value="${beforeDayVal}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforeyear" maxlength="4" value="${beforeYearVal}" />
			</div>
		</div>
	</div>
	</div>

	<br/>

	<button class="btn btn-secondary" type="submit">Search</button>
	<button class="btn btn-secondary" type="button" onclick="resetAdvancedSearch()">Reset</button>

</form>

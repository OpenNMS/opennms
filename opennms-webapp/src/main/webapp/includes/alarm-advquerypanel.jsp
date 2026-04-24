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
<%@ page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@ page import="org.opennms.web.filter.Filter" %>
<%@ page import="org.opennms.web.alarm.filter.SeverityOrFilter" %>
<%@ page import="org.opennms.web.alarm.filter.SeverityFilter" %>
<%@ page import="org.opennms.web.alarm.filter.ServiceOrFilter" %>
<%@ page import="org.opennms.web.alarm.filter.ServiceFilter" %>
<%@ page import="org.opennms.web.alarm.filter.NodeNameLikeFilter" %>
<%@ page import="org.opennms.web.alarm.filter.AlarmTextFilter" %>
<%@ page import="org.opennms.web.alarm.filter.LogMessageSubstringFilter" %>
<%@ page import="org.opennms.web.alarm.filter.IPAddrLikeFilter" %>
<%@ page import="org.opennms.web.alarm.filter.SituationFilter" %>
<%@ page import="org.opennms.web.alarm.filter.CategoryFilter" %>
<%@ page import="org.opennms.web.alarm.filter.NegativeCategoryFilter" %>
<%@ page import="org.opennms.web.alarm.filter.AfterFirstEventTimeFilter" %>
<%@ page import="org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter" %>
<%@ page import="org.opennms.web.alarm.filter.AfterLastEventTimeFilter" %>
<%@ page import="org.opennms.web.alarm.filter.BeforeLastEventTimeFilter" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/form" prefix="form" %>

<%
    //get the service names, in alpha order
    Map<String,Integer> serviceNameMap = new TreeMap<String,Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
    Set<String> serviceNameSet = serviceNameMap.keySet();
    final List<String> categories = NetworkElementFactory.getInstance(getServletContext()).getCategories();
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
    String prefillAlarmText = "";
    String prefillIpLike = "";
    String prefillNodeName = "";
    String prefillSituation = "any";
    String prefillCategory = "";
    boolean prefillNestedCategoryNot = false;

    String prefillSortStyle = "id";
    int prefillLimit = 20;

    // Initialize all date/time defaults from JSTL-computed "now" values
    int nowHourInt = Integer.parseInt((String) pageContext.getAttribute("nowHour"));
    String nowFormattedMin = (String) pageContext.getAttribute("formattedNowMinute");
    int nowMonthCount = Integer.parseInt((String) pageContext.getAttribute("nowMonth"));
    int nowDayVal = Integer.parseInt((String) pageContext.getAttribute("nowDate"));
    int nowYearVal = Integer.parseInt((String) pageContext.getAttribute("nowYear"));
    String rawAmPm = (String) pageContext.getAttribute("amPmText");
    String nowAmPmStr = rawAmPm != null ? rawAmPm.trim() : "AM";

    // After First Event Time
    boolean useAfterFirstEventTime = false;
    int afterFirstHour = nowHourInt;
    String afterFirstMinute = nowFormattedMin;
    int afterFirstMonthCount = nowMonthCount;
    int afterFirstDay = nowDayVal;
    int afterFirstYear = nowYearVal;
    String afterFirstAmPm = nowAmPmStr;

    // Before First Event Time
    boolean useBeforeFirstEventTime = false;
    int beforeFirstHour = nowHourInt;
    String beforeFirstMinute = nowFormattedMin;
    int beforeFirstMonthCount = nowMonthCount;
    int beforeFirstDay = nowDayVal;
    int beforeFirstYear = nowYearVal;
    String beforeFirstAmPm = nowAmPmStr;

    // After Last Event Time
    boolean useAfterLastEventTime = false;
    int afterLastHour = nowHourInt;
    String afterLastMinute = nowFormattedMin;
    int afterLastMonthCount = nowMonthCount;
    int afterLastDay = nowDayVal;
    int afterLastYear = nowYearVal;
    String afterLastAmPm = nowAmPmStr;

    // Before Last Event Time
    boolean useBeforeLastEventTime = false;
    int beforeLastHour = nowHourInt;
    String beforeLastMinute = nowFormattedMin;
    int beforeLastMonthCount = nowMonthCount;
    int beforeLastDay = nowDayVal;
    int beforeLastYear = nowYearVal;
    String beforeLastAmPm = nowAmPmStr;

    if (advParms != null) {
        String sortStyle = advParms.getSortStyleShortName();
        if (sortStyle != null && !sortStyle.isEmpty()) {
            prefillSortStyle = sortStyle;
        }
        int limitVal = advParms.getLimit();
        if (limitVal != 0) {
            prefillLimit = limitVal;
        }

        for (Filter filter : advParms.getFilters()) {
            if (filter instanceof SeverityOrFilter) {
                for (Filter sf : ((SeverityOrFilter) filter).getFilters()) {
                    checkedSeverities.add(((SeverityFilter) sf).getSeverity());
                }
            } else if (filter instanceof SeverityFilter) {
                checkedSeverities.add(((SeverityFilter) filter).getSeverity());
            } else if (filter instanceof ServiceOrFilter) {
                for (Filter sf : ((ServiceOrFilter) filter).getFilters()) {
                    checkedServices.add(((ServiceFilter) sf).getServiceId());
                }
            } else if (filter instanceof ServiceFilter) {
                checkedServices.add(((ServiceFilter) filter).getServiceId());
            } else if (filter instanceof AlarmTextFilter) {
                for (Filter sf : ((AlarmTextFilter) filter).getFilters()) {
                    if (sf instanceof LogMessageSubstringFilter) {
                        prefillAlarmText = ((LogMessageSubstringFilter) sf).getValue();
                        break;
                    }
                }
            } else if (filter instanceof NodeNameLikeFilter) {
                prefillNodeName = ((NodeNameLikeFilter) filter).getValue();
            } else if (filter instanceof IPAddrLikeFilter) {
                prefillIpLike = ((IPAddrLikeFilter) filter).getValue();
            } else if (filter instanceof SituationFilter) {
                String d = filter.getDescription();
                prefillSituation = d.substring(d.indexOf('=') + 1);
            } else if (filter instanceof NegativeCategoryFilter) {
                prefillCategory = ((NegativeCategoryFilter) filter).getValue();
                prefillNestedCategoryNot = true;
            } else if (filter instanceof CategoryFilter) {
                prefillCategory = ((CategoryFilter) filter).getValue();
                prefillNestedCategoryNot = false;
            } else if (filter instanceof AfterFirstEventTimeFilter) {
                useAfterFirstEventTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((AfterFirstEventTimeFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                afterFirstHour = (calHour == 0) ? 12 : calHour;
                afterFirstMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                afterFirstMonthCount = cal.get(Calendar.MONTH) + 1;
                afterFirstDay = cal.get(Calendar.DATE);
                afterFirstYear = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && afterFirstHour == 12) afterFirstAmPm = "Midnight";
                else if (isAm) afterFirstAmPm = "AM";
                else if (!isAm && afterFirstHour == 12) afterFirstAmPm = "Noon";
                else afterFirstAmPm = "PM";
            } else if (filter instanceof BeforeFirstEventTimeFilter) {
                useBeforeFirstEventTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((BeforeFirstEventTimeFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                beforeFirstHour = (calHour == 0) ? 12 : calHour;
                beforeFirstMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                beforeFirstMonthCount = cal.get(Calendar.MONTH) + 1;
                beforeFirstDay = cal.get(Calendar.DATE);
                beforeFirstYear = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && beforeFirstHour == 12) beforeFirstAmPm = "Midnight";
                else if (isAm) beforeFirstAmPm = "AM";
                else if (!isAm && beforeFirstHour == 12) beforeFirstAmPm = "Noon";
                else beforeFirstAmPm = "PM";
            } else if (filter instanceof AfterLastEventTimeFilter) {
                useAfterLastEventTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((AfterLastEventTimeFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                afterLastHour = (calHour == 0) ? 12 : calHour;
                afterLastMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                afterLastMonthCount = cal.get(Calendar.MONTH) + 1;
                afterLastDay = cal.get(Calendar.DATE);
                afterLastYear = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && afterLastHour == 12) afterLastAmPm = "Midnight";
                else if (isAm) afterLastAmPm = "AM";
                else if (!isAm && afterLastHour == 12) afterLastAmPm = "Noon";
                else afterLastAmPm = "PM";
            } else if (filter instanceof BeforeLastEventTimeFilter) {
                useBeforeLastEventTime = true;
                Calendar cal = Calendar.getInstance();
                cal.setTime(((BeforeLastEventTimeFilter) filter).getDate());
                int calHour = cal.get(Calendar.HOUR);
                beforeLastHour = (calHour == 0) ? 12 : calHour;
                beforeLastMinute = String.format("%02d", cal.get(Calendar.MINUTE));
                beforeLastMonthCount = cal.get(Calendar.MONTH) + 1;
                beforeLastDay = cal.get(Calendar.DATE);
                beforeLastYear = cal.get(Calendar.YEAR);
                boolean isAm = cal.get(Calendar.AM_PM) == Calendar.AM;
                if (isAm && beforeLastHour == 12) beforeLastAmPm = "Midnight";
                else if (isAm) beforeLastAmPm = "AM";
                else if (!isAm && beforeLastHour == 12) beforeLastAmPm = "Noon";
                else beforeLastAmPm = "PM";
            }
        }
    }

    // Expose date/time values to EL for use in selects
    pageContext.setAttribute("prefillSortStyle", prefillSortStyle);
    pageContext.setAttribute("prefillLimit", prefillLimit);

    pageContext.setAttribute("afterFirstHour", afterFirstHour);
    pageContext.setAttribute("afterFirstMinute", afterFirstMinute);
    pageContext.setAttribute("afterFirstMonthCount", afterFirstMonthCount);
    pageContext.setAttribute("afterFirstDay", afterFirstDay);
    pageContext.setAttribute("afterFirstYear", afterFirstYear);
    pageContext.setAttribute("afterFirstAmPm", afterFirstAmPm);

    pageContext.setAttribute("beforeFirstHour", beforeFirstHour);
    pageContext.setAttribute("beforeFirstMinute", beforeFirstMinute);
    pageContext.setAttribute("beforeFirstMonthCount", beforeFirstMonthCount);
    pageContext.setAttribute("beforeFirstDay", beforeFirstDay);
    pageContext.setAttribute("beforeFirstYear", beforeFirstYear);
    pageContext.setAttribute("beforeFirstAmPm", beforeFirstAmPm);

    pageContext.setAttribute("afterLastHour", afterLastHour);
    pageContext.setAttribute("afterLastMinute", afterLastMinute);
    pageContext.setAttribute("afterLastMonthCount", afterLastMonthCount);
    pageContext.setAttribute("afterLastDay", afterLastDay);
    pageContext.setAttribute("afterLastYear", afterLastYear);
    pageContext.setAttribute("afterLastAmPm", afterLastAmPm);

    pageContext.setAttribute("beforeLastHour", beforeLastHour);
    pageContext.setAttribute("beforeLastMinute", beforeLastMinute);
    pageContext.setAttribute("beforeLastMonthCount", beforeLastMonthCount);
    pageContext.setAttribute("beforeLastDay", beforeLastDay);
    pageContext.setAttribute("beforeLastYear", beforeLastYear);
    pageContext.setAttribute("beforeLastAmPm", beforeLastAmPm);
%>

<%
    StringBuilder _sevJs = new StringBuilder();
    for (Integer _id : checkedSeverities) {
        if (_sevJs.length() > 0) _sevJs.append(",");
        _sevJs.append(_id);
    }
    StringBuilder _svcJs = new StringBuilder();
    for (Integer _id : checkedServices) {
        if (_svcJs.length() > 0) _svcJs.append(",");
        _svcJs.append(_id);
    }
%>
<script type="text/javascript">
(function() {
    var _state = {
        alarmtext: '<%= Encode.forJavaScript(prefillAlarmText) %>',
        iplike: '<%= Encode.forJavaScript(prefillIpLike) %>',
        nodenamelike: '<%= Encode.forJavaScript(prefillNodeName) %>',
        sortby: '<%= Encode.forJavaScript(prefillSortStyle) %>',
        limit: '<%= prefillLimit %>',
        situation: '<%= Encode.forJavaScript(prefillSituation) %>',
        category: '<%= Encode.forJavaScript(prefillCategory) %>',
        nestedCategoryNot: <%= prefillNestedCategoryNot %>,
        severities: [<%= _sevJs %>],
        services: [<%= _svcJs %>],
        useAfterFirst: <%= useAfterFirstEventTime %>,
        afterFirstHour: '<%= afterFirstHour %>',
        afterFirstMinute: '<%= afterFirstMinute %>',
        afterFirstAmPm: '<%= Encode.forJavaScript(afterFirstAmPm) %>',
        afterFirstMonthIdx: '<%= afterFirstMonthCount - 1 %>',
        afterFirstDay: '<%= afterFirstDay %>',
        afterFirstYear: '<%= afterFirstYear %>',
        useBeforeFirst: <%= useBeforeFirstEventTime %>,
        beforeFirstHour: '<%= beforeFirstHour %>',
        beforeFirstMinute: '<%= beforeFirstMinute %>',
        beforeFirstAmPm: '<%= Encode.forJavaScript(beforeFirstAmPm) %>',
        beforeFirstMonthIdx: '<%= beforeFirstMonthCount - 1 %>',
        beforeFirstDay: '<%= beforeFirstDay %>',
        beforeFirstYear: '<%= beforeFirstYear %>',
        useAfterLast: <%= useAfterLastEventTime %>,
        afterLastHour: '<%= afterLastHour %>',
        afterLastMinute: '<%= afterLastMinute %>',
        afterLastAmPm: '<%= Encode.forJavaScript(afterLastAmPm) %>',
        afterLastMonthIdx: '<%= afterLastMonthCount - 1 %>',
        afterLastDay: '<%= afterLastDay %>',
        afterLastYear: '<%= afterLastYear %>',
        useBeforeLast: <%= useBeforeLastEventTime %>,
        beforeLastHour: '<%= beforeLastHour %>',
        beforeLastMinute: '<%= beforeLastMinute %>',
        beforeLastAmPm: '<%= Encode.forJavaScript(beforeLastAmPm) %>',
        beforeLastMonthIdx: '<%= beforeLastMonthCount - 1 %>',
        beforeLastDay: '<%= beforeLastDay %>',
        beforeLastYear: '<%= beforeLastYear %>'
    };

    function applyState(s) {
        var form = document.querySelector('#advancedSearchModal form');

        form.querySelector('[name="alarmtext"]').value = s.alarmtext;
        form.querySelector('[name="iplike"]').value = s.iplike;
        form.querySelector('[name="nodenamelike"]').value = s.nodenamelike;
        form.querySelector('[name="sortby"]').value = s.sortby;
        form.querySelector('[name="limit"]').value = s.limit;
        form.querySelector('[name="situation"]').value = s.situation;
        form.querySelector('[name="category"]').value = s.category;

        var ncat = form.querySelector('[name="nestedCategoryNot"]');
        if (ncat) ncat.checked = s.nestedCategoryNot;

        form.querySelectorAll('[name^="severity-"]').forEach(function(cb) {
            cb.checked = s.severities.indexOf(parseInt(cb.name.split('-')[1], 10)) >= 0;
        });
        form.querySelectorAll('[name^="service-"]').forEach(function(cb) {
            cb.checked = s.services.indexOf(parseInt(cb.name.split('-')[1], 10)) >= 0;
        });

        function setCollapseExpanded(collapseId, expanded) {
            var colEl = document.getElementById(collapseId);
            if (!colEl) return;

            if (expanded) { colEl.classList.add('show'); }
            else { colEl.classList.remove('show'); }

            var targetSelector = '#' + collapseId;
            document.querySelectorAll(
                '[href="' + targetSelector + '"], ' +
                '[data-target="' + targetSelector + '"], ' +
                '[data-bs-target="' + targetSelector + '"]'
            ).forEach(function(toggleEl) {
                toggleEl.setAttribute('aria-expanded', expanded ? 'true' : 'false');
            });
        }

        function applyTime(prefix, collapseId, useTime, hour, minute, ampm, monthIdx, day, year) {
            var cb = form.querySelector('[name="use' + prefix + '"]');
            if (cb) cb.checked = useTime;
            setCollapseExpanded(collapseId, useTime);
            form.querySelector('[name="' + prefix + 'hour"]').value = hour;
            form.querySelector('[name="' + prefix + 'minute"]').value = minute;
            form.querySelector('[name="' + prefix + 'ampm"]').value = ampm;
            form.querySelector('[name="' + prefix + 'month"]').value = monthIdx;
            form.querySelector('[name="' + prefix + 'date"]').value = day;
            form.querySelector('[name="' + prefix + 'year"]').value = year;
        }

        applyTime('afterfirsteventtime', 'collapseAlarmsFirstAfter', s.useAfterFirst,
            s.afterFirstHour, s.afterFirstMinute, s.afterFirstAmPm, s.afterFirstMonthIdx, s.afterFirstDay, s.afterFirstYear);
        applyTime('beforefirsteventtime', 'collapseAlarmsFirstBefore', s.useBeforeFirst,
            s.beforeFirstHour, s.beforeFirstMinute, s.beforeFirstAmPm, s.beforeFirstMonthIdx, s.beforeFirstDay, s.beforeFirstYear);
        applyTime('afterlasteventtime', 'collapseAlarmsLastAfter', s.useAfterLast,
            s.afterLastHour, s.afterLastMinute, s.afterLastAmPm, s.afterLastMonthIdx, s.afterLastDay, s.afterLastYear);
        applyTime('beforelasteventtime', 'collapseAlarmsLastBefore', s.useBeforeLast,
            s.beforeLastHour, s.beforeLastMinute, s.beforeLastAmPm, s.beforeLastMonthIdx, s.beforeLastDay, s.beforeLastYear);
    }

    var _nowHour = '${nowHour}';
    var _nowMin = '${formattedNowMinute}';
    var _nowAmpm = '${amPmText}';
    var _nowMonthIdx = parseInt('${nowMonth}', 10) - 1;
    var _nowDay = '${nowDate}';
    var _nowYear = '${nowYear}';

    window.resetAdvancedSearch = function() {
        applyState({
            alarmtext: '', iplike: '', nodenamelike: '',
            sortby: 'id', limit: '20', situation: 'any', category: '',
            nestedCategoryNot: false,
            severities: [], services: [],
            useAfterFirst: false,
            afterFirstHour: _nowHour, afterFirstMinute: _nowMin, afterFirstAmPm: _nowAmpm,
            afterFirstMonthIdx: _nowMonthIdx, afterFirstDay: _nowDay, afterFirstYear: _nowYear,
            useBeforeFirst: false,
            beforeFirstHour: _nowHour, beforeFirstMinute: _nowMin, beforeFirstAmPm: _nowAmpm,
            beforeFirstMonthIdx: _nowMonthIdx, beforeFirstDay: _nowDay, beforeFirstYear: _nowYear,
            useAfterLast: false,
            afterLastHour: _nowHour, afterLastMinute: _nowMin, afterLastAmPm: _nowAmpm,
            afterLastMonthIdx: _nowMonthIdx, afterLastDay: _nowDay, afterLastYear: _nowYear,
            useBeforeLast: false,
            beforeLastHour: _nowHour, beforeLastMinute: _nowMin, beforeLastAmPm: _nowAmpm,
            beforeLastMonthIdx: _nowMonthIdx, beforeLastDay: _nowDay, beforeLastYear: _nowYear
        });
    };

    $('#advancedSearchModal').on('show.bs.modal', function() {
        applyState(_state);
    });
})();
</script>

<form action="alarm/query" method="post">
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

	<div class="form-group col-sm-6">
		<label for="alarmtext">Alarm Text Contains</label>
		<input class="form-control" type="text" name="alarmtext" value="<%= Encode.forHtmlAttribute(prefillAlarmText) %>" />
	</div>

	<div class="form-group col-sm-6">
		<label for="iplike">TCP/IP Address Like</label>
		<input class="form-control" type="text" name="iplike" value="<%= Encode.forHtmlAttribute(prefillIpLike) %>" />
	</div>

	<div class="form-group col-sm-6">
		<label for="nodenamelike">Node Label Contains</label>
		<input class="form-control" type="text" name="nodenamelike" value="<%= Encode.forHtmlAttribute(prefillNodeName) %>" />
	</div>

    <div class="form-group col-sm-6">
        <label for="severity">Severity</label>
        <% for (OnmsSeverity severity : OnmsSeverity.values()) { %>
            <div>
                <label>
                    <input type="checkbox" name="severity-<%=severity.getId()%>" value="1"
                           <%=checkedSeverities.contains(severity.getId()) ? "checked" : ""%> /> <%= Encode.forHtml(severity.getLabel()) %>
                </label>
            </div>
        <% } %>
    </div>

    <!-- Use clear:left to make sure that this column breaks onto a new row -->
    <div class="form-group col-sm-6">
        <label for="service">Service</label>
        <% for (String name : serviceNameSet) { %>
            <div>
                <label>
                    <input type="checkbox" name="service-<%=serviceNameMap.get(name)%>" value="1"
                           <%=checkedServices.contains(serviceNameMap.get(name)) ? "checked" : ""%> /> <%= Encode.forHtml(name) %>
                </label>
            </div>
        <% } %>
    </div>

	<div class="form-group col-sm-6">
		<label for="sortby">Sort By</label>
		<select class="form-control custom-select" name="sortby">
			<option value="id" ${prefillSortStyle == 'id' ? 'selected' : ''}>Alarm ID (Descending)</option>
			<option value="rev_id" ${prefillSortStyle == 'rev_id' ? 'selected' : ''}>Alarm ID (Ascending)</option>
			<option value="severity" ${prefillSortStyle == 'severity' ? 'selected' : ''}>Severity (Descending)</option>
			<option value="rev_severity" ${prefillSortStyle == 'rev_severity' ? 'selected' : ''}>Severity (Ascending)</option>
			<option value="lasteventtime" ${prefillSortStyle == 'lasteventtime' ? 'selected' : ''}>Time (Descending)</option>
			<option value="rev_lasteventtime" ${prefillSortStyle == 'rev_lasteventtime' ? 'selected' : ''}>Time (Ascending)</option>
			<option value="node" ${prefillSortStyle == 'node' ? 'selected' : ''}>Node (Ascending)</option>
			<option value="rev_node" ${prefillSortStyle == 'rev_node' ? 'selected' : ''}>Node (Descending)</option>
			<option value="interface" ${prefillSortStyle == 'interface' ? 'selected' : ''}>Interface (Ascending)</option>
			<option value="rev_interface" ${prefillSortStyle == 'rev_interface' ? 'selected' : ''}>Interface (Descending)</option>
			<option value="service" ${prefillSortStyle == 'service' ? 'selected' : ''}>Service (Ascending)</option>
			<option value="rev_service" ${prefillSortStyle == 'rev_service' ? 'selected' : ''}>Service (Descending)</option>
		</select>
	</div>

	<div class="form-group col-sm-6">
		<label for="limit">Number of Alarms Per Page</label>
		<select class="form-control custom-select" name="limit">
			<option value="10" ${prefillLimit == 10 ? 'selected' : ''}>10 alarms</option>
			<option value="20" ${prefillLimit == 20 ? 'selected' : ''}>20 alarms</option>
			<option value="30" ${prefillLimit == 30 ? 'selected' : ''}>30 alarms</option>
			<option value="50" ${prefillLimit == 50 ? 'selected' : ''}>50 alarms</option>
			<option value="100" ${prefillLimit == 100 ? 'selected' : ''}>100 alarms</option>
			<option value="-1" ${prefillLimit == -1 ? 'selected' : ''}>All alarms</option>
		</select>
	</div>

	<div class="form-group col-sm-6">
		<label for="situation">Alarm type</label>
		<select class="form-control custom-select" name="situation">
			<option value="any" <%= "any".equals(prefillSituation) ? "selected" : "" %>>All Alarms and Situations</option>
			<option value="false" <%= "false".equals(prefillSituation) ? "selected" : "" %>>Only Alarms</option>
			<option value="true" <%= "true".equals(prefillSituation) ? "selected" : "" %>>Only Situations</option>
		</select>
	</div>

	<div class="form-group col-sm-6">
		<label for="category">Category</label>
		<select class="form-control custom-select" name="category">
			<option value="" <%= prefillCategory.isEmpty() ? "selected" : "" %>>Do not filter by category</option>
			<% for (final String category: categories) { %>
			<option value="<%= Encode.forHtmlAttribute(category) %>" <%= category.equals(prefillCategory) ? "selected" : "" %>><%= Encode.forHtml(category) %></option>
			<% } %>
		</select>

        <label for="nestedCategoryNot">Exclude:</label>
        <input type="checkbox" name="nestedCategoryNot" <%= prefillNestedCategoryNot ? "checked" : "" %>/>
	</div>

	<div class="col-sm-6 my-2">
		<label data-toggle="collapse" data-target="#collapseAlarmsFirstAfter" aria-expanded="<%= useAfterFirstEventTime ? "true" : "false" %>" aria-controls="collapseAlarmsFirstAfter">
			<input type="checkbox" name="useafterfirsteventtime" value="1" <%= useAfterFirstEventTime ? "checked" : "" %> /> Filter for Alarms after First Event:
		</label>
		<br />
		<div id="collapseAlarmsFirstAfter" class="collapse <%= useAfterFirstEventTime ? "show" : "" %>">
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterfirsteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${afterFirstHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterfirsteventtimeminute" maxlength="2" value="${afterFirstMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterfirsteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime}" selected="${dayTime==afterFirstAmPm}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterfirsteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == afterFirstMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterfirsteventtimedate" maxlength="2" value="${afterFirstDay}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterfirsteventtimeyear" maxlength="4" value="${afterFirstYear}" />
			</div>
		</div>
		</div>
	</div>

	<div class="col-sm-6 my-2">
		<label data-toggle="collapse" data-target="#collapseAlarmsFirstBefore" aria-expanded="<%= useBeforeFirstEventTime ? "true" : "false" %>" aria-controls="collapseAlarmsFirstBefore">
			<input type="checkbox" name="usebeforefirsteventtime" value="1" <%= useBeforeFirstEventTime ? "checked" : "" %> /> Filter for Alarms before First Event:
		</label>
		<br />
		<div id="collapseAlarmsFirstBefore" class="collapse <%= useBeforeFirstEventTime ? "show" : "" %>">
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforefirsteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${beforeFirstHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforefirsteventtimeminute" maxlength="2" value="${beforeFirstMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforefirsteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime}" selected="${dayTime==beforeFirstAmPm}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforefirsteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == beforeFirstMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforefirsteventtimedate" maxlength="2" value="${beforeFirstDay}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforefirsteventtimeyear" maxlength="4" value="${beforeFirstYear}" />
			</div>
		</div>
		</div>
	</div>

	<div class="col-sm-6 my-2">
		<label data-toggle="collapse" data-target="#collapseAlarmsLastAfter" aria-expanded="<%= useAfterLastEventTime ? "true" : "false" %>" aria-controls="collapseAlarmsLastAfter">
			<input type="checkbox" name="useafterlasteventtime" value="1" <%= useAfterLastEventTime ? "checked" : "" %> /> Filter for Alarms after Last Event:
		</label>
		<br />
		<div id="collapseAlarmsLastAfter" class="collapse <%= useAfterLastEventTime ? "show" : "" %>">
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterlasteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${afterLastHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterlasteventtimeminute" maxlength="2" value="${afterLastMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterlasteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime}" selected="${dayTime==afterLastAmPm}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="afterlasteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == afterLastMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterlasteventtimedate" maxlength="2" value="${afterLastDay}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="afterlasteventtimeyear" maxlength="4" value="${afterLastYear}" />
			</div>
		</div>
		</div>
	</div>

	<div class="col-sm-6 my-2">
		<label data-toggle="collapse" data-target="#collapseAlarmsLastBefore" aria-expanded="<%= useBeforeLastEventTime ? "true" : "false" %>" aria-controls="collapseAlarmsLastBefore">
			<input type="checkbox" name="usebeforelasteventtime" value="1" <%= useBeforeLastEventTime ? "checked" : "" %> /> Filter for Alarms before Last Event:
		</label>
		<br />
		<div id="collapseAlarmsLastBefore" class="collapse <%= useBeforeLastEventTime ? "show" : "" %>">
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforelasteventtimehour">
					<c:forEach var="i" begin="1" end="12">
						<form:option value="${i}" selected="${beforeLastHour==i}">${i}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforelasteventtimeminute" maxlength="2" value="${beforeLastMinute}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforelasteventtimeampm">
					<c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
						<form:option value="${dayTime}" selected="${dayTime==beforeLastAmPm}">${dayTime}</form:option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-xs-4 col-sm-4 col-md-3">
				<select class="form-control custom-select" name="beforelasteventtimemonth">
					<c:forEach var="month" items="${months}" varStatus="status">
						<form:option value="${status.index}" selected="${status.count == beforeLastMonthCount}">${month}</form:option>
					</c:forEach>
				</select>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforelasteventtimedate" maxlength="2" value="${beforeLastDay}" />
			</div>
			<div class="col-xs-4 col-sm-4 col-md-3">
				<input class="form-control" type="text" name="beforelasteventtimeyear" maxlength="4" value="${beforeLastYear}" />
			</div>
		</div>
		</div>
	</div>

	<div class="col-sm-12 my-2">
		<button class="btn btn-secondary" type="submit"><i class="fa fa-search"></i> Search</button>
		<button class="btn btn-secondary" type="button" onclick="resetAdvancedSearch()">Reset</button>
	</div>
</form>

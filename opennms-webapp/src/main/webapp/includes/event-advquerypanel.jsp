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
	buffer="1024kb"
	import="java.util.*,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.netmgt.model.OnmsSeverity
	"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> 
<%@ taglib tagdir="/WEB-INF/tags/form" prefix="form" %>
<%
    //get the service names, in alpha order
    Map<String, Integer> serviceNameMap = new TreeMap<String, Integer>(NetworkElementFactory.getInstance(getServletContext()).getServiceNameToIdMap());
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
    <c:when test="${nowAmPm == 'AM' && nowHour == 12}">Noon</c:when>
    <c:when test="${nowAmPm == 'AM' && nowHour != 12}">AM</c:when>
    <c:when test="${nowAmPm == 'PM' && nowHour == 12}">Midnight</c:when>
    <c:when test="${nowAmPm == 'PM' && nowHour != 12}">PM</c:when>
  </c:choose>
</c:set>



<form action="event/query" method="get">
  <table width="100%">
    <tr>
      <td valign="top">
        <table width="100%">
          <tr>
            <td>Event Text Contains:</td>
            <td>TCP/IP Address Like:</td>
          </tr>

          <tr>
            <td><input type="text" name="msgsub" /></td>
            <td><input type="text" name="iplike" value="" /></td>
          </tr>

          <tr>
            <td>Node Label Contains:</td>
            <td>Severity:</td>
          </tr>

          <tr>
            <td><input type="text" name="nodenamelike" /></td>
            <td>
              <select name="severity" size="1">
                <option selected="selected">Any</option>

                <% for (OnmsSeverity severity : OnmsSeverity.values() ) { %>
                  <option value="<%= severity.getId() %>">
                    <%= severity.getLabel() %>
                  </option>
                <% } %>
              </select>
            </td>
          </tr>

          <tr><td colspan="2">Exact Event UEI:</td></tr>
          <tr><td colspan="2"><input type="text" name="exactuei" size="64" maxsize="128" /></td></tr>

          <tr>
            <td colspan="2">Service:</td>
          </tr>
          <tr>
            <td colspan="2">
              <select name="service" size="1">
                <option selected>Any</option>

                <% for (String name : serviceNameSet) { %>
                  <option value="<%=serviceNameMap.get(name)%>"><%=name%></option>
                <% } %>
              </select>
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

          <tr>
            <td valign="top">
              <input type="checkbox" name="useaftertime" value="1" />Events After:
            </td>
            <td valign="top">
              <input type="checkbox" name="usebeforetime" value="1"/>Events Before:
            </td>
          </tr>
          <tr>
            <td>
              <select name="afterhour" size="1">
                <c:forEach var="i" begin="1" end="12">
                  <form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
                </c:forEach>
              </select>

              <input type="text" name="afterminute" size="4" maxlength="2" value="${formattedNowMinute}" />

              <select name="afterampm" size="1">
                <c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
                  <form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
                </c:forEach>
              </select>
            </td>
            <td>
              <select name="beforehour" size="1">
                <c:forEach var="i" begin="1" end="12">
                  <form:option value="${i}" selected="${nowHour==i}">${i}</form:option>
                </c:forEach>
              </select>

              <input type="text" name="beforeminute" size="4" maxlength="2" value="${formattedNowMinute}" />

              <select name="beforeampm" size="1">
                <c:forEach var="dayTime" items="AM,Noon,PM,Midnight">
                  <form:option value="${dayTime == 'AM' || dayTime == 'Midnight' ? 'am' : 'pm'}" selected="${dayTime==amPmText}">${dayTime}</form:option>
                </c:forEach>
              </select>
            </td>
          </tr>
          <tr>
            <td>
              <select name="aftermonth" size="1">
                <c:forEach var="month" items="${months}" varStatus="status">
                  <form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>                  
                </c:forEach>
              </select>

              <input type="text" name="afterdate" size="4" maxlength="2" value="${nowDate}" />
              <input type="text" name="afteryear" size="6" maxlength="4" value="${nowYear}" />
            </td>
            <td>
              <select name="beforemonth" size="1">
                <c:forEach var="month" items="${months}" varStatus="status">
                  <form:option value="${status.index}" selected="${status.count == nowMonth}">${month}</form:option>                  
                </c:forEach>
              </select>

              <input type="text" name="beforedate" size="4" maxlength="2" value="${nowDate}" />
              <input type="text" name="beforeyear" size="6" maxlength="4" value="${nowYear}" />
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

          <tr>
            <td>Sort By:</td>
            <td>Number of Events Per Page:</td>
          </tr>
          <tr>
            <td>
              <select name="sortby" size="1">
                <option value="id"           >Event ID  (Descending)</option>
                <option value="rev_id"       >Event ID  (Ascending) </option>
                <option value="severity"     >Severity  (Descending)</option>
                <option value="rev_severity" >Severity  (Ascending) </option>
                <option value="time"         >Time      (Descending)</option>
                <option value="rev_time"     >Time      (Ascending) </option>
                <option value="node"         >Node      (Ascending) </option>
                <option value="rev_node"     >Node      (Descending)</option>
                <option value="interface"    >Interface (Ascending) </option>
                <option value="rev_interface">Interface (Descending)</option>
                <option value="service"      >Service   (Ascending) </option>
                <option value="rev_service"  >Service   (Descending)</option>
              </select>
            </td>
            <td>
              <select name="limit" size="1">
                <option value="10">10 events</option>
                <option value="20">20 events</option>
                <option value="30">30 events</option>
                <option value="50">50 events</option>
                <option value="100">100 events</option>
                <option value="1000">1000 events</option>
              </select>
            </td>
          </tr>

          <tr><td colspan="2"><hr width=100% /></td></tr>

        </table>
      </td>
    </tr>
    <tr>
      <td>
        <input type="submit" value="Search" />
      </td>
    </tr>
  </table>
</form>




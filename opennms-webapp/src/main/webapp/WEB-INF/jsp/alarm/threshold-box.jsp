<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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

<%--
  This page is included by other JSPs to create a box containing an
  abbreviated list of threshold alarms.

  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url var="headingLink" value="alarm/list.htm?sortby=id&acktype=unack&limit=20&display=long&filter=partialUei="/>
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title"><a href="${headingLink}${ueiFilter}">Nodes with Threshold Alarms</a></h3>
    </div>
    <c:choose>
        <c:when test="${empty thresholdAlarmList}">
            <div class="panel-body">
                <p class="noBottomMargin">
                    There are no pending threshold alarms.
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <table class="table table-condensed severity">
                <c:forEach var="thresholdAlarm" items="${thresholdAlarmList}">
                    <c:url var="thresholdAlarmLink" value="alarm/detail.htm">
                        <c:param name="id" value="${thresholdAlarm.id}"/>
                    </c:url>
                    <c:choose>
                        <c:when test="${fn:contains(thresholdAlarm.uei, 'relativeChangeExceeded') && ! fn:contains(thresholdAlarm.uei, 'Rearmed')}">
                            <tr class="severity-${thresholdAlarm.severity.label} nodivider">
                                <td class="bright">
                                    <img src="images/thresh_change.png" />&nbsp;<a href="alarm/detail.htm?id=${thresholdAlarm.id}" title="${thresholdAlarm.logMsg}">${thresholdAlarm.nodeLabel}</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:when test="${fn:contains(thresholdAlarm.uei, 'absoluteChangeExceeded') && ! fn:contains(thresholdAlarm.uei, 'Rearmed')}">
                            <tr class="severity-${thresholdAlarm.severity.label} nodivider">
                                <td class="bright">
                                    <img src="images/thresh_change.png" />&nbsp;<a href="alarm/detail.htm?id=${thresholdAlarm.id}" title="${thresholdAlarm.logMsg}">${thresholdAlarm.nodeLabel}</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:when test="${fn:contains(thresholdAlarm.uei, 'highThresholdExceeded') && ! fn:contains(thresholdAlarm.uei, 'Rearmed')}">
                            <tr class="severity-${thresholdAlarm.severity.label} nodivider">
                                <td class="bright">
                                    <img src="images/thresh_high.png" />&nbsp;<a href="alarm/detail.htm?id=${thresholdAlarm.id}" title="${thresholdAlarm.logMsg}">${thresholdAlarm.nodeLabel}</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:when test="${fn:contains(thresholdAlarm.uei, 'lowThresholdExceeded') && ! fn:contains(thresholdAlarm.uei, 'Rearmed')}">
                            <tr class="severity-${thresholdAlarm.severity.label} nodivider">
                                <td class="bright">
                                    <img src="images/thresh_low.png" />&nbsp;<a href="alarm/detail.htm?id=${thresholdAlarm.id}" title="${thresholdAlarm.logMsg}">${thresholdAlarm.nodeLabel}</a>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <tr class="severity-${thresholdAlarm.severity.label} nodivider">
                                <td class="bright">
                                    <img src="images/thresh_unknown.png" />&nbsp;<a href="alarm/detail.htm?id=${thresholdAlarm.id}" title="${thresholdAlarm.logMsg}">${thresholdAlarm.nodeLabel}</a>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </table>
            <c:if test="${moreCount > 0}">
                <div class="panel-footer text-right">
                    <a href="${headingLink}${ueiFilter}">All threshold alarms...</a>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>
</div>

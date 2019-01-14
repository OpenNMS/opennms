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

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of alarms.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.netmgt.dao.api.MonitoringLocationDao"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  pageContext.setAttribute("defaultLocation", MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID);
%>

<!-- alarm/summary-box.htm -->
<c:url var="headingLink" value="alarm/list.htm"/>
<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title"><a href="${headingLink}">Pending Situations</a></h3>
  </div>
  <c:choose>
    <c:when test="${empty summaries}">
      <div class="panel-body">
        <p class="noBottomMargin">
          There are no pending problems.
        </p>
      </div>
    </c:when>
    <c:otherwise>
      <table class="table table-condensed severity">
        <c:forEach var="summary" items="${summaries}">
          <tr class="severity-${summary.situationSeverity.label} nodivider">
            <td class="bright">
              <a href="alarm/detail.htm?id=${summary.situationId}">Situation ${summary.situationId}</a>
              <c:if test="${summary.affectedNodes > 0}">
                affecting ${summary.affectedNodes} node${summary.affectedNodes > 1 ? 's' : ''}
              </c:if>
              having ${summary.relatedAlarms} alarm${summary.relatedAlarms > 1 ? 's' : ''}
              <c:if test="${summary.situationLocations != null && !defaultLocation.equals(summary.situationLocations)}">
                <br/>in location${fn:length(summary.situationLocationList) > 1 ? 's' : ''}
                <c:forEach var="location" items="${summary.situationLocationList}" varStatus="status">
                  <c:choose>
                    <c:when test="${status.first}">
                      ${location}
                    </c:when>
                    <c:otherwise>
                      <c:choose>
                        <c:when test="${status.last}">
                          and ${location}
                        </c:when>
                        <c:otherwise>
                          , ${location}
                        </c:otherwise>
                      </c:choose>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
              </c:if>
              <span style="white-space:nowrap;">(${summary.fuzzyTimeDown})</span>
            </td>
          </tr>
        </c:forEach>
      </table>
      <c:if test="${moreCount > 0}">
        <div class="panel-footer text-right">
          <c:url var="moreLink" value="alarm/list.htm"/>
          <a href="alarm/list.htm?filter=situation%3Dtrue">All pending problems...</a>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

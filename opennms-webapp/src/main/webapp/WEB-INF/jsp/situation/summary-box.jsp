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
<div class="card">
  <div class="card-header">
    <span><a href="${headingLink}">Pending Situations</a></span>
  </div>
  <c:choose>
    <c:when test="${empty summaries}">
      <div class="card-body">
        <p class="mb-0">
          There are no pending alarms.
        </p>
      </div>
    </c:when>
    <c:otherwise>
      <table class="table table-sm severity mb-0">
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
        <div class="card-footer text-right">
          <c:url var="moreLink" value="alarm/list.htm"/>
          <a href="alarm/list.htm?filter=situation%3Dtrue">All pending alarms...</a>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

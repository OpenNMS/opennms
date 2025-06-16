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
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- alarm/summary-box.htm -->
<c:url var="headingLink" value="alarm/list.htm"/>
<div class="card">
  <div class="card-header">
    <span><a href="${headingLink}">Nodes with Pending Alarms</a></span>
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
          <c:url var="nodeLink" value="element/node.jsp">
            <c:param name="node" value="${summary.nodeId}"/>
          </c:url>
          <tr class="severity-${summary.maxSeverity.label} nodivider"><td class="bright">
              <a href="${nodeLink}"><c:out value="${summary.nodeLabel}"/></a> has
              <a href="alarm/list.htm?sortby=id&acktype=unack&limit=20&display=short&filter=node%3D${summary.nodeId}">${summary.alarmCount}&nbsp;alarm${summary.alarmCount > 1 ? "s" : ""}</a>
              <span style="white-space:nowrap;">(${summary.fuzzyTimeDown})</span>
          </td></tr>
        </c:forEach>
      </table>
      <c:if test="${moreCount > 0}">
        <div class="card-footer text-right">
          <c:url var="moreLink" value="alarm/list.htm"/>
          <a href="${moreLink}">All pending alarms...</a>
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

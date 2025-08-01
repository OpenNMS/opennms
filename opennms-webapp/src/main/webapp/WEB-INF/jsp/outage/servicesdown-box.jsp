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
  abbreviated list of outages.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- outage/servicesdown-box.htm -->
<c:url var="headingLink" value="outage/list.htm"/>
<div class="card">
  <div class="card-header">
    <span><a href="${headingLink}">Nodes with Outages</a></span>
  </div>
  <div class="card-body">
    <c:choose>
      <c:when test="${empty summaries}">
        <p class="mb-0">
          There are no current outages
        </p>
      </c:when>

      <c:otherwise>
        <ul class="list-unstyled mb-0">
          <c:forEach var="summary" items="${summaries}">
            <c:url var="nodeLink" value="element/node.jsp">
              <c:param name="node" value="${summary.nodeId}"/>
            </c:url>
            <li><a href="${nodeLink}"><c:out value="${summary.nodeLabel}"/></a> <span style="white-space:nowrap;">(${summary.fuzzyTimeDown})</span></li>
          </c:forEach>
        </ul>
    
      </c:otherwise>
    </c:choose>
  </div>
  <c:if test="${moreCount > 0}">
    <div class="card-footer text-right">
      <c:url var="moreLink" value="outage/list.htm"/>
      <a href="${moreLink}">${moreCount} more nodes with outages...</a>
    </div>
  </c:if>
</div>

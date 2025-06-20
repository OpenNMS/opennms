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
<%@ page language="java" contentType="text/html" session="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- application/summary-box.htm -->
<c:url var="headingLink" value="application/index.jsp"/>
<div class="card">
  <div class="card-header">
    <a href="${headingLink}">Applications with Pending Alarms</a>
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
          <c:url var="applicationTopoLink" value="topology">
            <c:param name="focus-vertices" value="${summary.application.id}"/>
            <c:param name="szl" value="1"/>
            <c:param name="layout" value="Hierarchy Layout" />
            <c:param name="provider" value="Application" />
          </c:url>
          <tr class="severity-${summary.severity.label} nodivider">
            <td class="bright">
              <a href="${applicationTopoLink}">${summary.application.name}</a>
            </td>
          </tr>
        </c:forEach>
      </table>
      <c:if test="${more}">
        <div class="card-footer text-right">
          Not all Applications with Pending Alarms are shown.
        </div>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

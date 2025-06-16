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
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Site Status")
          .breadcrumb("Site Status")
          .breadcrumb("${e:forHtmlAttribute(view.columnValue)}")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Site status for nodes in site '${e:forHtml(view.columnValue)}'</span>
  </div>
  <table class="table table-sm table-bordered severity">
    <thead>
      <tr>
        <th>Device Type</th>
        <th>Nodes Down</th>
      </tr>
    </thead>
    <c:forEach items="${stati}" var="status">
      <tr class="CellStatus" >
        <td>${status.label}</td>
        <td class="bright severity-${status.status}" >
          <c:choose>
            <c:when test="${! empty status.link}">
              <c:url var="statusLink" value="${status.link}"/>
              <a href="${statusLink}">${status.downEntityCount} of ${status.totalEntityCount}</a>
            </c:when>
            <c:otherwise>
              ${status.downEntityCount} of ${status.totalEntityCount}
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
  </table>
</div> <!-- panel -->
  
<div class="card">
  <div class="card-header">
    <span>Site outages</span>
  </div>
  <div class="card-body">
    <c:url var="outagesLink" value="outage/list.htm">
      <c:param name="filter" value="asset.building=${view.columnValue}"/>
    </c:url>
    <p>
      <a href="${outagesLink}">View</a> current site outages.
    </p>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

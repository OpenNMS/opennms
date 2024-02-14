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
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<div id="application-box" class="card">

<div class="card-header">
<span>
  Application Memberships
  <c:if test="${isAdmin == 'true'}">
    (<a href="<c:url value='admin/applications.htm?edit&ifserviceid=${service.id}'/>">Edit</a>)
  </c:if>
</span>
</div>

<table class="table table-sm">
  <c:if test="${empty applications}">
    <tr>
      <td>This service is not a member of any applications</td>
    </tr>
  </c:if>
  
  <c:forEach items="${applications}" var="application">
    <tr>
      <td>${application.name}</td>
    </tr>
  </c:forEach>
</table>

</div>

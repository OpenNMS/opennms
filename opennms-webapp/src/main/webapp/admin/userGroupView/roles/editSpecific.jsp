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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Edit Schedule")
          .headTitle("On-Call Roles")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users, Groups and On-Call Roles", "admin/userGroupView/index.jsp")
          .breadcrumb("On-Call Role List", "admin/userGroupView/roles")
          .breadcrumb("Edit Entry")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Edit Schedule Entry</span>
  </div>
  <div class="card-body">
    <p class="alert alert-danger">${error}</p>
    <form role="form" class="form" action="<c:url value='${reqUrl}'/>" method="post" name="saveEntryForm">
      <input type="hidden" name="operation" value="saveEntry"/>
      <input type="hidden" name="role" value="${fn:escapeXml(role.name)}"/>
      <input type="hidden" name="schedIndex" value="${schedIndex}"/>
      <input type="hidden" name="timeIndex" value="${timeIndex}" />
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

      <div class="form-group form-row">
        <label class="col-sm-2">On-Call Role</label>
        <div class="col-sm-4">
          <p class="form-control-static"><c:out value="${role.name}"/></p>
        </div>
      </div>

      <div class="form-group form-row">
        <label for="input_roleUser" class="col-sm-2">User</label>
        <div class="col-sm-4">
          <select class="form-control custom-select" id="input_roleUser" name="roleUser">
          <c:forEach var="user" items="${role.membershipGroup.users}">
            <c:choose>
              <c:when test="${user == scheduledUser}"><option selected>${user}</option></c:when>
              <c:otherwise><option>${user}</option></c:otherwise>
            </c:choose>
          </c:forEach>
          </select>
        </div>
      </div>

      <div class="form-group form-row">
        <label class="col-sm-2">Start Date</label>
        <div class="col-sm-4">
          <c:import url="/includes/dateControl.jsp">
            <c:param name="prefix" value="start"/>
            <c:param name="date"><fmt:formatDate value="${start}" pattern="dd-MM-yyyy"/></c:param>
          </c:import>
        </div>
        <label class="col-sm-2">Start Time</label>
        <div class="col-sm-4">
          <c:import url="/includes/timeControl.jsp">
            <c:param name="prefix" value="start"/>
            <c:param name="time"><fmt:formatDate value="${start}" pattern="HH:mm:ss"/></c:param>
          </c:import>
        </div>
      </div>

      <div class="form-group form-row">
        <label class="col-sm-2">End Date</label>
        <div class="col-sm-4">
          <c:import url="/includes/dateControl.jsp">
            <c:param name="prefix" value="end"/>
            <c:param name="date"><fmt:formatDate value="${end}" pattern="dd-MM-yyyy"/></c:param>
          </c:import>
        </div>
        <label class="col-sm-2">End Time</label>
        <div class="col-sm-4">
          <c:import url="/includes/timeControl.jsp">
            <c:param name="prefix" value="end"/>
            <c:param name="time"><fmt:formatDate value="${end}" pattern="HH:mm:ss"/></c:param>
          </c:import>
        </div>
      </div>

      <button type="submit" class="btn btn-secondary" name="save">Save</button>
      <button type="submit" class="btn btn-secondary" name="cancel">Cancel</button>
    </form>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

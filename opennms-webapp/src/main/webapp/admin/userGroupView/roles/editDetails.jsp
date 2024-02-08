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
	session="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Edit")
          .headTitle("On-Call Roles")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Users, Groups and On-Call Roles", "admin/userGroupView/index.jsp")
          .breadcrumb("On-Call Role List", "admin/userGroupView/roles")
          .breadcrumb("Edit On-Call Role")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

	function changeDisplay() {
		document.displayForm.submit();
	}
	
	function prevMonth() {
		document.prevMonthForm.submit();
	}
	
	function nextMonth() {
		document.nextMonthForm.submit();
	}

</script>

<div class="card pane-default">
  <div class="card-header">
    <span>Edit On-Call Role</span>
  </div>
  <div class="card-body">
    <form role="form" class="form" action="<c:url value='${reqUrl}'/>" method="post" name="editForm">
      <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
      <input type="hidden" name="operation" value="saveDetails"/>
      <input type="hidden" name="role" value="${fn:escapeXml(role.name)}"/>

      <div class="form-group form-row">
        <label for="input_roleName" class="col-sm-2 col-form-label">Name</label>
        <div class="col-sm-10">
          <input class="form-control" name="roleName" id="input_roleName" type="text" value="${fn:escapeXml(role.name)}"/>
        </div>
      </div>

      <div class="form-group form-row">
        <label class="col-sm-2 col-form-label"><strong>Currently On Call</strong></label>
        <div class="col-sm-10">
          <ul>
            <c:forEach var="scheduledUser" items="${role.currentUsers}">
              <li>${scheduledUser}</li>
            </c:forEach>
          </ul>
        </div>
      </div>

      <div class="form-group form-row">
        <label for="input_roleUser" class="col-sm-2 col-form-label">Supervisor</label>
        <div class="col-sm-10">
          <select class="form-control custom-select" id="input_roleUser" name="roleUser">
            <c:forEach var="user" items="${userManager.users}">
              <c:choose>
                <c:when test="${user == role.defaultUser}">
                  <option selected>${user}</option>
                </c:when>
                <c:otherwise>
                  <option>${user}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
      </div>

      <div class="form-group form-row">
        <label for="input_roleGroup" class="col-sm-2 col-form-label">Membership Group</label>
        <div class="col-sm-10">
          <select class="form-control custom-select" id="input_roleGroup" name="roleGroup">
            <c:forEach var="group" items="${groupManager.groups}">
              <c:choose>
                <c:when test="${group == role.membershipGroup}">
                  <option selected>${group}</option>
                </c:when>
                <c:otherwise>
                  <option>${group}</option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
        </div>
      </div>

      <div class="form-group form-row">
        <label for="input_roleDescr" class="col-sm-2 col-form-label">Description</label>
        <div class="col-sm-10">
          <input class="form-control" name="roleDescr" id="input_roleDescr" type="text" value="${fn:escapeXml(role.description)}"/>
        </div>
      </div>

      <button type="submit" class="btn btn-secondary" name="save">Save</button>
      <button type="submit" class="btn btn-secondary" name="cancel">Cancel</button>
    </form>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

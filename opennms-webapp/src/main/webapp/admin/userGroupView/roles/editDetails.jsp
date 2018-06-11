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

<%@page language="java"
	contentType="text/html"
	session="true"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="On-Call Role Configuration" />
	<jsp:param name="headTitle" value="Edit" />
	<jsp:param name="headTitle" value="On-Call Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and On-Call Roles</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/roles'>On-Call Role List</a>" />
	<jsp:param name="breadcrumb" value="Edit On-Call Role" />
</jsp:include>

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

<div class="panel pane-default">
  <div class="panel-heading">
    <h3 class="panel-title">Edit On-Call Role</h3>
  </div>
  <div class="panel-body">
    <form role="form" class="form-horizontal" action="<c:url value='${reqUrl}'/>" method="post" name="editForm">
      <input type="hidden" name="operation" value="saveDetails"/>
      <input type="hidden" name="role" value="${fn:escapeXml(role.name)}"/>

      <div class="form-group">
        <label for="input_roleName" class="col-sm-2 control-label">Name</label>
        <div class="col-sm-10">
          <input class="form-control" name="roleName" id="input_roleName" type="text" value="${fn:escapeXml(role.name)}"/>
        </div>
      </div>

      <div class="form-group">
        <label class="col-sm-2 control-label">Currently On Call</label>
        <div class="col-sm-10">
          <ul>
            <c:forEach var="scheduledUser" items="${role.currentUsers}">
              <li>${scheduledUser}</li>
            </c:forEach>
          </ul>
        </div>
      </div>

      <div class="form-group">
        <label for="input_roleUser" class="col-sm-2 control-label">Supervisor</label>
        <div class="col-sm-10">
          <select class="form-control" id="input_roleUser" name="roleUser">
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

      <div class="form-group">
        <label for="input_roleGroup" class="col-sm-2 control-label">Membership Group</label>
        <div class="col-sm-10">
          <select class="form-control" id="input_roleGroup" name="roleGroup">
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

      <div class="form-group">
        <label for="input_roleDescr" class="col-sm-2 control-label">Description</label>
        <div class="col-sm-10">
          <input class="form-control" name="roleDescr" id="input_roleDescr" type="text" value="${fn:escapeXml(role.description)}"/>
        </div>
      </div>

      <button type="submit" class="btn btn-default" name="save">Save</button>
      <button type="submit" class="btn btn-default" name="cancel">Cancel</button>
    </form>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

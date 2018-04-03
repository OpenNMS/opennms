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
	session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="On-Call Role Configuration" />
	<jsp:param name="headTitle" value="Edit Schedule" />
	<jsp:param name="headTitle" value="On-Call Roles" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/index.jsp'>Users, Groups and On-Call Roles</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/userGroupView/roles'>On-Call Role List</a>" />
	<jsp:param name="breadcrumb" value="Edit Entry" />
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Edit Schedule Entry</h3>
  </div>
  <div class="panel-body">
    <p class="lead">${error}</p>
    <form role="form" class="form-horizontal" action="<c:url value='${reqUrl}'/>" method="post" name="saveEntryForm">
      <input type="hidden" name="operation" value="saveEntry"/>
      <input type="hidden" name="role" value="${fn:escapeXml(role.name)}"/>
      <input type="hidden" name="schedIndex" value="${schedIndex}"/>
      <input type="hidden" name="timeIndex" value="${timeIndex}" /> 

      <div class="form-group">
        <label class="col-sm-2">On-Call Role</label>
        <div class="col-sm-4">
          <p class="form-control-static"><c:out value="${role.name}"/></p>
        </div>
      </div>

      <div class="form-group">
        <label for="input_roleUser" class="col-sm-2">User</label>
        <div class="col-sm-4">
          <select class="form-control" id="input_roleUser" name="roleUser">
          <c:forEach var="user" items="${role.membershipGroup.users}">
            <c:choose>
              <c:when test="${user == scheduledUser}"><option selected>${user}</option></c:when>
              <c:otherwise><option>${user}</option></c:otherwise>
            </c:choose>
          </c:forEach>
          </select>
        </div>
      </div>

      <div class="form-group">
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

      <div class="form-group">
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

      <button type="submit" class="btn btn-default" name="save">Save</button>
      <button type="submit" class="btn btn-default" name="cancel">Cancel</button>
    </form>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

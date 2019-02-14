<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@ page import="org.opennms.web.api.Authentication" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Manage Report Schedule" />
  <jsp:param name="headTitle" value="Manage Report Schedule" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Manage Report Schedule"/>
</jsp:include>

<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/manageSchedule.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>

<div class="row">
    <div class="col-md-12">
        <c:choose>
            <c:when test="${empty pagedListHolder.pageList}">
                <div class="card">
                    <div class="card-header">
                        <span>Report Schedule List</span>
                    </div>
                    <div class="card-body">
                        <p>The database report schedule is empty.</p>
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <form:form commandName="command">
                    <element:pagedList pagedListHolder="${pagedListHolder}"
                                       pagedLink="${pagedLink}" />

                    <div class="spacer"><!--  --></div>
                    <table class="table table-bordered table-striped table-hover">
                        <thead>
                        <tr>
                            <th>Trigger Name</th>
                            <th>Next fire time</th>
                            <th>Report Parameters</th>
                            <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                            <th>Select</th>
                            <% } %>
                        </tr>
                        </thead>
                            <%-- // show only current page worth of data --%>
                        <c:forEach items="${pagedListHolder.pageList}" var="trigger">
                            <tr>
                                <td>${trigger.triggerName}</td>
                                <td>${trigger.nextFireTime}</td>
                                <td>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <div class="row">
                                                <div class="col-md-2"><strong>Report ID:</strong></div>
                                                <div class="col-md-6">${trigger.reportId}</div>
                                            </div>

                                            <div class="row">
                                                <div class="col-md-2"><strong>Format:</strong></div>
                                                <div class="col-md-6"> ${trigger.deliveryOptions.format}</div>
                                            </div>
                                            <div class="row">
                                                <div class="col-md-2"><strong>Persist:</strong></div>
                                                <div class="col-md-6">${trigger.deliveryOptions.persist}</div>
                                            </div>
                                            <div class="row">
                                                <div class="col-md-2"><strong>Mail:</strong></div>
                                                <div class="col-md-6">
                                                    <c:choose>
                                                        <c:when test="${trigger.deliveryOptions.sendMail}">${trigger.deliveryOptions.mailTo}</c:when>
                                                        <c:otherwise>false</c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <c:forEach items="${trigger.reportParameters}" var="entry">
                                                <div class="row">
                                                    <div class="col-md-2"><strong>${entry.key}:</strong></div>
                                                    <div class="col-md-6">${entry.value}</div>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </td>
                                <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                                <td><form:checkbox path="triggerNames" value="${trigger.triggerName}"/></td>
                                <% } %>
                            </tr>
                        </c:forEach>
                    </table>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                    <div class="pagination">
                        <button type="button" class="btn btn-link" onClick="toggle(true, 'triggerNames')">Select all</button>
                        <button type="button" class="btn btn-link" href onClick="toggle(false, 'triggerNames')">Deselect all</button>
                    </div>
                    <% } %>

                    <% // if deletion was successful %>
                    <c:if test="${not empty success}">
                        <div class="alert alert-success" style="clear:both">
                                ${success}
                        </div>
                    </c:if>

                    <% // If user is not allowed to delete %>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" style="clear:both">
                                ${error}
                        </div>
                    </c:if>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                    <input type="submit" class="btn btn-secondary" value="unschedule selected jobs"/>
                    <% } %>

                </form:form>



            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

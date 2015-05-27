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
  <jsp:param name="title" value="Manage Reports" />
  <jsp:param name="headTitle" value="Manage Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Manage Reports"/>
</jsp:include>

<jsp:useBean id="pagedListHolder" scope="request"
	type="org.springframework.beans.support.PagedListHolder" />
<c:url value="/report/database/manage.htm" var="pagedLink">
	<c:param name="p" value="~" />
</c:url>

<div class="row">
    <div class="col-md-12">
        <c:choose>
            <c:when test="${empty pagedListHolder.pageList}">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Database Report List</h3>
                    </div>
                    <div class="panel-body">
                        <p>None found.</p>
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <form:form commandName="command">
                    <element:pagedList pagedListHolder="${pagedListHolder}" pagedLink="${pagedLink}" />

                    <div class="spacer"><!--  --></div>
                    <table class="table table-condensed table-bordered table-striped table-hover">
                        <thead>
                        <tr>
                            <th>title</th>
                            <th>report ID</th>
                            <th>run date</th>
                            <th>view report</th>
                            <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                            <th>select</th>
                            <% } %>
                        </tr>
                        </thead>
                            <%-- // show only current page worth of data --%>
                        <c:forEach items="${pagedListHolder.pageList}" var="report">
                            <tr>
                                <td>${report.title}</td>
                                <td>${report.reportId}</td>
                                <td>${report.date}</td>
                                <td>
                                    <c:if test="${empty formatMap[report.reportId]}">
                                        <a href="report/database/downloadReport.htm?fileName=${report.location}">Download</a>
                                    </c:if>
                                    <c:forEach items='${formatMap[report.reportId]}' var="format">
                                        <a href="report/database/downloadReport.htm?locatorId=${report.id}&format=${format}">${format}</a>
                                    </c:forEach>
                                </td>
                                <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                                <td><form:checkbox path="ids" value="${report.id}"/></td>
                                <% } %>
                            </tr>
                        </c:forEach>
                    </table>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                        <div class="pagination">
                            <a onClick="toggle(true, 'ids')">Select all</a> /
                            <a onClick="toggle(false, 'ids')">Deselect all</a>
                        </div>
                    <% } %>

                    <% // if deletion was successful %>
                    <c:if test="${not empty success}">
                        <div class="alert-success" style="clear:both">
                                ${success}
                        </div>
                    </c:if>

                    <% // If user is not allowed to delete %>
                    <c:if test="${not empty error}">
                        <div class="alert-error" style="clear:both">
                                ${error}
                        </div>
                    </c:if>
                    <% if (!request.isUserInRole(Authentication.ROLE_READONLY)) { %>
                    <input class="btn btn-default" type="submit" value="delete checked reports"/>
                    <% } %>

                </form:form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

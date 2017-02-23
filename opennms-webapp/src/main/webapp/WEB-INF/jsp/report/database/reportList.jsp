<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib tagdir="/WEB-INF/tags/element" prefix="element" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="List Reports"/>
    <jsp:param name="headTitle" value="List Reports"/>
    <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>"/>
    <jsp:param name="breadcrumb" value="<a href='report/database/index.htm'>Database</a>"/>
    <jsp:param name="breadcrumb" value="List Reports"/>
</jsp:include>

<c:choose>
    <c:when test="${empty repositoryList}">
        <p>No repositories with reports available.</p>
    </c:when>

    <c:otherwise>
        <c:forEach var="mapEntry" items="${repositoryList}">
            <c:url value="/report/database/reportList.htm" var="pagedLink">
                <c:param name="p_${mapEntry.key.id}" value="~"/>
            </c:url>

                <div class="row">
                    <div class="col-md-12">
                        <div class="text-right">
                            <element:pagedList pagedListHolder="${mapEntry.value}" pagedLink="${pagedLink}"/>
                        </div>

                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h3 class="panel-title"><c:out value="${mapEntry.key.displayName}"/></h3>
                        </div>
                        <table class="table table-condensed">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                                <th colspan="3" style="width: 1px; text-align: center;">Action</th>
                            </tr>
                            </thead>
                                <%-- // show only current page worth of data --%>
                            <c:forEach items="${mapEntry.value.pageList}" var="report">
                                <tr>
                                    <td width="25%">${report.displayName}</td>
                                    <td>${report.description}</td>
                                    <c:choose>
                                        <c:when test="${report.allowAccess}">
                                            <c:choose>
                                                <c:when test="${report.isOnline}">
                                                    <td class="o-report-online"><a
                                                            href="report/database/onlineReport.htm?reportId=${report.id}"
                                                            title="Execute this report instantly" id="online-${report.id}"/></td>
                                                </c:when>
                                                <c:otherwise>
                                                    <td>&nbsp;</td>
                                                </c:otherwise>
                                            </c:choose>
                                            <td class="o-report-deliver"><a
                                                    href="report/database/batchReport.htm?reportId=${report.id}&schedule=false"
                                                    title="Deliver report to file system or via e-mail" id="deliver-${report.id}"/></td>
                                            <td class="o-report-schedule"><a
                                                    href="report/database/batchReport.htm?reportId=${report.id}&schedule=true"
                                                    title="Create a schedule for this report" id="schedule-${report.id}"/></td>
                                        </c:when>
                                        <c:otherwise>
                                            <td colspan="3" class="o-report-subscribe"><a href="${mapEntry.key.managementUrl}"
                                                                                       id="o-report-subscribe">Get this report!</a>
                                            </td>
                                        </c:otherwise>
                                    </c:choose>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                </div>
            </div>

        </c:forEach>
    </c:otherwise>
</c:choose>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

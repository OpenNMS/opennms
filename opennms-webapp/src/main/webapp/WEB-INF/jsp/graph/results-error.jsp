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

<%@page language="java"
        contentType="text/html"
        session="true"
        %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:import url="/includes/bootstrap.jsp">
    <c:param name="title" value="Resource Graph Results" />
    <c:param name="headTitle" value="Results" />
    <c:param name="headTitle" value="Resource Graphs" />
    <c:param name="headTitle" value="Reports" />
    <c:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
    <c:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>"/>
    <c:param name="breadcrumb" value="Results" />
    <c:param name="scrollSpy" value="#results-sidebar" />
    <c:param name="meta"       value="<meta http-equiv='X-UA-Compatible' content='IE=Edge' />"/>
    <c:param name="renderGraphs" value="true" />
</c:import>

<div class="row">
  <div class="col-md-12 text-center">
    <h4>Can't generate graphs. Maybe one of the provided resource IDs don't have metrics associated.</h4>
  </div> <!-- column -->
</div> <!-- row -->

<c:if test="${showFootnote1 == true}">
    <jsp:include page="/includes/footnote1.jsp" flush="false" />
</c:if>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

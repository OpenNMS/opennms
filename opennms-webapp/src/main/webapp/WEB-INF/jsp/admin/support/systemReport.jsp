<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="System Reports" />
    <jsp:param name="headTitle" value="System Reports" />
    <jsp:param name="breadcrumb" value="<a href='support/index.htm'>Support</a>" />
    <jsp:param name="breadcrumb" value="System Reports" />
    <jsp:param name="breadcrumb" value="${report.type}" />
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Plugins</h3>
  </div>
  <div class="panel-body">
    <p>Choose which plugins to enable:</p>
    <c:forEach items="${report.plugins}" var="plugin">
     <c:out value="using plugin ${plugin.name}" />
    </c:forEach>
  </div>
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

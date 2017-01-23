<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true"
        import="java.util.*,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation"%>

<%@ page import="com.google.common.base.Strings" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="element" tagdir="/WEB-INF/tags/element" %>

<c:if test="${model.nodeCount == 1 && command.snmpParm == null && command.maclike == null}">
  <jsp:forward page="/element/node.jsp?node=${model.nodes[0].node.id}"/>
</c:if>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="headTitle" value="Node List" />
  <jsp:param name="location" value="nodelist" />
  <jsp:param name="breadcrumb" value="<a href ='element/index.jsp'>Search</a>"/>
  <jsp:param name="breadcrumb" value="Node List"/>
</jsp:include>


<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Nodes and their interfaces</h3>
  </div> <!-- panel-heading -->
  <div class="panel-body">
    <jsp:include page="/geolocation/box.jsp"/>
  </div> <!-- panel-body -->
</div> <!-- panel -->


<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

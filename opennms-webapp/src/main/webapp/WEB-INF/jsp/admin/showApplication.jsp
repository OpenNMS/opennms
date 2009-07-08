<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Application" />
	<jsp:param name="headTitle" value="Application" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/applications.htm'>Applications</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Application ${model.application.name}</h3>

<p>
Application '${model.application.name}' has ${fn:length(model.memberServices)} services (<a href="admin/applications.htm?edit=edit&applicationid=${model.application.id}">edit</a>)
</p>


<table>
  <tr>
    <th>Node</th>
    <th>Interface</th>
    <th>Service</th>
  </tr>
  <c:forEach items="${model.memberServices}" var="service">
    <tr>
    	<td><a href="element/node.jsp?node=${service.ipInterface.node.id}">${service.ipInterface.node.label}</a></td> 
    	<td><a href="element/interface.jsp?ipinterfaceid=${service.ipInterface.id}">${service.ipInterface.ipAddress}</a></td> 
    	<td><a href="element/service.jsp?ifserviceid=${service.id}">${service.serviceName}</a></td> 
    </tr>
  </c:forEach>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>
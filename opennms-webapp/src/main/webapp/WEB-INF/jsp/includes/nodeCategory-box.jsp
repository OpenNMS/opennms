<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
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

<%@page language="java"
	contentType="text/html"
	session="true"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<div id="category-box">

<h3 class="o-box">
  Surveillance Category Memberships
  <c:if test="${isAdmin == 'true'}">
    (<a href="<c:url value='admin/categories.htm?edit&node=${param.node}'/>">Edit</a>)
  </c:if>
</h3>

<table class="o-box">
  <c:if test="${empty categories}">
    <tr>
      <td>This node is not a member of any categories</td>
    </tr>
  </c:if>
  

  <c:forEach items="${categories}" var="category">
    <tr>
      <td>${category.name}</td>
    </tr>
  </c:forEach>
</table>   

</div>

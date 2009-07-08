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

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Categories" />
	<jsp:param name="headTitle" value="Categories" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Categories" />
</jsp:include>

<h3>Surveillance Categories</h3>

<table>
  <tr>
    <th>Delete</th>
    <th>Edit</th>
    <th>Category</th>
  </tr>
  <c:forEach items="${categories}" var="category">
	  <tr>
	    <td><a href="admin/categories.htm?removeCategoryId=${category.id}"><img src="images/trash.gif" alt="Delete Category"/></a></td>
	    <td><a href="admin/categories.htm?categoryid=${category.id}&edit"><img src="images/modify.gif" alt="Edit Category"/></a></td>
	    <td><a href="admin/categories.htm?categoryid=${category.id}">${category.name}</a></td> 
  	  </tr>
  </c:forEach>
  <tr>
    <td></td>
    <td></td>
    <td>
      <form action="admin/categories.htm">
        <input type="textfield" name="newCategoryName" size="40"/>
        <input type="submit" value="Add New Category"/>
      </form>
  </tr>
</table>


<jsp:include page="/includes/footer.jsp" flush="false"/>

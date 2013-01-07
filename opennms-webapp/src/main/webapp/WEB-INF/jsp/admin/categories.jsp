<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.api.Util" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Categories" />
	<jsp:param name="headTitle" value="Categories" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Categories" />
</jsp:include>

<h3>Surveillance Categories</h3>
<script type="text/javascript">

   var surveillanceCategories = {
		   <c:set var="first" value="true"/>
		   <c:forEach var="surveillanceCat" items="${surveillanceCategories}" varStatus="count">
		     <c:choose>
		       <c:when test="${first == true}">
		         <c:set var="first" value="false" />
		         "${surveillanceCat}":"${surveillanceCat}"
		       </c:when>
		       <c:otherwise>
		         ,"${surveillanceCat}":"${surveillanceCat}"
		       </c:otherwise>
		     </c:choose>
		   </c:forEach>
   };

   function deleteCategory(categoryName, categoryId){
	   if(surveillanceCategories.hasOwnProperty(categoryName)){
           if(confirm("This Surveillance Category is also in your surveillance-views.xml config.\nPlease edit surveillance-views.xml to reflect changes.")){
               location = "<%= Util.calculateUrlBase(request, "admin/categories.htm") %>?removeCategoryId=" + categoryId;
           }
       }else{
           location = "<%= Util.calculateUrlBase(request, "admin/categories.htm") %>?removeCategoryId=" + categoryId;
       }
   }
</script>
<table>
  <tr>
    <th>Delete</th>
    <th>Edit</th>
    <th>Category</th>
  </tr>
  <c:forEach items="${categories}" var="category">
	  <tr>
	    <td><a onclick="deleteCategory('${fn:escapeXml(category.name)}', ${category.id})" ><img src="images/trash.gif" alt="Delete Category"/></a></td>
	    <td><a href="admin/categories.htm?categoryid=${category.id}&edit"><img src="images/modify.gif" alt="Edit Category"/></a></td>
	    <td><a href="admin/categories.htm?categoryid=${category.id}">${fn:escapeXml(category.name)}</a></td> 
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

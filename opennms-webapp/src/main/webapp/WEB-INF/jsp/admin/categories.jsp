<%@ page contentType="text/html;charset=UTF-8" language="java"%>
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Applications" />
	<jsp:param name="headTitle" value="Applications" />
	<jsp:param name="breadcrumb"
		value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="Applications" />
</jsp:include>

<h3>Applications</h3>

<table>
  <tr>
    <th>Delete</th>
    <th>Edit</th>
    <th>Application</th>
  </tr>
  <c:forEach items="${applications}" var="app">
	  <tr>
	    <td><a href="admin/applications.htm?removeApplicationId=${app.id}"><img src="images/trash.gif" alt="Delete Application"/></a></td>
	    <td><a href="admin/applications.htm?applicationid=${app.id}&edit=edit"><img src="images/modify.gif" alt="Edit Application"/></a></td>
	    <td><a href="admin/applications.htm?applicationid=${app.id}">${fn:escapeXml(app.name)}</a></td> 
  	  </tr>
  </c:forEach>
  <tr>
    <td></td>
    <td></td>
    <td>
      <form action="admin/applications.htm">
        <input type="textfield" name="newApplicationName" size="40"/>
        <input type="submit" value="Add New Application"/>
      </form>
  </tr>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

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
    <th></th>
    <th></th>
    <th>Application</th>
  </tr>
  <c:forEach items="${applications}" var="app">
	  <tr>
	    <td><a href="#"><img src="images/trash.gif" alt="Delete Application"/></a></td>
	    <td><a href="#"><img src="images/modify.gif" alt="Edit Application"/></a></td>
	    <td><c:out value="${app.name}" /></td>
  	  </tr>
  </c:forEach>
</table>


<jsp:include page="/includes/footer.jsp" flush="false"/>

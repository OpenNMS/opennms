<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Review Report" />
  <jsp:param name="headTitle" value="Review Report" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<h3>Review Report</h3>

	  <p>You are about to run the following report:</p>
	  <table>
	  		<td>Report Name</td>
			<td><c:out value="${criteria.displayName}"/></td>
	  <%-- // date fields --%>
	  <c:forEach items="${criteria.dates}" var="date" >
			<tr>
				<td><c:out value="${date.displayName}"/></td>
				<td><c:out value="${date.date}"/></td>
			</tr>
	  </c:forEach>
	  <%-- // category fields --%>
		<c:forEach items="${criteria.categories}" var="category" >
			<tr>
				<td><c:out value="${category.displayName}"/></td>
				<td><c:out value="${category.category}"/></td>
            </tr>
		</c:forEach>
	  </table>
	  <br>
	  
<form:form>  
    <input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
	<input type="submit" name="_eventId_revise" value="Revise"/>&#160;
	<input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;
</form:form>

<jsp:include page="/includes/footer.jsp" flush="false" />
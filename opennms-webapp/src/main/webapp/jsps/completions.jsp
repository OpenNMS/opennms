<%@ page language="java" contentType="text/html" session="true"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<ul>
<c:forEach var="name" items="${matches}">
	<li><c:out value="${name}"/></li>
</c:forEach>
</ul>

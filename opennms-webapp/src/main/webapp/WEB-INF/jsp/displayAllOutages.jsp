<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>

<html>
<head>
<title><fmt:message key="title" /></title>
</head>
<body>


<h1><fmt:message key="now" /></h1>

<b> That was the whole collection </b>

<c:out value="${status.errorMessage}" />

The time is now <br>

<c:out value="${now}" />

<c:forEach items="${outages}" var="outageId">
	<c:out value="${outageId}" />
	<br>
	<br>
</c:forEach>

</body>
</html>


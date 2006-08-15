<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@page import="java.util.Collection"%>
<%@page import="org.opennms.netmgt.model.AggregateStatusView"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Aggregate Status Page</title>
</head>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Current Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb" value="Current Outages By Node" />
</jsp:include>

<body>

<h1> Aggregated Status </h1>

<h2><c:out value="${view.label}" /></h2> <br>

<c:forEach items="${stati}" var="status">
    <c:out value="${status.label}" />
    <c:out value="${status.downEntityCount}" /> of 
    <c:out value="${status.totalEntityCount}" /> <br>
</c:forEach>

</body>
<jsp:include page="/includes/footer.jsp" flush="false" />
</html>
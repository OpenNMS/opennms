<?xml version="1.0" encoding="ISO-8859-1" ?>
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0">
    <jsp:directive.page language="java"
        contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" />
    <jsp:text>
        <![CDATA[ <?xml version="1.0" encoding="ISO-8859-1" ?> ]]>
    </jsp:text>
    <jsp:text>
        <![CDATA[ <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
    </jsp:text>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
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


<c:forEach items="${stati}" var="status">
    <c:out value="${status}" />
</c:forEach>

</body>
<jsp:include page="/includes/footer.jsp" flush="false" />
</html>
</jsp:root>
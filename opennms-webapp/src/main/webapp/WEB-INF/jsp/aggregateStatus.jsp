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
	<jsp:param name="title" value="Aggregate Status Page" />
	<jsp:param name="headTitle" value="AggregateStatus" />
	<jsp:param name="breadcrumb" value="Aggregated Status" />
</jsp:include>

<body>
<h1 align="center">Site Status</h1>

<div id="index-contentmiddle">


<h3> <c:out value="${view.columnValue}" /></h3>

  <table>
    <thead>
      <tr>
        <th>Device Type</th>
        <th>Nodes Down</th>
      </tr>
    </thead>
    <c:forEach items="${stati}" var="status">
      <tr>
        <td><c:out value="${status.label}" /></td>
        <td class="<c:out value='${status.status}'/>" ><c:out value="${status.downEntityCount}" /> of <c:out value="${status.totalEntityCount}" /></td>
      </tr>
    </c:forEach>
  </table>

</div>

</body>
<jsp:include page="/includes/footer.jsp" flush="false" />
</html>
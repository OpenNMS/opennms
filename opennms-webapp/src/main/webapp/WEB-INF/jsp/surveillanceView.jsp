<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%@page import="java.util.Collection"%>
<%@page import="org.opennms.web.svclayer.SurveillanceTable"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>OpenNMS Surveillance View Page</title>
</head>
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Surveillance View" />
	<jsp:param name="headTitle" value="Surveillance" />
	<jsp:param name="breadcrumb" value="Surveillance" />
</jsp:include>

<body>
<h1 align="center">Surveillance View</h1>

<div id="index-contentmiddle">

<h3> <c:out value="${table.label}" /></h3>

  <table>
    <thead>
      <tr>
        <th>Nodes Down</th>
        <c:forEach items="{$table.columnHeaderList}" var="header">
          <th><c:out value="${header}" /></th>
        </c:forEach>
      </tr>
    </thead>
  </table>
  
</div>
</body>


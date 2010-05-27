<%@page language="java"
	contentType="text/html; charset=utf-8"
	session="true"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:choose>
<c:when test="${param.docType == 'html'}">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
</c:when>
<c:otherwise>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
</c:otherwise>
</c:choose>

<head>
  <title>
    <c:forEach var="headTitle" items="${paramValues.headTitle}">
      <c:out value="${headTitle}" escapeXml="false"/> |
    </c:forEach>
    OpenNMS Web Console
  </title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta http-equiv="Content-Style-Type" content="text/css"/>
  <meta http-equiv="Content-Script-Type" content="text/javascript"/>

  <c:forEach var="script" items="${paramValues.script}">
    <c:out value="${script}" escapeXml="false" />
  </c:forEach>
</head>

<%-- The <body> tag is unmatched in this file (its matching tag is in the
     footer), so we hide it in a JSP code fragment so the Eclipse HTML
     validator doesn't complain.  See bug #1728. --%>
<%= "<body>" %>

<div id="header">
<h1>[Header Placeholder]</h1>
</div>
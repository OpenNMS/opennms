<%@ attribute name="node" type="java.lang.Object" rtexprvalue="true" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="property" rtexprvalue="false" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<form:label path="${property}">${label}</form:label>
<c:set var="suffixed" value="${editingNode}." scope="page" />
<c:choose>
  <c:when test="${nestedPath == suffixed}">
    <form:input path="${property}" /> 
  </c:when>
  <c:otherwise>
    <form:input cssStyle="border:0; background: lightgrey" path="${property}" readonly="true" /> 
  </c:otherwise>
</c:choose>

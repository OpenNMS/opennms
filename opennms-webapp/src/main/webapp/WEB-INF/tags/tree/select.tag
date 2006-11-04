<%@ attribute name="node" type="java.lang.Object" rtexprvalue="true" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="property" required="true" %>
<%@ attribute name="items" type="java.lang.Object" rtexprvalue="true" required="true" %>
<%@ attribute name="itemLabel" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>


<form:label path="${property}">${label}</form:label>
<c:set var="suffixed" value="${editingNode}." scope="page" />
<c:choose>
  <c:when test="${nestedPath == suffixed}">
    <form:select path="${property}" itemLabel="${itemLabel}" items="${items}" />
  </c:when>
  <c:otherwise>
    <form:input cssStyle="border:0; background: lightgrey" path="${property}" readonly="true" /> 
  </c:otherwise>
</c:choose>



<%@ attribute name="label" required="true" %>
<%@ attribute name="property" required="true" %>
<%@ attribute name="items" type="java.lang.Object" rtexprvalue="true" required="true" %>
<%@ attribute name="itemLabel" required="false" %>
<%@ attribute name="fieldSize" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>


<form:label path="${property}">${label}</form:label>
<c:set var="nestedPathSansDot" value="${fn:substring(nestedPath, 0, fn:length(nestedPath)-1)}" scope="page" />
<c:choose>
  <c:when test="${nestedPathSansDot == treeFormModel.currentNode}">
    <c:choose>
      <c:when test="${empty itemLabel}" >
        <form:select path="${property}" items="${items}" />
      </c:when>
      <c:otherwise>
        <form:select path="${property}" itemLabel="${itemLabel}" items="${items}" />
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <form:input cssStyle="border:0; background: lightgrey" size="${!(empty fieldSize)? fieldSize : 48}" path="${property}" readonly="true" /> 
  </c:otherwise>
</c:choose>



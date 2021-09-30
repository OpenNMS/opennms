<%@ attribute name="label" required="true" %>
<%@ attribute name="property" rtexprvalue="false" required="true" %>
<%@ attribute name="size" rtexprvalue="false" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>


<form:label path="${property}">${label}</form:label>
<c:set var="nestedPathSansDot" value="${fn:substring(nestedPath, 0, fn:length(nestedPath)-1)}" scope="page" />
<c:set var="inputSize" value="${size != null ? size : 16}"/>
<c:choose>
  <c:when test="${nestedPathSansDot == treeFormModel.currentNode}">
    <form:input path="${property}" size="${inputSize}"/>
  </c:when>
  <c:otherwise>
    <form:input cssStyle="border:0; background: lightgrey" path="${property}" readonly="true" size="${inputSize}"/> 
  </c:otherwise>
</c:choose>

<%@ tag body-content="scriptless" %>
<%@ attribute name="path" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
  <c:when test="${empty nestedPath}">
    <c:set value="${path}" var="nestedPath" scope="request" />
  </c:when>
  <c:otherwise>
    <c:set value="${nestedPath}" var="savedNestedPath" scope="page" />
    <c:set value="${nestedPath}.${path}" var="nestedPath" scope="request" />
  </c:otherwise>
</c:choose>
<jsp:doBody/>
<c:set value="${savedNestedPath}" var="nestedPath" scope="request"/>


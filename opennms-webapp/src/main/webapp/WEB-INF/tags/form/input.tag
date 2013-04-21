<%@ tag dynamic-attributes="attrMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<input size="48"
  <c:forEach var="attr" items="${attrMap}">
    <c:choose>
      <c:when test="${attr.key=='disabled'}">
        <c:if test="${attr.value == true}">
          disabled="disabled"
        </c:if>
      </c:when>
      <c:otherwise>
        ${attr.key}="${attr.value}"
      </c:otherwise>
    </c:choose>
  </c:forEach>
>

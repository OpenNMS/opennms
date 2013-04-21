<%@ tag dynamic-attributes="attrMap" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<option size="48"
  <c:forEach var="attr" items="${attrMap}">
    <c:choose>
      <c:when test="${attr.key=='selected'}">
        <c:if test="${attr.value == true}">
          selected="selected"
        </c:if>
      </c:when>
      <c:otherwise>
        ${attr.key}="${attr.value}"
      </c:otherwise>
    </c:choose>
  </c:forEach>
>
<jsp:doBody/>
</option>

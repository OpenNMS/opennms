<%@ tag dynamic-attributes="attrMap" %>
<%@ attribute name="disabled" required="false" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<input ${disabled ? 'disabled="disabled"' : ""}
  <c:forEach var="attr" items="${attrMap}">${attr.key}="${attr.value}"</c:forEach>
>
<jsp:doBody/>
</option>


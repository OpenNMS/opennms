<%@ tag dynamic-attributes="attrMap" %>
<%@ attribute name="selected" required="false" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<option ${selected ? 'selected="selected"' : ""}
  <c:forEach var="attr" items="${attrMap}">${attr.key}="${attr.value}"</c:forEach>
>
<jsp:doBody/>
</option>


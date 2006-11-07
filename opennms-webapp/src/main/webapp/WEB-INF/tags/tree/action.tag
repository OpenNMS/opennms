<%@ attribute name="label" required="true" %>
<%@ attribute name="action" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<c:set var="nestedPathSansDot" value="${fn:substring(nestedPath, 0, fn:length(nestedPath)-1)}" scope="page" />
<c:if test="${nestedPathSansDot != treeFormModel.currentNode}">
  <a href="javascript:submitTreeForm('${treeFormName}', '${nestedPathSansDot}', '${action}')">${label}</a>
</c:if>


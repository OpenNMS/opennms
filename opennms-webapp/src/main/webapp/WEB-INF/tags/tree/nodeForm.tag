
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<c:set var="nestedPathSansDot" value="${fn:substring(nestedPath, 0, fn:length(nestedPath)-1)}" scope="page" />
<c:choose>
  <c:when test="${nestedPathSansDot == treeFormModel.currentNode}">
    <spring:bind ignoreNestedPath="true" path="${treeFormName}.currentNode">
	    <input type="hidden" name="${status.expression}" value="${treeFormModel.currentNode}" />
    </spring:bind>
    <jsp:doBody /> 
    <input type="button" value="Save" onclick="javascript:submitTreeForm('${treeFormName}', '${nestedPathSansDot}', 'save')"/>
    <input type="button" value="Cancel" onclick="javascript:submitTreeForm('${treeFormName}', '${nestedPathSansDot}', 'cancel')"/>
  </c:when>
  <c:otherwise>

	<c:if test="${empty showDelete || showDelete == true}">
      <a href="javascript:submitTreeForm('${treeFormName}', '${nestedPathSansDot}', 'delete')"><img src="images/trash.gif"/></a>
    </c:if>
    
    <a href="javascript:submitTreeForm('${treeFormName}', '${nestedPathSansDot}', 'edit')"><img src="images/modify.gif"/></a>
    
    <jsp:doBody /> 
  </c:otherwise> 
</c:choose>

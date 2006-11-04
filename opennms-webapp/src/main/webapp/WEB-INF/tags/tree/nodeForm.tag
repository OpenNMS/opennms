
<%@ attribute name="root" type="java.lang.Object" rtexprvalue="true" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>


<c:set var="suffixed" value="${editingNode}." scope="page" />
<c:choose>
  <c:when test="${nestedPath == suffixed}">
    <input type="hidden" value="${editingNode}" />
    <jsp:doBody /> 
    <input type="button" value="Save" onclick="javascript:submitTreeForm('${org_opennms_web_treeFormName}', '${nestedPath}', 'save')"/>
  </c:when>
  <c:otherwise>
  
    <a href="javascript:submitTreeForm('${org_opennms_web_treeFormName}', '${nestedPath}', 'delete')"><img src="images/trash.gif"/></a>
    
    <c:url var="editUrl" value="/admin/import.htm" scope="page">
      <c:param name="path" value="${nestedPath}"/>
      <c:param name="action" value="edit"/>
    </c:url>
    <a href="javascript:submitTreeForm('${org_opennms_web_treeFormName}', '${nestedPath}', 'edit')"><img src="images/modify.gif"/></a>
    
    <jsp:doBody /> 
  </c:otherwise> 
</c:choose>

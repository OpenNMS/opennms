<%@ attribute name="node" type="java.lang.Object" rtexprvalue="true" required="true" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="action" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<c:set var="suffixed" value="${editingNode}." scope="page" />
<c:if test="${nestedPath != suffixed}">
  <a href="javascript:submitTreeForm('${org_opennms_web_treeFormName}', '${nestedPath}', '${action}' })">${label}</a>
</c:if>


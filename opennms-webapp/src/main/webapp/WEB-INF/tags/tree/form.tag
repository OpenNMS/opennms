
<%@ attribute name="commandName" type="java.lang.Object" rtexprvalue="true" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<script language="Javascript" type="text/javascript" >
	function submitTreeForm(formName, target, action) {
	    document[formName].target.value = target;
	    document[formName].action.value = action;
		document[formName].submit();
	}
</script>


<form:form commandName="${commandName}" name="${commandName}"> 
  <input type="hidden" name="target" />
  <input type="hidden" name="action" />
  <c:set var="org_opennms_web_treeFormName" value="${commandName}" scope="request" />
  
  <jsp:doBody />
</form:form>

<%@ attribute name="commandName" type="java.lang.Object" rtexprvalue="true" required="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<script type="text/javascript" >
	function submitTreeForm(formName, target, action) {
	    document[formName].formPath.value = target;
	    document[formName].action.value = action;
		document[formName].submit();
	}
</script>


<form:form commandName="${commandName}" name="${commandName}"> 
  
  <c:set var="treeFormName" value="${commandName}" scope="request" />
  <c:set var="treeFormModel" value="${requestScope[commandName]}" scope="request" />
  
  <form:hidden path="formPath"/>
  <form:hidden path="action" />

  <spring:nestedPath path="formData">

    <jsp:doBody />
  
  </spring:nestedPath>
</form:form>
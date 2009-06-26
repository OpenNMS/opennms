<%@ attribute name="label" required="true" %>
<%@ attribute name="property" rtexprvalue="false" required="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>


<form:label path="${property}">${label}</form:label>
<c:set var="nestedPathSansDot" value="${fn:substring(nestedPath, 0, fn:length(nestedPath)-1)}" scope="page" />
<form:input cssStyle="border:0; background: lightgrey" path="${property}" readonly="true" /> 

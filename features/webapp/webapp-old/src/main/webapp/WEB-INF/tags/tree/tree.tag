
<%@ attribute name="root" type="java.lang.Object" rtexprvalue="true" required="false" %>
<%@ attribute name="childProperty" rtexprvalue="false" required="true" %>
<%@ attribute name="var" rtexprvalue="false" required="true" %>
<%@ attribute name="varStatus" rtexprvalue="false" required="true" %>
<%@ variable name-from-attribute="var" alias="child" variable-class="java.lang.Object" scope="NESTED" %>
<%@ variable name-from-attribute="varStatus" alias="childStatus" variable-class="java.lang.Object" scope="NESTED" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<c:set var="node" value="${empty root ? parent : root}" /> 
<c:set var="children" value="${node[childProperty]}" />

<ul>
  <c:forEach items="${children}" var="child" varStatus="childStatus">
    <li>
	  <spring:nestedPath path="${childProperty}[${childStatus.index}]" >
	    <jsp:doBody/>
      </spring:nestedPath>
    </li>
  </c:forEach>
</ul>
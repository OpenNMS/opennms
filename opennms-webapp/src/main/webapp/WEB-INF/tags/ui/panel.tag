<%@ attribute name="title" required="true" %>
<%@ attribute name="showHeader" required="false" %>
<%@ attribute name="link" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="o-box">
<c:choose>
	<c:when test="${showHeader == 'true'}">
		<c:if test="${!empty link}">
		<h3 class="o-box-header"><a href="${link}">${title}</a></h3>
		</c:if>
		<c:if test="${empty link}">
		<h3 class="o-box-header">${title}</h3>
		</c:if>
		<div class="o-box-spacer">
		  <jsp:doBody/>
		</div>
	</c:when>
    <c:otherwise>
        <jsp:doBody/>
    </c:otherwise>
</c:choose>

</div>
<%@ attribute name="title" required="true" %>
<%@ attribute name="showHeader" required="false" %>
<%@ attribute name="link" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags/ui" %>

<c:choose>
	<c:when test="${showHeader == 'true'}">
		<div class="o-box">
		   <ui:onms-header title="${title}" link="${link}"/>
			<div class="o-box-spacer">
			  <jsp:doBody/>
			</div>
	    </div>
	</c:when>
    <c:otherwise>
	    <div class="o-box slim">
	        <jsp:doBody/>
	    </div>
    </c:otherwise>
</c:choose>


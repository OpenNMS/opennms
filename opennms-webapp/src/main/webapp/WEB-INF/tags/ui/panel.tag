<%@ attribute name="title" required="true" %>
<%@ attribute name="showHeader" required="false" %>
<%@ attribute name="noPadding" required="false" description="true or false adds padding around contents"%>
<%@ attribute name="link" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags/ui" %>

<div class="o-box">
<div class="border">
<c:if test="${showHeader == 'true'}">
   <ui:onms-header title="${title}" link="${link}"/>
</c:if>
<c:choose>
    <c:when test="${noPadding == 'true'}">
        <div class="slim">
            <jsp:doBody/>
        </div>
    </c:when>
    <c:otherwise>
        <div class="o-box-spacer">
            <jsp:doBody/>
        </div>
    </c:otherwise>
</c:choose>
</div>
</div>
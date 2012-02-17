<%@ attribute name="title" required="true"%>
<%@ attribute name="link" required="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${!empty link}">
    <h3 class="o-box-header"><a href="${link}">${title}</a></h3>
</c:if>
        
<c:if test="${empty link}">
        <h3 class="o-box-header">${title}</h3>
</c:if>

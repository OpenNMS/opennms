<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="navbar">
<ul>
	<c:forEach var="entry" items="${model.entries}">
		<c:choose>
			<c:when test="${entry.name == 'Help'}">
			    <c:set var="liClass">class="last"</c:set>
			</c:when>
			<c:otherwise>
			    <c:set var="liClass"></c:set>
			</c:otherwise>
		</c:choose>
		
		<li ${liClass}>
		<c:choose>
			<c:when test="${model.location == entry.locationMatch}">
				<c:out value="${entry.name}" />
			</c:when>
			<c:otherwise>
				<a href="<c:out value="${entry.URL}"/>"><c:out
					value="${entry.name}" /></a>
			</c:otherwise>
		</c:choose>
		</li>
	</c:forEach>
</ul>
</div>
<!-- id="navbar" -->

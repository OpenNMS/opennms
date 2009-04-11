<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Logoff" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

<div class="formOnly">
	<h2><spring:message code="logoff.notice"/></h2>
	<p><spring:message code="logoff.loginagain" arguments="<a href='acegilogin.jsp'>"/></p>
</div>
<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

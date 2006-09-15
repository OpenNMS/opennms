<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Logoff" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

<div class="formOnly">
	<h2>You have been logged off.</h2>
	<p>You may <a href="acegilogin.jsp"><strong>log in</strong></a> again.</p>
</div>
<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />
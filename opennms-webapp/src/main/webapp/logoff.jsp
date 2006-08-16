<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Logoff" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

You have been logged off.

You may <a href="acegilogin.jsp"> log in again</a>.

<jsp:include page="/includes/footer.jsp" flush="false">
  <jsp:param name="quiet" value="true" />
</jsp:include>

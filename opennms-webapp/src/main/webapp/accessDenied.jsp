<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%--
<%@ page import="org.springframework.security.ui.AbstractProcessingFilter" %>
<%@ page import="org.springframework.security.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.AuthenticationException" %>
--%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Access denied" />
</jsp:include>

<h2>Access denied</h2>

<p>
You do not have permission to access this page.
</p>

<jsp:include page="/includes/footer.jsp" flush="false"/>

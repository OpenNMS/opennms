<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%--
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>
--%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value='<spring:message code="header.accessdenied"/>' />
</jsp:include>

<h2><spring:message code="accessdenied.title"/></h2>

<p>
<spring:message code="accessdenied.message"/>
</p>

<jsp:include page="/includes/footer.jsp" flush="false"/>

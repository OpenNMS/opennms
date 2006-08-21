<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="org.acegisecurity.ui.AbstractProcessingFilter" %>
<%@ page import="org.acegisecurity.ui.webapp.AuthenticationProcessingFilter" %>
<%@ page import="org.acegisecurity.AuthenticationException" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Login" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>
	
    <%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
    <c:if test="${not empty param.login_error}">
      <p style="color:red;">
        <strong>Your log-in attempt failed, please try again</strong></p>
      <p>Reason: <%= ((AuthenticationException) session.getAttribute(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %></p>
    </c:if>

    <div style="width:250px; text-align:right; margin:50px;">
    <form action="<c:url value='j_acegi_security_check'/>" method="POST">
        <p>User: <input type='text' name='j_username' <c:if test="${not empty param.login_error}">value='<%= session.getAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY) %>'</c:if> /><br /> 
           Password: <input type='password' name='j_password'></p>
        <p><input type="checkbox" name="_acegi_security_remember_me"> Don't ask for my password for two weeks</p>
			<input name="reset" type="reset" value="Reset" />
			<input name="Get in" type="submit" value="Get in" />
    </form>
    </div>
    <hr />
<jsp:include page="/includes/footer.jsp" flush="false" />

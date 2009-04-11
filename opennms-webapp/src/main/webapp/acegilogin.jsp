<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core'%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value='<spring:message code="login.login"/>' />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

<%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
<c:if test="${not empty param.login_error}">
  <p style="color:red;">
    <strong><spring:message code="login.failed"/></strong>
  </p>

  <%-- This is: AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY --%>
  <p>Reason: ${ACEGI_SECURITY_LAST_EXCEPTION.message}</p>
</c:if>

<div class="formOnly">
  <form action="<c:url value='j_acegi_security_check'/>" method="POST">
    <p>
      <spring:message code="login.user"/><input type="text" id="input_j_username" name="j_username" <c:if test="${not empty param.login_error}">value='<c:out value="${ACEGI_SECURITY_LAST_USERNAME}"/>'</c:if> /><br />
      <spring:message code="login.password"/><input type='password' name='j_password'>
    </p>
      
    <!--
    <p><input type="checkbox" name="_acegi_security_remember_me"> Don't ask for my password for two weeks</p>
    -->
    
    <input name="reset" type="reset" value='<spring:message code="login.reset"/>' />
    <input name="Login" type="submit" value='<spring:message code="login.login"/>' />

    <script type="text/javascript">
      if (document.getElementById) {
        document.getElementById('input_j_username').focus();
      }
    </script>
  
  </form>
</div>

<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

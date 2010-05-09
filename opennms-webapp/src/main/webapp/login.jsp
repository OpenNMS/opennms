<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core'%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Login" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

<%-- this form-login-page form is also used as the 
         form-error-page to ask for a login again.
         --%>
<c:if test="${not empty param.login_error}">
  <p style="color:red;">
    <strong>Your log-in attempt failed, please try again</strong>
  </p>

  <%-- This is: AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY --%>
  <p>Reason: ${SPRING_SECURITY_LAST_EXCEPTION.message}</p>
</c:if>

<div class="formOnly">
  <form action="<c:url value='j_spring_security_check'/>" method="post">
    <p>
      User: <input type="text" id="input_j_username" name="j_username" <c:if test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:if> /><br />
      Password: <input type='password' name='j_password'>
    </p>
      
    <!--
    <p><input type="checkbox" name="_spring_security_remember_me"> Don't ask for my password for two weeks</p>
    -->
    
    <input name="reset" type="reset" value="Reset" />
    <input name="Login" type="submit" value="Login" />

    <script type="text/javascript">
      if (document.getElementById) {
        document.getElementById('input_j_username').focus();
      }
    </script>
  
  </form>
</div>

<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

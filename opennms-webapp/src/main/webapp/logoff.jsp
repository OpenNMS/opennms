<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Log out" />
  <jsp:param name="nonavbar" value="true" />
</jsp:include>

<div class="formOnly">
	<h2>You have been logged out.</h2>
	<p>You may <a href="login.jsp"><strong>log in</strong></a> again.</p>
</div>
<hr />

<jsp:include page="/includes/footer.jsp" flush="false" />

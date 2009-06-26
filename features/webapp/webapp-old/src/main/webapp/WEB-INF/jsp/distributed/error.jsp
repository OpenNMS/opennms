<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Error" />
</jsp:include>

<h3><spring:message code="error"/></h3>
<div class="boxWrapper">
  <ul class="error">
    <c:forEach var="err" items="${errors.allErrors}">
      <li><spring:message message="${err}"/></li>
    </c:forEach>
  </ul>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>

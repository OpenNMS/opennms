<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="<c:out vlaue="${phaseLabel}"/>" />
	<jsp:param name="headTitle" value="<c:out value="${phaseLabel}"/>" />
	<jsp:param name="meta" value="<meta http-equiv='refresh' content='3'/>" />
</jsp:include>

<div id="index-contentmiddle">

  <h2><c:out value="${phaseLabel}"/></h2>

  <center>
    <div style="width: 400px; height: 30px; border-size: 1px; border-style: ridge;">
      <div style="width: <c:out value="${phase / phaseLabel * 100}"/>%; height: 30px; background-color: green;"></div>
    </div>
  </center>
  
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />


<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Progress" />
	<jsp:param name="headTitle" value="Progress" />
	<jsp:param name="meta" value="<meta http-equiv='refresh' content='3'>" />
</jsp:include>

  <div align="center">
    <c:out value="${progress.phaseLabel}"/>...

    <div style="width: 400px; height: 30px; border-size: 1px; border-style: ridge;">
      <div align="left" style="float: left; width: <c:out value="${progress.phase / progress.phaseCount * 100}"/>%; height: 30px; background-color: green;"></div>
    </div>
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />


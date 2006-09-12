<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%

/*
 * Hopefully our servlet container will not have flushed the few newlines of
 * output from above before we set the headers.
 */

long startTime = System.currentTimeMillis();
response.setHeader("Refresh", "2");
response.setHeader("Cache-Control", "no-store, private");
response.setDateHeader("Date", startTime);
response.setDateHeader("Expires", startTime);

%>
 
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Progress" />
	<jsp:param name="headTitle" value="Progress" />
</jsp:include>

<c:set var="label" value="${progress.phaseLabel}"/>
<c:set var="percentage">
	<fmt:formatNumber maxFractionDigits="0" value="${progress.phase / progress.phaseCount * 100}"/>
</c:set>

  <div align="center">
    <c:out value="${label}"/>...
    
    <div style="width: 400px; height: 30px; border-size: 1px; border-style: ridge;">
      <div style="width: 400px; height: 30px; position: relative; text-align: center; line-height: 30px; z-index: 2">
        <c:out value="${percentage}"/>%
      </div>
      <div style="position: relative; top: -30px; z-index: 1; float: left; width: <c:out value="${percentage}"/>%; height: 30px; background-color: green;">
      </div>
    </div>
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />


<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
    <p style="margin-bottom: 0px; font-size: 80%;">
      ${label}...
    </p>

    <div style="width: 400px; height: 25px; border-size: 1px; border-style: ridge; background-color: white;">
      <div style="float: left; width: ${percentage}%; height: 25px; background-color: green;">&nbsp;</div>
    </div>
    
    <p>
      ${percentage}% completed
    </p>
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />


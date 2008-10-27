<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" session="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<% String breadcrumb1 = "<a href='report/index.jsp'> Reports </a>"; %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="JasperServer-Reporting" />
  <jsp:param name="headTitle" value="JasperServer-Reporting" />
  <jsp:param name="location" value="JasperServer-Reporting" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="JasperServer-Reporting" />
</jsp:include>

<br/>
<table border="1">
  <tr>
    <th align="left">Type</th>
    <th align="left">Report</th>
    <th align="left">Description</th>
  </tr>
  <c:forEach var="entry" items="${model.folderEntries}">
    <c:if
      test="${entry.resourceType == \"com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.ReportUnit\"}">
    <!--  Display only report-units -->
    <tr>
      <td>
        <a href="jasperws/pdfreport.htm?report=<c:out value="${entry.uriString}" />" target="_blank">
          <img src="images/icon-adobe-reader.gif" alt="PDF-Icon" border="0"/>
        </a>
      </td>
      <td class="td_report" width="20%">
      <div class="housebutton"><a style="font-weight:bold"
        href="jasperws/pdfreport.htm?report=<c:out value="${entry.uriString}" />" target="_blank"><c:out
        value="${entry.label}" /></a></div>
      </td>
      <td><c:out value="${entry.description}" /></td>
    </tr>
    </c:if>
  </c:forEach>
</table>
<jsp:include page="/includes/footer.jsp" flush="false" />

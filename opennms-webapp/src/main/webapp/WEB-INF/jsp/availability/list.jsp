<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Availability" />
  <jsp:param name="headTitle" value="Availability" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<h3>Pre compiled reports</h3>

 <p>These reports have already been run. You may download them in html, pdf or pdf with embedded SVG by following the appropriate links</p>

  <table>
      <thead>
      <tr>
      <th>category</th>
      <th>type</th>
      <th>period ending</th>
      <th>available</th>
      <th>view report</th>
      </tr>
     </thead>
    <c:forEach items="${reports}" var="report">
      <tr>
      <td>${report.category}</td>
      <td>${report.type}</td>
      <td>${report.date}</td>
      <td>${report.available}</td>
      <td>
      <a href="availability/viewreport.htm?format=html&reportid=${report.id}">html</a>
       <a href="availability/viewreport.htm?format=pdf&reportid=${report.id}">pdf</a>
        <a href="availability/viewreport.htm?format=svg&reportid=${report.id}">svg</a>
      </tr>
    </c:forEach>
  </table>



<jsp:include page="/includes/footer.jsp" flush="false" />

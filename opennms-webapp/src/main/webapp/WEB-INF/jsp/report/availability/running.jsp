<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Availability Reports" />
  <jsp:param name="headTitle" value="Availability Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/availability/index.htm'>Availability</a>" />
  <jsp:param name="breadcrumb" 
  		value="<a href='report/availability/report.htm'>Run</a>"/>
  <jsp:param name="breadcrumb" value="Report Running"/>
</jsp:include>

<h3>Network Availability Report Running</h3>

	  <p>Running an availability report with the following format:</p>
	  <table>
	  <tr>
	  	<td>category:</td>
	  	<td>${availabilityReportCriteria.categoryName}</td>
	  </tr>
	  <tr>
	  	<td>format:</td>
	  	<td>${availabilityReportCriteria.format}</td>
	  </tr>
	  <tr>
	  	<td>month format:</td>
	  	<td>${availabilityReportCriteria.monthFormat}</td>
	  </tr>
	  <tr>
	  	<td>period ending:</td>
	  	<td>${availabilityReportCriteria.periodEndDate}</td>
	  </tr>
	   <tr>
	  	<td>email:</td>
	  	<td>${availabilityReportCriteria.email}</td>
	  </tr>
	   <tr>
	  	<td>logo:</td>
	  	<td>${availabilityReportCriteria.logo}</td>
	  </tr>
	  </table>
	  <br>
      <p>
        This is a very comprehensive report, and so can take some time to generate.
      </p>

<jsp:include page="/includes/footer.jsp" flush="false" />
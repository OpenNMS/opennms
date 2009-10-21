<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Availability Reports" />
  <jsp:param name="headTitle" value="Availability Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/availability/index.htm'>Availability</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<h3>Report Delivery Options</h3>

  
<form:form commandName="criteria">

	<B>Delivery Format</B><br>
	<form:radiobutton path="mailFormat" value="SVG"/> Graphical Reports in PDF Format <br>
    <form:radiobutton path="mailFormat" value="PDF"/> Numeric Reports in PDF Format <br>
    <form:radiobutton path="mailFormat" value="HTML"/> Numeric Reports in HTM Format <br>
    <br><B>Recipient Address</B><br>
    <form:input path="mailTo"/>
    <br><B>Save a copy of the report</B><br>
    Save Report <form:checkbox path="persist"/>
    <br>
    <input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
	<input type="submit" name="_eventId_revise" value="Revise"/>&#160;
	<input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;
</form:form>
  
<jsp:include page="/includes/footer.jsp" flush="false" />
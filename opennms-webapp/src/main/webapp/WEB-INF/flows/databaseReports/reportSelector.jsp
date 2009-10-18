<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/availability/index.htm'>Availability</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<h3>Database Reports</h3>

  <div style="width: 40%; float: left;">
  
  
   <form:form commandName="criteria">
        <p>
        	<B>Choose the category.</B><br>
                <form:select path="categoryName">
                <form:options items="${categories}"/>
                </form:select>
       </p>
      
    <input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
	<input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;

    </form:form>
  
  </div>

  <div style="width: 60%; float: left;">
        <p>Generating the pretty availability reports may take a few minutes, especially
        for large networks, so please do not press the stop or reload buttons
        until it has finished.  Thank you for your patience.
        </p>
        <p>You can keep a copy of the report for future reference by checking the "Save Report" radio button</p>
        <p>The SVG and PDF report formats can be viewed using Adobe Acrobat Reader.
        If you do not have Adobe Acrobat Reader and wish to download it, please click on the following link:</p>
        <p><a href="http://www.adobe.com/products/acrobat/readstep2.html" target="_new"><img src="images/getacro.gif" border="0" hspace="0" vspace="0" alt="Get Acrob
at Reader"/></a></p>
        <p><font size="-1">Acrobat is a registered trademark of Adobe Systems Incorporated.</font></p>
  </div>

<jsp:include page="/includes/footer.jsp" flush="false" />
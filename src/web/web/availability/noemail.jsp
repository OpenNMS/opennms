<%@page language="java" contentType="text/html" session="true" import="java.util.*" %>

<html>
<head>
  <title>Outage | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="No Email" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='availability/index.jsp'>Availability Report</a>" />
  <jsp:param name="breadcrumb" value="No Email" />
</jsp:include>

<br />

<!-- page title -->
<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr><td>No Email Address Configured</td></tr>
</table>

<br />

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>
      <p>
        The Availability report you requested could not be generated because you
        do not have an email address configured.  You will need to have your
        system administrator setup a valid email address for you.  Then, when
        you request this outage report, it will be emailed to you.
      </p>
      
      <p>
        <a href="availability/index.jsp">Go back to the Availability Report page</a>
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br />

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="report" />
</jsp:include>

</body>
</html>

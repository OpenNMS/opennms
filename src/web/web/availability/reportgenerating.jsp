<%@page language="java" contentType="text/html" session="true" import="java.util.*,org.opennms.netmgt.config.UserFactory" %>

<%!
    protected UserFactory userFactory;

    public void init() throws ServletException {
        try { 				
            UserFactory.init();
            this.userFactory = UserFactory.getInstance();
        }
        catch(Exception e) {
            this.log("could not initialize the UserFactory", e);
            throw new ServletException("could not initialize the UserFactory", e);
        }
    }
%>

<%
    String email = this.userFactory.getEmail(request.getRemoteUser());
%>

<html>
<head>
  <title>Availability | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Generated Report" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='availability/index.jsp'>Availability Report</a>" />
  <jsp:param name="breadcrumb" value="Report Generating" />
</jsp:include>

<br />

<!-- page title -->
<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr><td>Availability Report Generating</td></tr>
</table>

<br />

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>
      <p>
        The availability report you requested is now being generated.  This is a very 
        comprehensive report, and so can take up to a couple of hours to generate.
        It will be emailed to your email address (<%=email%>) as soon as it is 
        finished.
      </p>
      
      <p>
        <a href="availability/index.jsp">Go to the Availability Report page</a>
      </p>
      <p>
        <a href="report/index.jsp">Go to the Report menu</a>
      </p>      
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

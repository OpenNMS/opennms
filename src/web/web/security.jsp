<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Security | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Security" />
  <jsp:param name="location" value="security" />
</jsp:include>

<br>
<!-- Body -->
<h1>Sorry, not implemented yet</h1>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="security" />
</jsp:include>

</body>
</html>

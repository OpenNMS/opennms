<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Service Level Monitoring | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("SLM"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service Level Monitoring" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <jsp:include page="/includes/categories-box.jsp" flush="false" />
    </td>

    <td>&nbsp;</td>
  </tr>
</table>                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

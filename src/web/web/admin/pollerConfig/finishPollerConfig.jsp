<%@page language="java" contentType = "text/html" session = "true"  %>

<html>
<head>
  <title>Restart Pollers | Configure Pollers | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a HREF='admin/pollerConfig/index.jsp'>Configure Pollers</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Restart Pollers"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Restart Pollers" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br>

<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td>
        <h3>The Pollers Need to be Restarted for the Changes to Take Effect</h3>
        <p>
            Please click the &quot;Restart Pollers&quot; button below to have the pollers read the
            new configuration. If you want to make more poller configuration changes, please revisit the
            <a HREF="admin/pollerConfig/index.jsp">Configure Pollers</a> page.
        </p>
        <form method="post" action="admin/pollerConfig/finishPollerConfig">
          <input type="submit" value="Restart Pollers"/>
        </form>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>
</body>
</html>

<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Advanced Event Search | Events | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href=\"event/index.jsp\">Events</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Advanced Event Search"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Advanced Event Search" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br />
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td width="10">&nbsp;</td>

    <td valign="top">
      <h3>Advanced Event Search</h3>

      <jsp:include page="/event/advquerypanel.jsp" flush="false" />
    </td>

    <td width="20">&nbsp;</td>

    <td width="50%" valign="top">
      <h3>Searching Instructions</h3>

      <p>The <strong>Advanced Event Search</strong> page can be used to search the event list on
      multiple fields. Fill in values for each field that you wish to use to narrow down
      the search.</p>

      <p>To select events by time, first check the box for the time range
      that you wish to limit and then fill out the time in the boxes provided.</p>

      <p>If you wish to select events within a specific time span, check <em>both</em>
      boxes and enter the beginning and end of the range in the boxes provided.</p>
    </td>

    <td width="10">&nbsp;</td>
  </tr>
</table>

<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

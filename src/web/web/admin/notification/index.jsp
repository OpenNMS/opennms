<%@page language="java" contentType="text/html" session="true" import="" %>

<html>
<head>
  <title>Configure Notifications | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Configure Notifications"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Notifications" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; </td>

    <td valign="top">
      <h3>Configure Notifications</h3>

      <p>
        <a href="admin/notification/noticeWizard/eventNotices.jsp">Configure Event Notifications</a>
      </p>

      <p>
        <a href="admin/notification/destinationPaths.jsp">Configure Destination Paths</a>
      </p>
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Event Notifications</h3>

      <p>
        Each event can be configured to send a notification whenever that event is
        triggered. This wizard will walk you through the steps needed for configuring
        an event to send a notification.
      </p>

      <h3>Destination Paths</h3>

      <p>
        A destination path describes what users or groups will receive notifications, how the
        notifications will be sent, and who to notify if escalation is needed. This wizard will 
        walk you through setting up a resuable list of who to contact and how to contact them, 
        which are used in the event configuration.
      </p>
    </td>
    
    <td> &nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>

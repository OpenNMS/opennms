<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Outages | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Outages"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outages" />
  <jsp:param name="location" value="outage" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />  
</jsp:include>

<br />
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Outage Menu</h3>    

      <p><a href="outage/current.jsp">View current outages</a></p>
      <p><a href="outage/list">View all outages</a></p>
      
      <p>
        <form method="GET" action="outage/detail.jsp" >
          Get&nbsp;details&nbsp;for&nbsp;Outage&nbsp;ID:<br />
          <input type="text" name="id" />
          <input type="submit" value="Search" />
        </form>      
      </p>
    </td>
    
    <td>&nbsp;</td>

    <td valign="top" width="60%" >
      <h3>Outages and Service Level Availability</h3>

      <p>
        Outages are tracked by OpenNMS by polling services that have been
        discovered.  If the service does not respond to the poll, a service outage
        is created and service level availbility levels are impacted.  Service 
        outages created notifications. 
      </p>     
    </td>

    <td>&nbsp;</td>
  </tr>
</table>                                    
                                     
<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

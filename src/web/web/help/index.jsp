<%@page language="java" contentType="text/html" session="true" %>

<html>
<head>
  <title>Help | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Help"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Help" />
  <jsp:param name="location" value="help" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Local Resources</h3>
      <p><a href="help/about.jsp">About the OpenNMS Web Console</a></p>

      <h3>Internet Resources</h3>
      <p><a href="http://www.opennms.org/users/faq/">Frequently Asked Questions</a></p>
      <p><a href="http://www.opennms.org/users/docs/">Online Documentation</a></p>
    </td>
    
    <td>&nbsp;</td>

    <td valign="top" width="60%">
      <h3>Helpful Resources</h3>
    
      <p>
        <em>Local resources</em> contain help and are located within your own OpenNMS system.
        <em>Internet resources</em> are external web pages (exits from your OpenNMS Web Console)
        that have information relevant to your OpenNMS system.
      </p>
      
      <p>
        Browse the <em>Frequently Asked Questions</em> to find
        answers to common questions or read up on network management the OpenNMS way in the 
        <em>Online Documentation</em>.  Check out important attributes of your OpenNMS system
        on the <em>About page</em>.
      </p>
      
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="help" />
</jsp:include>

</body>
</html>

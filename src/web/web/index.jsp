<%@page language="java" contentType="text/html" session="true"  %>

<html>
<head>
  <title>OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Web Console" />
</jsp:include>

<br> 

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr> 
    <td>&nbsp;</td>
  
    <!-- Column 1 of Body -->  
    <td width="25%" valign="top" >
      <!-- Services down box -->
      <jsp:include page="/includes/servicesdown-box.jsp" flush="false" />
    </td>
    
    <td>&nbsp;</td>

    <!-- Middle Column -->
    <td valign="top">
      <!-- category box(es) -->    
      <jsp:include page="/includes/categories-box.jsp" flush="false" />                

      <!--desktop query box -->  
    </td>

    <td>&nbsp;</td>

    <!-- Column 3 of Body -->  
    <td width="25%" valign="top" >
      <!-- notification box -->    
      <jsp:include page="/includes/notification-box.jsp" flush="false" />    
            
      <br>
      
      <!-- Performance box -->    
      <jsp:include page="/includes/performance-box.jsp" flush="false" />

        <br>
      
      <!-- Performance box -->    
      <jsp:include page="/includes/response-box.jsp" flush="false" />

        <br>
        <!-- security box -->    
      <%--
        Commenting out the security box include until it is functional
        <jsp:include page="/includes/security-box.jsp" flush="false" />
      --%>
    </td>
    
    <td>&nbsp;</td>   
  </tr>
</table>
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

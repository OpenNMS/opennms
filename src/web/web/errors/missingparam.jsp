<%@page language="java" contentType="text/html" session="true" isErrorPage="true" import="org.opennms.web.MissingParameterException"%>

<html>
<head>
  <title>Missing Parameter | Error | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Error"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<% 
    MissingParameterException mpe = (MissingParameterException)exception;  
%>

<!-- Body -->

<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h1>Missing Parameter</h1>

      <p>
        The request you made was incomplete.  It was missing the <strong><%=mpe.getMissingParameter()%></strong> parameter.
      </p>

      <p>
        The following parameters are required:
        <ul>
<%      
        String[] params = mpe.getRequiredParameters();        

        for( int i = 0; i < params.length; i++ ) {  %>
          <li> <%=params[i]%>
<%      }    %>
        </ul>
      </p>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>                               

<br>


<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

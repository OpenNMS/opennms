<%@page language="java" contentType="text/html" session="true" isErrorPage="true" import="org.opennms.web.category.*"%>

<html>
<head>
  <title>Category Not Found | Error | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" leftmargin="0" rightmargin="0" topmargin="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Error"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br />

<%
    CategoryNotFoundException cnfe = null;
    
    if( exception instanceof CategoryNotFoundException ) {
        cnfe = (CategoryNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        cnfe = (CategoryNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }    
    
    try {
        //try to resubscribe
        RTCPostSubscriber subscriber = new RTCPostSubscriber();
        subscriber.subscribe(cnfe.getCategory());
        this.log( "Sent subscription event to RTC for " + cnfe.getCategory() );
    }
    catch( Exception e ) {
        this.log( "Could not send POST subscription event to RTC", e );
    }
%>

<!-- Body -->

<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h1>Category Not Found</h1>

      <p>
        No information is currently available for the <strong><%=cnfe.getCategory()%></strong> category.
        <br>
        Either the category does not exist, or its information is still being calculated.  
      </p>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>                               

<br />


<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%@page language="java" contentType="text/html" session="true" isErrorPage="true" import="org.opennms.web.notification.*"%>

<html>
<head>
  <title>Notice Id Not Found | Error | OpenNMS Web Console</title>
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
    NoticeIdNotFoundException ninfe = null;
    
    if( exception instanceof NoticeIdNotFoundException ) {
        ninfe = (NoticeIdNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        ninfe = (NoticeIdNotFoundException)((ServletException)exception).getRootCause();
    }
    else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
%>


<!-- Body -->

<table width="100%" border="0" cellspacing="0" cellpadding="2">
  <tr>
    <td> &nbsp; </td>

    <td>
      <h1>Notice Id Not Found</h1>

      <p>
        The notice id <%=ninfe.getBadID()%> is invalid. <%=ninfe.getMessage()%><br>
        You can re-enter it here or <a href="notification/browse?acktype=unack">browse all of the notices</a> to find the notice you are looking for.
      </p>

      <p>
        <form METHOD="GET" ACTION="notice/detail.jsp" >
          Get&nbsp;details&nbsp;for&nbsp;Notice&nbsp;ID:<br>
        <input type="TEXT" NAME="id" />
      </form>
      </p>
    </td>

    <td> &nbsp; </td>
  </tr>
</table>                               

<br>


<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%@page language="java" contentType = "text/html" session = "true"  %>
<%
	String error = null;
	String name = null;
	try
    	{
		error = request.getParameter( "error" );
		name = request.getParameter( "name" );
	}
	catch(Exception e)
	{
		throw new ServletException("Admin:pollerConfig " + e.getMessage());
	}
%>
<html>
<head>
  <title>Error Page | Configure Poller | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='admin/pollerConfig/index.jsp'>Configure Pollers</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("Error Page"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="User Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
</jsp:include>

<br />

<table border="0" width="100%">
<tr>
<td>&nbsp;</td>
<td>
<% 
	int errorcode = (new Integer(error)).intValue();
	switch(errorcode)
	{
		case 0:	%>
				An error has occured due to a missing parameter <%= name %> in the poller configuration file
			
	<%		break;
		case 1: %>
				An error has occured since the <%= name %> poller already exists
	<%		break;
		case 2: %>
				An error has occured due to the poller-configuration.xml file being empty
	<%		break;
		case 3:%>
				An error has occured due to the capsd-configuration.xml file being empty
	<%
			break;
	}
%>

</td>
</tr>
<tr><td>&nbsp;</td>
<p><a href="admin/pollerConfig/index.jsp">Go back to the Poller Configuration page</a></p>
</td>

</tr></table>

<br />

<jsp:include page="/includes/footer.jsp" flush="true"/>
</body>
</html>

<%-- This page has been deprecated!  Please do not use it. --%>
<%@ page language="java" contentType = "text/html" session = "true" import="org.opennms.web.admin.*,java.util.*"  isErrorPage="true" %>
<html>
<head>
<title>Error | OpenNMS Web Console</title>
<base HREF="<%=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/"%>" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<%= session.getValue("exception") %>

</body>
</html>

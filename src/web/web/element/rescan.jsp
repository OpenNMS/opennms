<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,org.opennms.web.*" %>

<%
    String nodeIdString = request.getParameter("node");
    String ipAddr = request.getParameter("ipaddr");
    
    if( nodeIdString == null ) {
        throw new MissingParameterException("node");
    }
    
    int nodeId = Integer.parseInt(nodeIdString);
    String nodeLabel = NetworkElementFactory.getNodeLabel(nodeId);
        
    String returnUrl = null;        
    if( ipAddr == null ) {        
        returnUrl = "element/node.jsp?node=" + nodeIdString;
    }
    else {
        returnUrl = "element/interface.jsp?node=" + nodeIdString + "&intf=" + ipAddr;    
    }
%>

<html>
<head>
  <title>Rescan | Element | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% if( ipAddr == null ) { %>
  <% String breadcrumb1 = java.net.URLEncoder.encode("<a href='element/index.jsp'>Search</a>"); %>
  <% String breadcrumb2 = java.net.URLEncoder.encode("<a href='element/node.jsp?node=" + nodeId + "'>Node</a>"); %>
  <% String breadcrumb3 = java.net.URLEncoder.encode("Rescan"); %>
  <jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Rescan" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  </jsp:include>
<% } else { %>
  <% String intfCrumb = ""; %>
  <% String breadcrumb1 = java.net.URLEncoder.encode("<a href='element/index.jsp'>Search</a>"); %>
  <% String breadcrumb2 = java.net.URLEncoder.encode("<a href='element/node.jsp?node=" + nodeId + "'>Node</a>"); %>
  <% String breadcrumb3 = java.net.URLEncoder.encode("<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr + "'>Interface</a>"); %>
  <% String breadcrumb4 = java.net.URLEncoder.encode("Rescan"); %>
  <jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Rescan" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
  </jsp:include>
<% } %>

<br>

<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="0"border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Capability Rescan</h3>
      
      <p>Are you sure you want to rescan the <nobr><%=nodeLabel%></nobr>      
        <% if( ipAddr==null ) { %>
            node?
        <% } else { %>
            (<%=ipAddr%>) interface?
        <% } %>
      </p>
      
      <p>
        <form method="POST" action="element/rescan">
          <input type="hidden" name="node" value="<%=nodeId%>" />
          <input type="hidden" name="returnUrl" value="<%=returnUrl%>" />             

          <input type="submit" value="Rescan" />
          <input type="button" value="Cancel" onClick="window.open('<%=Util.calculateUrlBase(request) + "/" + returnUrl%>', '_self')" />             
        </form>
      </p>

    </td>
    
    <td>&nbsp;</td>

    <td valign="top" width="60%">
      <h3>Capability Scanning</h3>
    
      <p>
        A <em>capability scan</em> is a suite of tests to determine what <em>capabilities</em>
        a node or interface has.  A capability is in most cases a service, like FTP or HTTP.
      </p>      
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*" %>

<%
    Node[] nodes = null;
    String nameParm = request.getParameter( "nodename" );
    String ipLikeParm = request.getParameter( "iplike" );
    String serviceParm = request.getParameter( "service" );

    if( nameParm != null ) {
        nodes = NetworkElementFactory.getNodesLike( nameParm );
    }
    else if( ipLikeParm != null ) {
        nodes = NetworkElementFactory.getNodesWithIpLike( ipLikeParm );
    }
    else if( serviceParm != null ) {
        int serviceId = Integer.parseInt( serviceParm );
        nodes = NetworkElementFactory.getNodesWithService( serviceId );
    }
    else {
        nodes = NetworkElementFactory.getAllNodes();
    }

    int lastIn1stColumn = (int)Math.ceil( nodes.length/2.0 );
    int interfaceCount = 0;
%>

<html>
<head>
  <title>Node List | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='element/index.jsp'>Search</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Node List"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node List" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <h3>Nodes and their Interfaces</h3>
    <td> &nbsp; </td>
  </tr>

  <tr>
    <td> &nbsp; </td>

  <% if( nodes.length > 0 ) { %>
    <td valign="top">
      <!-- left column -->
      <ul>
      <% for( int i=0; i < lastIn1stColumn; i++ ) { %>
        <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
        <% Interface[] interfaces = NetworkElementFactory.getInterfacesOnNode(nodes[i].getNodeId()); %>
        <ul>
          <% for( int j=0; j < interfaces.length; j++ ) { %>
            <% if( !"0.0.0.0".equals(interfaces[j].getIpAddress() )) { 
               interfaceCount++;
            %>
            <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a>
            <% } %>
          <% } %>
        </ul>
      <% } %>
      </ul>
    </td>

    <td valign="top">      
      <!-- right column -->
      <ul>
      <% for( int i=lastIn1stColumn; i < nodes.length; i++ ) { %>
        <li><a href="element/node.jsp?node=<%=nodes[i].getNodeId()%>"><%=nodes[i].getLabel()%></a>
        <% Interface[] interfaces = NetworkElementFactory.getInterfacesOnNode(nodes[i].getNodeId()); %>
        <ul>
          <% for( int j=0; j < interfaces.length; j++ ) { %>
            <% if( !"0.0.0.0".equals(interfaces[j].getIpAddress() )) { 
               interfaceCount++;
            %>
            <li> <a href="element/interface.jsp?node=<%=interfaces[j].getNodeId()%>&intf=<%=interfaces[j].getIpAddress()%>"><%=interfaces[j].getIpAddress()%></a>
            <% } %>
          <% } %>
        </ul>
      <% } %>
      </ul>
    </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
    
    <td> &nbsp; </td>
  </tr>
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <br><%=nodes.length%> Nodes, <%=interfaceCount%> Interfaces</td>
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

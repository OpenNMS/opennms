<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*" %>

<%!
    AssetModel model;

    public void init() throws ServletException {
        this.model = new AssetModel();
    }
%>

<%
    String column = request.getParameter( "column" );
    String search = request.getParameter( "searchvalue" );

    if( column == null ) {
        throw new org.opennms.web.MissingParameterException( "column", new String[] {"column","searchvalue"} );
    }

    if( search == null ) {
        throw new org.opennms.web.MissingParameterException( "searchvalue", new String[] {"column","searchvalue"} );
    }

    AssetModel.MatchingAsset[] assets = model.searchAssets( column, search );
%>

<html>
<head>
  <title>Asset List | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='asset/index.jsp'>Assets</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Asset List"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Asset List" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->
<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td> &nbsp; </td>
    <td colspan="2"> <h3>Assets</h3>
    <td> &nbsp; </td>
  </tr>

  <tr>
    <td> &nbsp; </td>

  <% if( assets.length > 0 ) { %>
    <td valign="top">
      <table width="100%" cellspacing="2" cellpadding="2" border="0">
        <tr>
          <td width="10%"><b>Asset</b></td>
          <td><b>Matching Text</b></td>
        </tr>

      <% for( int i=0; i < assets.length; i++ ) { %>
        <tr>
          <td><a href="asset/modify.jsp?node=<%=assets[i].nodeId%>"><%=assets[i].nodeLabel%></a></td>
          <td><%=assets[i].matchingValue%></td>
        </tr>
      <% } %>
      </table>
   </td>
  <% } else { %>
    <td>
      None found.
    </td>    
  <% } %>
    
    <td> &nbsp; </td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

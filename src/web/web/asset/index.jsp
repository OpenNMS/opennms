<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*,java.util.*,org.opennms.web.element.NetworkElementFactory" %>

<%!
    protected AssetModel model;
    protected String[][] columns;

    public void init() throws ServletException {
        this.model = new AssetModel();
        this.columns = this.model.getColumns();
    }
%>

<%
    Asset[] allAssets = this.model.getAllAssets();
    ArrayList assetsList = new ArrayList();

    for( int i=0; i < allAssets.length; i++ ) {
        if( !"".equals(allAssets[i].getAssetNumber()) ) {
            assetsList.add( allAssets[i] );
        }
    }

    int assetCount = assetsList.size();
    int middle = assetCount/2;  //integer division so it should round down
    if( assetCount%2 == 1 ) {
        middle++;  //make sure the one odd entry is on the left side
    }
%>

<html>
<head>
  <title>Assets | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("Assets"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Assets" />
  <jsp:param name="location" value="asset" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<br>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>

    <td valign="top">
      <h3>Search Asset Information</h3>
      <p>
        <form action="asset/nodelist.jsp" method="GET">
          Assets in category: <br>
          <input type="hidden" name="column" value="category" />
          <select name="searchvalue" size="1">
            <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
              <option><%=Asset.CATEGORIES[i]%></option> 
            <% } %>
          </select>
          <input type="submit" value="Search" />
        </form>

        <a href="asset/nodelist.jsp?column=<%=this.columns[0][1]%>&searchvalue=">List all nodes with asset info</a>
      </p>
      <br>
    </td>
    
    <td>&nbsp;</td>

    <td width="60%" valign="top">
        <h3>Assets Inventory</h3>

        <p>The OpenNMS system provides a means for you to easily track and share 
            important information about capital assets in your organization.  This 
            data, when coupled with the information about your network that the 
            OpenNMS system obtains during network discovery, can be a powerful tool not 
            only for solving problems, but in tracking the current state of 
            equipment repairs as well as network or system-related moves, additions, 
            or changes.
        </p>    
            
        <p>There are two ways to add or modify the asset data stored in the OpenNMS system:
            <ul>
              <li>Import the data from another source (Importing asset data is 
            described on the <em>Admin</em> page)
              <li>Enter the data by hand
            </ul>
            
            Once you begin adding data to the OpenNMS system's assets inventory page, 
            any node with an asset number (for example, bar code) will be displayed on the 
            lower half of this page, providing you a one-click mechanism for 
            tracking the current physical status of that device.  If you wish to 
            search for particular assets by category, simply click the drop-down box 
            labeled <b>Assets in category</b>, select the desired category, and click 
            <b>[Search]</b> to retrieve a list of all assets associated with that category. 
            And for a complete list of nodes, whether or not they have associated 
            asset numbers, simply click on the <b>List all nodes with asset information</b> 
            link.
        </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
<br>
<hr align="center" size="2" width="95%">
<br>
<table>
  <tr>
    <td>&nbsp;</td>

    <td colspan="3" valign="top">
      <h3>Assets with asset numbers</h3>

      <table width="100%" cellspacing="0" cellpadding="2" border="0">
        <tr>
          <td>
            <ul>
            <% for( int i=0; i < middle; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a>
            <% } %>
            </ul>
          </td>
          <td>
            <% for( int i=middle; i < assetCount; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a>
            <% } %>
          </td>
        </tr>
      </table>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="asset" />
</jsp:include>

</body>
</html>

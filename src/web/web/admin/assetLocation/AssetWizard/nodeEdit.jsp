<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.asset.*,org.opennms.web.element.*,org.opennms.web.admin.assetLocation.assetWizard.*" %>


<%
    boolean isNew = false;
    HttpSession user = request.getSession(true);
    Asset asset = (Asset)user.getAttribute("newAsset");
    String isNewString = (String)user.getAttribute("isNew");
	if ( isNewString.equals("true")){
	        isNew = true;        
}

    int nodeId = asset.getNodeId();
    String nodeLabel = org.opennms.web.element.NetworkElementFactory.getNodeLabel( nodeId );
    Node node_db = NetworkElementFactory.getNode( nodeId );

%>

<html>
<head>
  <title>Asset Location Node Configuration | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "<a href='admin/assetLocation/index.jsp" +  "'>Configure Asset Location</a>"; %>
<% String breadcrumb3 = "<a href='admin/assetLocation/AssetWizard/building.jsp" +  "'>Buildings</a>"; %>
<% String breadcrumb4 = "<a href='admin/assetLocation/AssetWizard/room.jsp" +  "'>Rooms</a>"; %>
<% String breadcrumb5 = "Edit Node"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Building's Room Configuration" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb5%>" />
</jsp:include>


<br>

<table width="100%" cellspacing="0" cellpadding="2" border="0">
  <tr>
    <td>&nbsp;</td>
    <td colspan="3">
      <h2>Define Asset Location for node <%=nodeLabel%></h2>
    </td>
    <td>&nbsp;</td>
  </tr>

  <tr>
    <td>&nbsp;</td>

    <td>
      <form action="admin/assetLocation/AssetWizard/assetLocationWizard" method="POST">
      <input type="hidden" name="sourcePage" value="<%=AssetLocationWizardServlet.SOURCE_PAGE_EDIT_NODE%>"/>
        <input type="hidden" name="node" value="<%=nodeId%>" />
        <input type="hidden" name="isnew" value="<%=isNew%>" />
        <input type="hidden" value="<%=asset.getAddress1()%>" name="address1"/>
        <input type="hidden" value="<%=asset.getAddress2()%>" name="address2"/>
        <input type="hidden" value="<%=asset.getCity()%>" name="city"/>
        <input type="hidden" value="<%=asset.getState()%>" name="state"/>
        <input type="hidden" value="<%=asset.getZip()%>" name="zip"/>
        <input type="hidden" value="<%=asset.getBuilding()%>" name="building"/>
        <input type="hidden" value="<%=asset.getRoom()%>" name="room"/>
        <input type="hidden" value="<%=asset.getFloor()%>" name="floor"/>

        <table width="100%" cellspacing="0" cellpadding="2" border="1" bgcolor="#cccccc" bordercolor="#000000">
          <tr>
            <td colspan="6"><h3>Location</h3></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Address&nbsp;1</td>
            <td colspan="5">&nbsp;<%=asset.getAddress1()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">City</td>
            <td>&nbsp;<%=asset.getCity()%></td>
            <td bgcolor="#999999">State</td>
            <td>&nbsp;<%=asset.getState()%></td>
            <td bgcolor="#999999">ZIP</td>
            <td>&nbsp;<%=asset.getZip()%></td>
          </tr>
          <tr>
            <td bgcolor="#999999">Building</td>
            <td bgcolor="red">&nbsp;<%=asset.getBuilding()%></td>
            <td bgcolor="#999999">Floor</td>
            <td>&nbsp;<%=asset.getFloor()%></td>
            <td bgcolor="#999999">Room</td>
            <td bgcolor="red">&nbsp;<%=asset.getRoom()%></td>
          </tr>
	</table>

        <table width="100%" cellspacing="0" cellpadding="2" border="0">
          <tr>
		<td colspan="8"><br></td>
	  </tr>
            <td>Category</td>
            <td>
              <select name="category" size="1">
              <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
                <option <%=(Asset.CATEGORIES[i].equals(asset.getCategory()))?"selected":""%>><%=Asset.CATEGORIES[i]%></option>
              <% } %>
              </select>
            </td>
          </tr>
          <tr>
            <td>Rack</td>
            <td><input type="text" name="rack" value="<%=asset.getRack()%>" size="20" maxlength="64"/></td>
            <td>Slot</td>
            <td><input type="text" name="slot" value="<%=asset.getSlot()%>" size="20" maxlength="64"/></td>
            <td>Port</td>
            <td><input type="text" name="port" value="<%=asset.getPort()%>" size="20" maxlength="64"/></td>
            <td>Circuit&nbsp;ID</td>
            <td><input type="text" name="circuitid" value="<%=asset.getCircuitId()%>" size="20" maxlength="64"/></td>
          </tr>

          <tr>
            <td colspan="4">
              <input type="submit" value="Submit"/>
              <input type="reset" />
            </td>
            <td colspan="4" align="right"> 
              <font size="-1">
              <% if( isNew ) { %>
                  <em>New Record</em>
              <% } else { %>
                  <em>Last Modified: <%=asset.getLastModifiedDate()%> by <%=asset.getUserLastModified()%></em>
              <% } %>
              </font>  
            </td>
          </tr>
        </table>
      </form>

      <p>Note that all commas and end of line markers will be removed when 
        submitted.  Please try to format your comments and other values without
        commas or hitting the return key to add new lines.
      </p>
    </td>

    <td>&nbsp;</td>
  </tr>
</table>
                                     
<br>
<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

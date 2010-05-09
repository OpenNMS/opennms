<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.asset.*,
		java.util.*,
		org.opennms.web.element.NetworkElementFactory
	"
%>

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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Assets" />
  <jsp:param name="headTitle" value="Assets" />
  <jsp:param name="location" value="asset" />
  <jsp:param name="breadcrumb" value="Assets" />
</jsp:include>

  <div class="TwoColLeft">
    <h3>Search Asset Information</h3>
    <div class="boxWrapper">
      <form action="asset/nodelist.jsp" method="get">
        <p align="right">Assets in category: 
        <input type="hidden" name="column" value="category" />
        <select name="searchvalue" size="1">
          <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
            <option><%=Asset.CATEGORIES[i]%></option> 
          <% } %>
        </select>
        <input type="submit" value="Search" />
      </form>
      <ul class="plain">
        <li><a href="asset/nodelist.jsp?column=<%=this.columns[0][1]%>&searchvalue=">All nodes with asset info</a></li>
      </ul>
    </div>
  </div>

  <div class="TwoColRight">
    <h3>Assets Inventory</h3>
    <div class="boxWrapper">
        <p>The OpenNMS system provides a means for you to easily track and share 
            important information about capital assets in your organization.  This 
            data, when coupled with the information about your network that the 
            OpenNMS system obtains during network discovery, can be a powerful tool not 
            only for solving problems, but in tracking the current state of 
            equipment repairs as well as network or system-related moves, additions, 
            or changes.
        </p>
        <p>There are two ways to add or modify the asset data stored in the OpenNMS system:</p>
        <ul>
          <li>Import the data from another source (Importing asset data is described on the <em>Admin</em> page)</li>
          <li>Enter the data by hand</li>
        </ul>
          <p>Once you begin adding data to the OpenNMS system's assets inventory page, 
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
      </div>
  </div>
  <hr />
  <h3>Assets with asset numbers</h3>
  <div class="boxWrapper">
    <ul class="plain" style="width:48%; margin-right:2%; float:left;">
    <% for( int i=0; i < middle; i++ ) {%>
      <%  Asset asset = (Asset)assetsList.get(i); %>
      <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a></li>
    <% } %>
    </ul>
    <ul class="plain" style="width:50%; float:left;">
    <% for( int i=middle; i < assetCount; i++ ) {%>
      <%  Asset asset = (Asset)assetsList.get(i); %>
      <li><%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(asset.getNodeId())%></a></li>
    <% } %>
    </ul>
    <hr />
  </div>
<jsp:include page="/includes/footer.jsp" flush="false"/>

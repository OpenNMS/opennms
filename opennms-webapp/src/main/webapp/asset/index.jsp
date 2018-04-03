<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

    public void init() throws ServletException {
        this.model = new AssetModel();
    }
%>

<%
    Asset[] allAssets = this.model.getAllAssets();
    ArrayList<Asset> assetsList = new ArrayList<>();

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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Assets" />
  <jsp:param name="headTitle" value="Assets" />
  <jsp:param name="location" value="asset" />
  <jsp:param name="breadcrumb" value="Assets" />
</jsp:include>

<div class="row">
  <div class="col-md-6">
    <div class="row">
      <div class="col-md-12">
        <div class="panel panel-default">
          <div class="panel-heading">
            <h3 class="panel-title">Search Asset Information</h3>
          </div>
          <div class="panel-body">
            <div class="row">
              <div class="col-md-6 col-xs-6">
                <ul class="list-unstyled">
                  <li><a href="asset/nodelist.jsp?column=_allNonEmpty">All nodes with asset info</a></li>
                </ul>
              </div> <!-- column -->
              <div class="col-md-6 col-xs-6">
                <form role="form" class="form-inline text-right" action="asset/nodelist.jsp" method="get">
                  <div class="form-group">
                    <label for="input_searchvalue">Assets in category:</label>
                    <select class="form-control" id="input_searchvalue" name="searchvalue">
                      <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
                        <option><%=Asset.CATEGORIES[i]%></option>
                      <% } %>
                    </select>
                  </div>
                  <input type="hidden" name="column" value="category" />
                  <button type="submit" class="btn btn-default">Search</button>
                </form>
              </div> <!-- column -->
            </div> <!-- row -->
          </div> <!-- panel-body -->
        </div> <!-- panel -->
      </div> <!-- column -->
    </div> <!-- row -->
    <div class="row">
      <div class="col-md-12">
        <div class="panel panel-default">
          <div class="panel-heading">
            <h3 class="panel-title">Assets with Asset Numbers</h3>
          </div>
          <div class="panel-body">
            <ul class="list-unstyled" style="width:48%; margin-right:2%; float:left;">
            <% for( int i=0; i < middle; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(asset.getNodeId())%></a></li>
            <% } %>
            </ul>
            <ul class="list-unstyled" style="width:50%; float:left;">
            <% for( int i=middle; i < assetCount; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li><%=asset.getAssetNumber()%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(asset.getNodeId())%></a></li>
            <% } %>
            </ul>
          </div> <!-- panel-body -->
        </div> <!-- panel -->
      </div>
    </div>
  </div>

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Assets Inventory</h3>
      </div>
      <div class="panel-body">
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
       </div> <!-- panel-body -->
     </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

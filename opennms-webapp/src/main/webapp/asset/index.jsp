<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.asset.*,
		java.util.*,
		org.opennms.web.element.NetworkElementFactory
	"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Assets")
          .breadcrumb("Assets")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="row">
      <div class="col-md-12">
        <div class="card">
          <div class="card-header">
            <span>Search Asset Information</span>
          </div>
          <div class="card-body">
            <div>
                <ul class="list-unstyled">
                <li><a href="asset/nodelist.jsp?column=_allNonEmpty">All nodes with asset info</a></li>
                </ul>
            </div> <!-- row -->
            <div>
                <form role="form" class="form-group" action="asset/nodelist.jsp" method="post">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                  <label for="input_searchvalue">Assets in category</label>
                  <div class="input-group">
                    <select class="form-control custom-select" id="input_searchvalue" name="searchvalue">
                      <% for( int i=0; i < Asset.CATEGORIES.length; i++ ) { %>
                        <option><%=Asset.CATEGORIES[i]%></option>
                      <% } %>
                    </select>
                    <div class="input-group-append">
                        <button type="submit" class="btn btn-secondary"><i class="fas fa-magnifying-glass"></i></button>
                    </div>
                  </div>
                  <input type="hidden" name="column" value="category" />
                </form>
            </div> <!-- row -->
          </div> <!-- card-body -->
        </div> <!-- panel -->
      </div> <!-- column -->
    </div> <!-- row -->
    <div class="row">
      <div class="col-md-12">
        <div class="card">
          <div class="card-header">
            <span>Assets with Asset Numbers</span>
          </div>
          <div class="card-body">
            <ul class="list-unstyled mb-0" style="width:48%; margin-right:2%; float:left;">
            <% for( int i=0; i < middle; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li> <%=WebSecurityUtils.sanitizeString(asset.getAssetNumber())%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=WebSecurityUtils.sanitizeString(NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(asset.getNodeId()))%></a></li>
            <% } %>
            </ul>
            <ul class="list-unstyled mb-0" style="width:50%; float:left;">
            <% for( int i=middle; i < assetCount; i++ ) {%>
              <%  Asset asset = (Asset)assetsList.get(i); %>
              <li><%=WebSecurityUtils.sanitizeString(asset.getAssetNumber())%>: <a href="asset/modify.jsp?node=<%=asset.getNodeId()%>"><%=WebSecurityUtils.sanitizeString(NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(asset.getNodeId()))%></a></li>
            <% } %>
            </ul>
          </div> <!-- card-body -->
        </div> <!-- panel -->
      </div>
    </div>
  </div>

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Assets Inventory</span>
      </div>
      <div class="card-body">
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
       </div> <!-- card-body -->
     </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

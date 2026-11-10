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
		    org.opennms.web.servlet.MissingParameterException,
            org.opennms.web.springframework.security.AclUtils"%>
<%
    final String ALL_NON_EMPTY = "_allNonEmpty";
    String column = request.getParameter("column");
    String search = request.getParameter("searchvalue");
    String requiredParameters[] = new String[] { "column", "searchvalue" };

    if( column == null ) {
        throw new MissingParameterException("column", requiredParameters);
    }

    if( search == null && ! column.equals(ALL_NON_EMPTY)) {
        throw new MissingParameterException("searchvalue", requiredParameters);
    }

    AssetModel.MatchingAsset[] assets = column.equals(ALL_NON_EMPTY) ? AssetModel.searchNodesWithAssets() : AssetModel.searchAssets(column, search);

    AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Asset List")
          .breadcrumb("Assets", "asset/index.jsp")
          .breadcrumb("Asset List")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

 <% if (request.getParameter("showMessage") != null && request.getParameter("showMessage").equalsIgnoreCase("true")) { %>
 <br />
 <p class="lead"><%= request.getSession(false).getAttribute("message") %></p>
 <% } %>

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Assets</span>
      </div>
    <% if( assets.length > 0 ) { %>
        <table class="table table-sm table-bordered">
          <tr>
            <th>Matching Text</td>
            <th>Asset Link</td>
            <th>Node Link</td>
          </tr>

        <% for( int i=0; i < assets.length; i++ ) {
            if (!accessChecker.isNodeAccessible(assets[i].nodeId)) {
                continue;
            }
        %>
          <tr>
            <td><%=assets[i].matchingValue%></td>
            <td><a href="asset/modify.jsp?node=<%=assets[i].nodeId%>"><%=assets[i].nodeLabel%></a></td>
            <td><a href="element/node.jsp?node=<%=assets[i].nodeId%>"><%=assets[i].nodeLabel%></a></td>
          </tr>
        <% } %>
        </table>
    <% } else { %>
        <div class="card-body">
          <p>None found.</p>
        </div>
    <% } %>
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
<%@page language="java" contentType="text/html" session="true"
	import="java.sql.Connection,
			java.util.List,
			java.util.Set,
			org.opennms.core.db.DataSourceFactory,
			org.opennms.core.utils.DBUtils,
			org.opennms.netmgt.dao.api.PathOutageManager,
			org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl"
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Path Outage Nodes")
          .breadcrumb("Path Outages", "pathOutage/index.jsp")
          .breadcrumb("Nodes")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<% 
      String critIp = request.getParameter("critIp");
      String critSvc = request.getParameter("critSvc");
      PathOutageManager pathOutageManager = PathOutageManagerDaoImpl.getInstance();
      String[] pthData = pathOutageManager.getCriticalPathData(critIp, critSvc);
      Set<Integer> nodeList = pathOutageManager.getNodesInPath(critIp, critSvc);
%>
  
<div class="card fix-subpixel">
    <div class="card-header">
        <span>Path Outage Node List</span>
    </div>
    <table class="table table-sm severity">
          <tr>
          <th>Critical Path</th>
          <th>Status</th>
          </tr>

          <tr>
          <td><%= critIp %></td>
          <td class="bright severity-<%= pthData[3].toLowerCase() %>"><%= critSvc %></td>
          </tr>

          <tr>
          <th>Node</th>
          <th>Status</th>
          </tr>

          <% for (Integer nodeid : nodeList) {
              String labelColor[] = PathOutageManagerDaoImpl.getInstance().getLabelAndStatus(nodeid.toString(), null); %>
              <tr>
              <td><a href="element/node.jsp?node=<%= nodeid %>"><%= labelColor[0] %></a></td>
              <td class="bright severity-<%= labelColor[1].toLowerCase() %>"><%= labelColor[2] %></td>
              </tr>
          <% } %>
    </table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

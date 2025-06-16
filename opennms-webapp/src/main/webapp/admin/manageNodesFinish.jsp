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
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.OnmsNode
	"
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Manage Interfaces")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Manage/Unmanage Interfaces Finish")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<%
	OnmsNode node = null;
	String nodeIdString = request.getParameter("node");
	if (nodeIdString != null) {
		try {
			int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
			node = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
		} catch (NumberFormatException e) {
			// ignore this, we just won't put a link if it fails
		}
	}
%>

<div class="card">
  <div class="card-header">
    <span>Database Update Complete After Management Changes</span>
  </div>
  <div class="card-body">
    <p>
      These changes take effect immediately. OpenNMS does not need to be restarted.
    </p>

    <p>
      Changes for a specific node will become effective upon execution of
      a forced rescan on that node. The node must be up when rescanned for the
      inventory information to be updated.
    </p>

    <% if (node != null) { %>
    <p>
      <a href="element/rescan.jsp?node=<%= node.getId() %>">Rescan this node</a>
    </p>
    <p>
      <a href="element/node.jsp?node=<%= node.getId() %>">Return to node page</a>
    </p>
    <% } %>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

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
	isErrorPage="true"
	import="org.opennms.web.admin.nodeManagement.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
    NoManagedInterfacesException nmie = ExceptionUtils.getRootCause(exception, NoManagedInterfacesException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("No Managed Interfaces for Node")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>No Managed Interfaces for Node</h1>

<p>
  The selected node has no interfaces that are included in an OpenNMS polling package. Therefore no interfaces or services on this node can be
  managed or unmanaged. Your OpenNMS administrator can add interfaces to a polling package.</p>

  <% if (nmie.getNodeListUri() != null) { %>
  <p>
  To select a different node, you can browse the
  <a href="<%=nmie.getNodeListUri()%>">node list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

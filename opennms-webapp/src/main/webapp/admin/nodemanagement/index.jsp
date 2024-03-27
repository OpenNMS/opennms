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
			org.opennms.netmgt.model.OnmsNode,
			org.opennms.web.servlet.MissingParameterException"
%>

<%
    int nodeId = -1;
    
    String nodeIdString = request.getParameter("node");

    if (nodeIdString == null) {
        throw new MissingParameterException("node");
    }
    try {
        nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    } catch (NumberFormatException numE) {
        throw new ServletException(numE);
    }

    if (nodeId < -1) {
        throw new ServletException("Invalid node ID.");
    }
        
    //get the database node info
    OnmsNode node_db = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
    if (node_db == null) {
        // XXX handle this WAY better, very awful
        throw new ServletException("No such node in database");
    }
    
    boolean isRequisitioned = (node_db.getForeignSource() != null && node_db.getForeignSource().length() != 0);
%>

<%@page import="org.opennms.core.resource.Vault"%>
<%@ page import="org.opennms.netmgt.model.OnmsIpInterface" %>
<%@ page import="org.opennms.core.utils.InetAddressUtils" %>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Node Management")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Node Management")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

  function getInterfacesPost()
  {
      document.getInterfaces.submit();
  }
</script>

<form method="post" name="getInterfaces"
      action="admin/nodemanagement/getInterfaces">
  <input name="node" value="<%=nodeId%>" type="hidden"/>
</form>

<h4>Node: <%=WebSecurityUtils.sanitizeString(node_db.getLabel())%> (ID: <%=node_db.getId()%>)</h4>
<% if (isRequisitioned) { %>
<h4><em>Created via requisition <strong><%=node_db.getForeignSource()%></strong> (foreignId: <strong><%=node_db.getForeignId()%></strong>)</em></h4>
<% } else { %>
<h4><em>Not a member of any requisition</em></h4>
<% } %>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Admin Options</span>
      </div>
      <div class="card-body">
  <% if (!isRequisitioned) { %>
  <p>
    <a href="admin/nodelabel.jsp?node=<%=nodeId%>">Change Node Label</a>
  </p>
  <% } %>

  <p>
    <a href="javascript:getInterfacesPost()">Manage and Unmanage Interfaces
    and Services</a>
  </p>

  <p>
    <a href="admin/snmpInterfaces.jsp?node=<%=nodeId%>&nodelabel=<%=java.net.URLEncoder.encode(node_db.getLabel(), "UTF-8")%>">
    Configure SNMP Data Collection per Interface</a>
  </p>

  <%
    final OnmsIpInterface onmsIpInterface = node_db.getPrimaryInterface();
    if (onmsIpInterface != null) {
      final String primaryIpAddress = InetAddressUtils.str(onmsIpInterface.getIpAddress());
      %>
        <p>
          <a href="admin/snmpConfig?action=get&ipAddress=<%=primaryIpAddress%>#updateForm">Configure SNMP Community Strings</a>
        </p>
      <%
    }
  %>
  <% if (!isRequisitioned) { %>
  <p>
    <a href="admin/nodemanagement/deletenode.jsp?node=<%=nodeId%>">Delete
    Node</a>
  </p>
  <% } %>

  <p>
    <a href="admin/nodemanagement/setPathOutage.jsp?node=<%=nodeId%>">
    Configure Path Outage</a>
  </p>
  
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
      

  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Option Descriptions</span>
      </div>
      <div class="card-body">
  <% if (!isRequisitioned) { %>
  <p>
    <b>Change Node Label</b> allows administrators either to specify a node 
    name, or let the system to automatically select the node name.
  </p>
  <% } %>

  <p>
    <b>Manage and Unmanage Interfaces and Services</b> allows you to make
    small tweaks to what OpenNMS monitors.
    When OpenNMS was first started, the nodes, interfaces, and services
    in the network were <em>provisioned</em> either via an automatic process
    such as discovery or via one or more <em>requisitions</em>. As your
    network grows and changes, the IP address ranges you want to manage, as
    well as the interfaces and services within those ranges, may change. For
    requisitioned nodes, it's usually better to make changes in the requisition
    rather than via the options presented below. Even for auto-provisioned nodes,
    configuring foreign-source policies will afford more flexibility, especially
    when dealing with large numbers of nodes.
  </p>

  <p>
    <b>Manage SNMP Data Collection per Interface</b> allows you
    to configure which non-IP interfaces are targeted for SNMP Data Collection.
    For small, per-node tweaks use this mechanism. For more system-wide changes,
    consider configuring foreign-source policies for more flexibility.
  </p>
        
  <p>
    <% if (!isRequisitioned) { %>
    <b>Delete Node</b> allows you to delete a current node permanently
    from the database.
    <% } %>
  </p>
        
  <p>
    <b>Configure Path Outage</b> Set the critical path and service to test
    before sending Node Down notifications for this node.
  </p>
  
  <% if (isRequisitioned) { %>
  <p>
    <b>To delete this node or change its label</b>, either directly edit the
    &quot;<em><%=node_db.getForeignSource() %></em>&quot; requisition and synchronize it or,
    if that requisition is automatically generated by an integration process,
    contact your OpenNMS administrator for assistance.
  </p>
  <% } %>
  
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

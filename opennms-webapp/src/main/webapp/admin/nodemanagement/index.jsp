<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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
<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Node Management" />
  <jsp:param name="headTitle" value="Node Management" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="nodemanagement" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Node Management" />
</jsp:include>

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

<h4>Node: <%=node_db.getLabel()%> (ID: <%=node_db.getId()%>)</h4>
<% if (isRequisitioned) { %>
<h4><em>Created via requisition <strong><%=node_db.getForeignSource()%></strong> (foreignId: <strong><%=node_db.getForeignId()%></strong>)</em></h4>
<% } else { %>
<h4><em>Not a member of any requisition</em></h4>
<% } %>

<div class="row">
  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Admin Options</h3>
      </div>
      <div class="panel-body">
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
    <a href="admin/snmpInterfaces.jsp?node=<%=nodeId%>&nodelabel=<%=node_db.getLabel()%>">
    Configure SNMP Data Collection per Interface</a>
  </p>

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
  
      <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) { %>
  <p>
    <a href="admin/rancid/rancidAdmin.htm?node=<%=nodeId%>">
    Configure Rancid Integration</a>
  
  </p>

  <p>
    <a href="admin/storage/storageAdmin.htm?node=<%=nodeId%>">
    Configure Software Images</a>
  </p>

  <% } %>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
      

  <div class="col-md-6">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Option Descriptions</h3>
      </div>
      <div class="panel-body">
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
  
        <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) { %>
  <p>
    <b>Configure Rancid</b> Configure RANCID group router.db files and rancid cloginrc
     authentication data.
  </p>

  <p>
    <b>Configure Software Images</b> Add and Delete software images.
  </p>

  <% } %>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

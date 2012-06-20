<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
%>

<%@page import="org.opennms.core.resource.Vault"%>
<jsp:include page="/includes/header.jsp" flush="false" >
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

<h2>Node: <%=node_db.getLabel()%></h2>

<div class="TwoColLAdmin">
  <h3>Admin Options</h3>

  <p>
    <a href="admin/nodelabel.jsp?node=<%=nodeId%>">Change Node Label</a>
  </p>

  <p>
    <a href="javascript:getInterfacesPost()">Manage and Unmanage Interfaces
    and Services</a>
  </p>

  <p>
    <a href="admin/snmpGetInterfaces?node=<%=nodeId%>&nodelabel=<%=node_db.getLabel()%>">
    Configure SNMP Data Collection per Interface</a>
  </p>

  <% if (node_db.getForeignSource() == null || node_db.getForeignSource().length() == 0) { %>
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
</div>
      
<div class="TwoColRAdmin">

  <h3>Option Descriptions</h3>

  <p>
    <b>Change Node Label</b> allows administrators either to specify a node 
    name, or let the system to automatically select the node name.
  </p>

  <p>
    When OpenNMS was first started, the nodes, interfaces, and services
    in the network were either <em>discovered</em> automatically or added
    via one or more <em>requisitions</em>. As your network grows and changes, 
    the TCP/IP ranges you want to manage, as well as the interfaces and
    services within those ranges, may change. For requisitioned nodes, it's
    usually better to make changes in the requisition rather than via the
    options presented below.
    <b>Manage and Unmanage Interfaces and Services</b> allows you to change
    your OpenNMS configuration along with your network.
  </p>

  <p>
    <b>Manage SNMP Data Collection per Interface</b> allows you
    to configure which non-IP interfaces are used in SNMP Data Collection.
  </p>
        
  <p>
    <% if (node_db.getForeignSource() == null || node_db.getForeignSource().length() == 0) { %>
    <b>Delete Node</b> allows you to delete a current node permanently
    from the database.
    <% } else { %>
    This node was imported via a provisioning Requisition and
    must therefore be deleted from its requisition rather than through
    this interface. Otherwise, <b>Delete Node</b> would appear on this page.
    <% } %>
  </p>
        
  <p>
    <b>Configure Path Outage</b> Set the critical path and service to test
    before sending Node Down notifications for this node.
  </p>
  
        <% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.rancidIntegrationEnabled"))) { %>
  <p>
    <b>Configure Rancid</b> Configure rancid group router.db files and rancid cloginrc
     authentication data.
  </p>

  <p>
    <b>Configure Software Images</b> Add and Delete software images.
  </p>

  <% } %>
  
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>

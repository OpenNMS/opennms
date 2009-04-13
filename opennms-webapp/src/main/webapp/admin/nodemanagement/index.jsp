<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Apr 17: Added 'Configure Path Outage' link
// 2004 Feb 12: Fix page format issues.
// 2004 Jan 15: page created.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
			org.opennms.web.element.*,
        	org.opennms.web.MissingParameterException"
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
    Node node_db = NetworkElementFactory.getNode(nodeId);
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

<script language="Javascript" type="text/javascript" >

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
    in the network were <em>discovered</em>. As your network grows and changes, 
    the TCP/IP ranges you want to manage, as well as the interfaces and
    services within those ranges, may change.
    <b>Manage and Unmanage Interfaces and Services</b> allows you to change
    your OpenNMS configuration along with your network.
  </p>

  <p>
    <b>Manage SNMP Data Collection per Interface</b> allows you
    to configure which non-IP interfaces are used in SNMP Data Collection.
  </p>
        
  <p>
    <% if (node_db.getForeignSource() == null || node_db.getForeignSource().length() == 0) { %>
    <b>Delete Node</b> allows you to permanently delete a current node
    from the database.
    <% } else { %>
    This node was imported via a Provisioning Group or the Model Importer and
    must therefore be deleted from its provisioning source rather than through
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
  <% } %>
  
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>

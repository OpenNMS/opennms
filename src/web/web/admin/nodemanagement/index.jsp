<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
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

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*,org.opennms.web.event.*,java.net.*" %>

<%
    int nodeId = -1;
    
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }
    try {
        nodeId = Integer.parseInt( nodeIdString );
    } catch (NumberFormatException numE) {
        throw new ServletException(numE);
    }

    if (nodeId < -1)
        throw new ServletException("Invalid node ID.");
        
    //get the database node info
    Node node_db = NetworkElementFactory.getNode( nodeId );
    if( node_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }
%>

<html>
<head>
  <title>Node Management | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />

<script language="Javascript" type="text/javascript" >

  function getInterfacesPost()
  {
      document.getInterfaces.submit();
  }
</script>
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Node Management"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node Management" />
  <jsp:param name="location" value="nodemanagement" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<FORM METHOD="POST" NAME="getInterfaces" ACTION="admin/nodemanagement/getInterfaces">
  <input name="node" value=<%=nodeId%> type="hidden"/>
</FORM>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp;&nbsp; </td>
    
    <td width="100%" valign="top">
      <h2>Node: <%=node_db.getLabel()%></h2>
      <hr>
    </td>
  </tr>
</table>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp; &nbsp; </td>
      
    <td valign="top">
      <h3>Admin Options</h3>

      <p>
        <a href="admin/nodelabel.jsp?node=<%=nodeId%>">Change Node Label</a>
      <p>
        <a href="javascript:getInterfacesPost()">Manage and Unmanage Interfaces and Services</a>
<!--
      <p>
        <a href="admin/snmpselect.jsp?node=<%=nodeId%>">Configure SNMP Data Collection per Interface</a>
-->
      <p>
        <a href="admin/nodemanagement/deletenode.jsp?node=<%=nodeId%>">Delete Node</a>
      
    </td>

    <td> &nbsp; </td>

    <td valign="top" width="60%">
      <h3>Option Descriptions</h3>

        <p><b>Change Node Label</b> allows administrators either to specify a node 
            name, or let the system to automatically select the node name.
        </p>

        <p>When OpenNMS was first started, the nodes, interfaces, and services
            in the network were <em>discovered</em>. As your network grows and changes, 
            the TCP/IP ranges you want to manage, as well as the interfaces and services 
            within those ranges, may change. <b>Manage and Unmanage Interfaces and Services
            </b> allows you to change your OpenNMS configuration along with your network.
        </p>

	<p><b>Manage SNMP Data Collection per Interface</b>: This interface will allow you
	to configure which non-IP interfaces are used in SNMP Data Collection.
	</p>
        
        <p><b>Delete Node</b> allows you to permanently delete current node from database.
        </p>

    </td>
    
    <td> &nbsp;&nbsp; </td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" >
  <jsp:param name="location" value="nodemanagement" />
</jsp:include>
</body>
</html>

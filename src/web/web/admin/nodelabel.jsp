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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.blast.com/
//
-->
<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.utils.NodeLabel,org.opennms.core.resource.Vault,java.util.*,java.sql.*" %>

<%!
    HashMap typeMap;

    public void init() {
        typeMap = new HashMap();
        typeMap.put( new Character(NodeLabel.SOURCE_USERDEFINED), "User defined" );
        typeMap.put( new Character(NodeLabel.SOURCE_NETBIOS),     "Windows/NETBIOS Name" );
        typeMap.put( new Character(NodeLabel.SOURCE_HOSTNAME),    "DNS Hostname" );
        typeMap.put( new Character(NodeLabel.SOURCE_SYSNAME),     "SNMP System Name" );
        typeMap.put( new Character(NodeLabel.SOURCE_ADDRESS),     "IP Address" );
        typeMap.put( new Character(NodeLabel.SOURCE_UNKNOWN),     "Uknown" );
    }
%>

<%
    String nodeIdString = request.getParameter( "node" );

    Connection myConn = Vault.getDbConnection();
    
    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = Integer.parseInt( nodeIdString );

    NodeLabel currentLabel = NodeLabel.retrieveLabel( nodeId, myConn );
    NodeLabel autoLabel = NodeLabel.computeLabel( nodeId, myConn );

    if( currentLabel == null || autoLabel == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }
%>

<html>
<head>
  <title> Change Node Label | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Change Node Label"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Change Node Label" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td valign="top" >
      <h3>Current Label</h3>
        <p><a href="element/node.jsp?node=<%=nodeId%>" title="More information for this node"><%=currentLabel.getLabel()%></a> (<%=typeMap.get(new Character(currentLabel.getSource()))%>)
        </p>

      <hr>

      <h3>Choose a New Label</h3>
        <p>You can either specify a name or allow the system to automatically
           select the name.          
        <form action="admin/nodeLabelChange" method="POST">
          <input type="hidden" name="node" value="<%=nodeId%>" />

          <table width="50%" border="0" cellspacing="0" cellpadding="2">
            <tr>
              <td colspan="2"><strong>User Defined</strong></td>
            </tr>
            <tr>
              <td width="5%"><input type="radio" name="labeltype" value="user" <%=(currentLabel.getSource() == NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> /></td>
              <td><input type="text" name="userlabel" value="<%=currentLabel.getLabel()%>" maxlength="255" size="32"/></td>
            </tr>
            <tr>
              <td colspan="2"><strong>Automatic</strong></td>
            </tr>
            <tr>
              <td width="5%"><input type="radio" name="labeltype" value="auto" <%=(currentLabel.getSource() != NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> /></td>
              <td><%=autoLabel.getLabel()%> (<%=typeMap.get(new Character(autoLabel.getSource()))%>)</td>
            </tr>
            <tr>
              <td colspan="2">
                <input type="submit" value="Change Label" />
                <input type="reset" />
              </td>
            </tr>
          </table>
        </form>
        </p>
    </td>    
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

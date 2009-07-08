<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.netmgt.utils.NodeLabel,
		org.opennms.core.resource.Vault,
        	org.opennms.web.MissingParameterException,
        	org.opennms.web.WebSecurityUtils,
		java.util.*,
		java.sql.*
	"
%>

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
        throw new MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    NodeLabel currentLabel = NodeLabel.retrieveLabel( nodeId, myConn );
    NodeLabel autoLabel = NodeLabel.computeLabel( nodeId, myConn );

    if( currentLabel == null || autoLabel == null ) {
        // XXX handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Change Node Label" />
  <jsp:param name="headTitle" value="Change Node Label" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Change Node Label" />
</jsp:include>

<h3>Current Label</h3>
<p>
  <a href="element/node.jsp?node=<%=nodeId%>" title="More information for this node"><%=currentLabel.getLabel()%></a> (<%=typeMap.get(new Character(currentLabel.getSource()))%>)
</p>

<hr>

<h3>Choose a New Label</h3>

<p>
  You can either specify a name or allow the system to automatically
  select the name.          
</p>

  <form action="admin/nodeLabelChange" method="post">
    <input type="hidden" name="node" value="<%=nodeId%>" />

  <strong>User Defined</strong>
  <br>
  <input type="radio" name="labeltype" value="user" <%=(currentLabel.getSource() == NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> />
  <input type="text" name="userlabel" value="<%=currentLabel.getLabel()%>" maxlength="255" size="32"/>

  <br>
  <br>

  <strong>Automatic</strong>
  <br>
  <input type="radio" name="labeltype" value="auto" <%=(currentLabel.getSource() != NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> />

    <%=autoLabel.getLabel()%> (<%=typeMap.get(new Character(autoLabel.getSource()))%>)

  <br>
  <br>

  <input type="submit" value="Change Label" />
  <input type="reset" />
  </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

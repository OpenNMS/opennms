<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
	import="org.opennms.netmgt.utils.NodeLabel,
		org.opennms.web.servlet.MissingParameterException,
		org.opennms.core.utils.WebSecurityUtils,
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

    
    if( nodeIdString == null ) {
        throw new MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    NodeLabel currentLabel = NodeLabel.retrieveLabel( nodeId );
    NodeLabel autoLabel = NodeLabel.computeLabel( nodeId );

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
  <br/>
  <input type="radio" name="labeltype" value="user" <%=(currentLabel.getSource() == NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> />
  <input type="text" name="userlabel" value="<%=currentLabel.getLabel()%>" maxlength="255" size="32"/>

  <br/>
  <br/>

  <strong>Automatic</strong>
  <br/>
  <input type="radio" name="labeltype" value="auto" <%=(currentLabel.getSource() != NodeLabel.SOURCE_USERDEFINED) ? "checked" : ""%> />

    <%=autoLabel.getLabel()%> (<%=typeMap.get(new Character(autoLabel.getSource()))%>)

  <br/>
  <br/>

  <input type="submit" value="Change Label" />
  <input type="reset" />
  </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

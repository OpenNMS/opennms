<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of last active inventories.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*" %>


<%
    statusMap = new HashMap();
  	statusMap.put( new Character('A'), "Active" );
    statusMap.put( new Character(' '), "Unknown" );
    statusMap.put( new Character('D'), "Deleted" );
    statusMap.put( new Character('N'), "Not Active" );

    //required parameter node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node");
    }
        
    int nodeId = Integer.parseInt(nodeIdString);
    
    //gets active route entry on node
    
   	StpNode[] stpnodes = ExtendedNetworkElementFactory.getStpNode(nodeId);

%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  
    <tr bgcolor="#999999">
       <td colspan="11"><b>Node Bridge Info</b></td>
     </tr>

<% if(stpnodes.length == 0) { %>
  <tr>
    <td BGCOLOR="#cccccc" colspan="11">There have been no bridge info on this node.</b></a></td>
  </tr>
<% } else { %>
              <tr>
                <td bgcolor="red"><b>Vlan</b></td>			  
                <td><b>Base Address</b></td>
                <td><b>Type</b></td>
                <td><b>Stp Proto Spec</b></td>
                <td><b>Port Num.</b></td>
                <td><b>Status</b></td>
                <td><b>Stp Root</b></td>
                <td><b>Stp Priority</b></td>
                <td><b>Stp Root Cost</b></td>
                <td><b>Stp Root Port</b></td>
                <td><b>Last Poll Time</b></td>
              </tr>
              <% for (int i=0; i < stpnodes.length;i++) { %>
			  <tr>
                <td bgcolor="red"><%=stpnodes[i].get_basevlan()%></td>			  
                <td><%=stpnodes[i].get_basebridgeaddress()%></td>
                <td><%=BRIDGE_BASE_TYPE[stpnodes[i].get_basetype()]%></td>
                <td><%=STP_PROTO_TYPE[stpnodes[i].get_stpprotocolspecification()]%></td>
                <td><%=stpnodes[i].get_basenumports()%></td>
                <td><%=getStatusString(stpnodes[i].get_status())%></td>
				<% if (stpnodes[i].get_stprootnodeid() != 0) { 
					   	Node node = NetworkElementFactory.getNode(stpnodes[i].get_stprootnodeid());
				%>
                <td><a href="element/node.jsp?node=<%=stpnodes[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpnodes[i].get_stpdesignatedroot()%></strong>)</td>
				<% } else { %>
				<td><%=stpnodes[i].get_stpdesignatedroot()%></td>
				<% } %>
                <td><%=stpnodes[i].get_stppriority()%></td>
                <td><%=stpnodes[i].get_stprootcost()%></td>
                <td><%=stpnodes[i].get_stprootport()%></td>
                <td><%=stpnodes[i].get_lastPollTime()%></td>
              </tr>
              <% } %>
       <% } %>
                     
</table>      

<%!
    public static HashMap statusMap;

    
    public String getStatusString( char c ) {
        return( (String)statusMap.get( new Character(c) ));
    }

  public static final String[] BRIDGE_BASE_TYPE = new String[] {
    "&nbsp;",           //0 (not supported)
    "UnKnown",          //1
    "Trasparent-Only",  //2
    "Sourceroute-Only", //3
    "Src"               //4
  };

  public static final String[] STP_PROTO_TYPE = new String[] {
    "&nbsp;",           //0 (not supported)
    "UnKnown",          //1
    "DEC Lan Bridge",  //2
    "IEEE 802.1d", //3
  };

%>

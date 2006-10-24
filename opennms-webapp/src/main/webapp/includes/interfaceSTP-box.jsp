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
  abbreviated list of links.
  
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
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String ifindexString = request.getParameter( "ifindex" );
    
    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node", new String[] { "node", "intf", "ifindex?"} );
    }

    if( ipAddr == null ) {
        throw new org.opennms.web.MissingParameterException( "intf", new String[] { "node", "intf", "ifindex?" } );
    }

    int nodeId = -1;
    int ifindex = -1;
    
    try {
        nodeId = Integer.parseInt( nodeIdString );
        if (ifindexString != null) 
            ifindex = Integer.parseInt( ifindexString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer", e );
    }


// find STP interface info
    StpInterface[] stpifs = NetworkElementFactory.getStpInterface(nodeId,ifindex);

%>
<h3>Interface Spanning Tree Protocol Info</h3>
<table class="standard">
  
  
<% if(stpifs.length == 0) { %>
  <tr>
    <td class="standardheaderplain" colspan="11">There have been no STP Interfaces info on this node.</td>
  </tr>
<% } else { %>
        <% for (int i=0; i < stpifs.length;i++) { %>
	<tr>
        <td class="standard">VlanIdentifier</td>			  
		<td class="standard" bgcolor="<%=getVlanColorIdentifier(stpifs[i].get_stpvlan())%>"><%=stpifs[i].get_stpvlan()%>
		</td>			  
	</tr>
	<tr>
        <td  class="standard">STP Port Status</td>
        <td  class="standard"><%=STP_PORT_STATUS[stpifs[i].get_stpportstate()]%></td>
	</tr>
	<tr>
        <td  class="standard">Path Cost</td>
        <td  class="standard"><%=stpifs[i].get_stpportpathcost()%></td>
	</tr>
	<tr>
        <td  class="standard">Stp Root</td>
	<% if (stpifs[i].get_stprootnodeid() != 0) { 
	Node node = NetworkElementFactory.getNode(stpifs[i].get_stprootnodeid());
	%>
	<td  class="standard"><a href="element/node.jsp?node=<%=stpifs[i].get_stprootnodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedroot()%></strong>)</td>
	<% } else { %>
	<td  class="standard"><%=stpifs[i].get_stpdesignatedroot()%></td>
	<% } %>
	</tr>
	<tr>
        <td  class="standard">Designated Bridge</td>
	<% if (stpifs[i].get_stpbridgenodeid() != 0) { 
	Node node = NetworkElementFactory.getNode(stpifs[i].get_stpbridgenodeid());
	%>
	<td  class="standard"><a href="element/node.jsp?node=<%=stpifs[i].get_stpbridgenodeid()%>"><%=node.getLabel()%></a><br>(<strong><%=stpifs[i].get_stpdesignatedbridge()%></strong>)</td>
	<% } else {%>
	<td  class="standard"><%=stpifs[i].get_stpdesignatedbridge()%></td>
	<% } %>
	</tr>
	<tr>
        <td  class="standard">Designated Port</td>
        <td><%=stpifs[i].get_stpdesignatedport()%></td>
	</tr>
	<tr>
        <td  class="standard">Designated Cost</td>
        <td><%=stpifs[i].get_stpportdesignatedcost()%></td>
	</tr>
	<tr>
        <td  class="standard">Information Status</td>
        <td><%=getStatusString(stpifs[i].get_status())%></td>
	</tr>
	<tr>
        <td  class="standard">Last Poll Time</td>
        <td  class="standard"><%=stpifs[i].get_lastPollTime()%></td>
        </tr>
        <% } %>
<% } %>
                     
</table>      

<%!
    public static HashMap statusMap;

     public String getVlanColorIdentifier( int i ) {
        int red = 128;
        int green = 128;
        int blue = 128;
        int redoffset = 47;
        int greenoffset = 29;
        int blueoffset = 23;
        if (i == 0) return "";
        if (i == 1) return "#FFFFFF";
        red = (red + i * redoffset)%255;
        green = (green + i * greenoffset)%255;
        blue = (blue + i * blueoffset)%255;
        if (red < 64) red = red+64;
        if (green < 64) green = green+64;
        if (blue < 64) blue = blue+64;
        return "#"+Integer.toHexString(red)+Integer.toHexString(green)+Integer.toHexString(blue);
    }
    
    public String getStatusString( char c ) {
        return( (String)statusMap.get( new Character(c) ));
    }

  public static final String[] STP_PORT_STATUS = new String[] {
    "&nbsp;",     //0 (not supported)
    "Disables",   //1
    "Blocking",   //2
    "Listening",  //3
    "Learning",   //4
    "Forwarding", //5
    "Broken",     //6
  };

%>

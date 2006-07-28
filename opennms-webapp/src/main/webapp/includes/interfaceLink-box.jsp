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
    } catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer", e );
    }


// find links

    DataLinkInterface[] dl_if = NetworkElementFactory.getDataLinksOnInterface(nodeId,ifindex);
	
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  

<% if(dl_if == null  || dl_if.length == 0) { %>
  <tr>
    <td BGCOLOR="#999999" colspan="4"><b>There have been no Link Info on this Interface</b></td>
  </tr>
<% } else { %>
  <tr> 
    <td BGCOLOR="#999999" colspan="4"><b>Link Node/Interface</b></td>
  </tr>
  <tr bgcolor="#999999">
    <td><b>Node</b></td>
    <td><b>Interface</b></td>
    <td><b>Status</b></td>
    <td><b>Last Poll Time</b></td>
  </tr>
  <% for( int i=0; i < dl_if.length; i++ ) { %>
    <% Interface iface = null; %>
    <tr>
    <td><a href="element/linkednode.jsp?node=<%=dl_if[i].get_nodeparentid()%>"><%=NetworkElementFactory.getNodeLabel(dl_if[i].get_nodeparentid())%></a></td>
	<td>
    <% if( "0.0.0.0".equals( dl_if[i].get_parentipaddr() )) { %>
		<% if ( dl_if[i].get_parentifindex() == 0) {
			iface = NetworkElementFactory.getInterface(dl_if[i].get_nodeparentid(),dl_if[i].get_parentipaddr());
		 } else {
		   iface = NetworkElementFactory.getInterface(dl_if[i].get_nodeparentid(),dl_if[i].get_parentipaddr(),dl_if[i].get_parentifindex());
         }
		%>
<a href="element/interface.jsp?node=<%=dl_if[i].get_nodeparentid()%>&intf=<%=dl_if[i].get_parentipaddr()%>&ifindex=<%=dl_if[i].get_parentifindex()%>">Non-IP</a>
<%=" (ifIndex: "+dl_if[i].get_parentifindex()+"-"+iface.getSnmpIfDescription()+")"%>
    <% } else { %>  
		<%if(dl_if[i].get_parentipaddr()!=null){%>
<a href="element/interface.jsp?node=<%=dl_if[i].get_nodeparentid()%>&intf=<%=dl_if[i].get_parentipaddr()%>"><%=dl_if[i].get_parentipaddr()%></a>
		<% }else{
	   	out.print("&nbsp;");
	   	}%>      
   <% } %>
   </td>

      <td><%=getStatusString(dl_if[i].get_status())%></td>
      <td><%=dl_if[i].get_lastPollTime()%></td>
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

%>

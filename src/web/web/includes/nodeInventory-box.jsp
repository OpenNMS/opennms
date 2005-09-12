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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of outages.  All current outages and any outages resolved
  within the last 24 hours are shown.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.inventory.*,java.util.*" %>

<%! 
   InventoryFactory invFactory = new InventoryFactory();
%>

<%
    //required parameter node
    String nodeIdString = request.getParameter("node");
    
    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException("node");
    }
        
    int nodeId = Integer.parseInt(nodeIdString);
    
    //determine yesterday's respresentation
    Calendar cal = new GregorianCalendar();
    cal.add( Calendar.DATE, -1 );
    Date yesterday = cal.getTime();

    //gets all current Acive inventory that have been within the
    //the last 24 hours
       Inventory[] inventories = this.invFactory.getActiveInventoryOnNode(nodeId, yesterday);
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  

<% if(inventories.length == 0) { %>
  <tr>
    <td BGCOLOR="#999999" colspan="5">There have been no inventory configuration saved on this node in the last 24 hours.</b></a></td>
  </tr>
<% } else { %>
  <tr> 
    <td BGCOLOR="#999999" colspan="5"><b>Recent Active Inventories</b></td>
  </tr>

  <tr bgcolor="#999999">
    <td><b>Inventory</b></td>
    <td><b>Create Time</b></td>
    <td><b>Last Poll Time</b></td>
  </tr>

  <% for( int t=0; t < inventories.length; t++ ) { %>
     <tr>
	<%
	String nodeLabel = inventories[t].getNodeLabel();
	String pathFile = inventories[t].getPathToFile();
	String inventName = inventories[t].getName();
	%>
    <td><a href="<%=request.getContextPath()%>/conf/showinventory.jsp?file=<%=pathFile%>&category=<%=inventName%>&lastpolltime=<%=inventories[t].getLastPollTime()%>&nodelabel=<%=nodeLabel%>"> <%=inventName%></a></td>
    <td><%=org.opennms.netmgt.EventConstants.formatToUIString(inventories[t].getCreateTime())%></td>
    <td><%=org.opennms.netmgt.EventConstants.formatToUIString(inventories[t].getLastPollTime())%></td>

    </tr>
  <% } %>
<% } %>

</table>      

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
  abbreviated list of outages.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.outage.*" %>

<%! 
    public static final int ROW_COUNT = 12;
    OutageModel model = new OutageModel();    
%>

<%
    OutageSummary[] summaries = this.model.getCurrentOutageSummaries();
%>

<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
  <tr> 
    <td BGCOLOR="#999999"><a href="outage/current.jsp"><b>Nodes with Outages</b></a></td>
  </tr>

<% for( int i=0; i < ROW_COUNT; i++ ) { %>
  <% if( i < summaries.length ) { %>
    <% OutageSummary summary = summaries[i];
       String nodeLabel = summary.getNodeLabel();
       int nodeId = summary.getNodeId();
    %>
    <tr>
      <td align="left"><a href="element/node.jsp?node=<%=nodeId%>"><nobr><%=nodeLabel%></nobr></a></td>
    </tr>
  <% } else { %>
    <tr><td>&nbsp;</td></tr>
  <% } %>
<% } %>

<% if( summaries.length > ROW_COUNT ) { %>
  <tr>
    <td>
      <a HREF="outage/index.jsp"><%=summaries.length - ROW_COUNT%> more</a>
    </td>
  </tr>
<% } %>
</table>      

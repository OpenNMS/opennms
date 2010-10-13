<%--

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
// 2002 Sep 24: Added a "select" parameter for SNMP collection and config page.
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
	import="java.io.File,
		java.util.*,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.admin.nodeManagement.*
	"
%>

<%!
    int interfaceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List nodes = null;
    Integer lineItems= new Integer(0);
    
    interfaceIndex = 0;
    
    if (userSession == null) {
	throw new ServletException("session is null");
    }

    nodes = (List)userSession.getAttribute("listAllnodes.snmpmanage.jsp");
    lineItems = (Integer)userSession.getAttribute("lineNodeItems.snmpmanage.jsp");

    if (nodes == null) {
	throw new ServletException("session attribute listAllnodes.snmpmanage.jsp is null");
    }
    if (lineItems == null) {
	throw new ServletException("session attribute lineNodeItems.snmpmanage.jsp is null");
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage SNMP by Interface" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Manage SNMP by Interface" />
</jsp:include>

<%
  int midNodeIndex = 1;
  
  if (nodes.size() > 1)
  {
    midNodeIndex = nodes.size()/2;
  }
%>

<h3>Manage SNMP Data Collection per Interface</h3>

<p>
  In the datacollection-config.xml file, for each different collection
  scheme there is a parameter called <code>snmpStorageFlag</code>.  If
  this value is set to "primary", then only values pertaining to the
  node as a whole or the primary SNMP interface will be stored in the
  system. If this value is set to "all", then all interfaces for which
  values are collected will be stored.
</p>

<p>
  If this parameter is set to "select", then the interfaces for which
  data is stored can be selected.  By default, only information from
  Primary and Secondary SNMP interfaces will be stored, but by using
  this interface, other non-IP interfaces can be chosen.
</p>

<p>
  Simply select the node of interest below, and follow the instructions
  on the following page.
</p>

      
   <% if (nodes.size() > 0) { %>
	<div id="contentleft">
          <table class="standardfirst">
            <tr>
              <td class="standardheader" width="5%" align="center">Node ID</td>
              <td class="standardheader" width="10%" align="center">Node Label</td>
            </tr>
            <%=buildTableRows(nodes, 0, midNodeIndex)%>
            
          </table>
	</div>
          <% } /*end if*/ %>
        
      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
	<div id="contentright">
          <table class="standardfirst">
            <tr>
              <td class="standardheader" width="5%" align="center">Node ID</td>
              <td class="standardheader" width="10%" align="center">Node Label</td>
            </tr>
            
            <%=buildTableRows(nodes, midNodeIndex, nodes.size())%>
               
          </table>
	</div>
        <% } /*end if */ %>

<jsp:include page="/includes/footer.jsp" flush="true"/>

<%!
      public String buildTableRows(List nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                SnmpManagedNode curNode = (SnmpManagedNode)nodes.get(i);
                String nodelabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(curNode.getNodeID());
		int nodeid = curNode.getNodeID();
                 
          row.append("<tr>\n");
          row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
	  row.append(nodeid);
          row.append("</td>\n");
          row.append("<td class=\"standard\" width=\"10%\" align=\"left\">");
          row.append("<a href=\"admin/snmpGetInterfaces?node=");
	  row.append(nodeid);
          row.append("&nodelabel=");
	  row.append(nodelabel);
          row.append("\">");
	  row.append(nodelabel);
          row.append("</a>");
          row.append("</td>\n");
          row.append("</tr>\n");
          } /* end i for */
          
          return row.toString();
      }
      
%>

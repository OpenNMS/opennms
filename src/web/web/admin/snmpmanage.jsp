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

<%@page language="java" contentType="text/html" session="true" import="java.io.File,java.util.*,org.opennms.web.element.NetworkElementFactory,org.opennms.web.admin.nodeManagement.*" %>

<%!
    int interfaceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List nodes = null;
    List interfaces = null;
    Integer lineItems= new Integer(0);
    Integer lineIntItems= new Integer(0);
    
    interfaceIndex = 0;
    
    if (userSession != null)
    {
  	nodes = (List)userSession.getAttribute("listAllnodes.snmpmanage.jsp");
        lineItems = (Integer)userSession.getAttribute("lineNodeItems.snmpmanage.jsp");
  	interfaces = (List)userSession.getAttribute("listAllinterfaces.snmpmanage.jsp");
        lineIntItems = (Integer)userSession.getAttribute("lineIntItems.snmpmanage.jsp");
    }
%>

<html>
<head>
  <title>Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

  function applyChanges()
  {
      if (confirm("Are you sure you want to proceed? This action will permanently delete the checked nodes and cannot be undone."))
      {
          document.deleteAll.submit();
      }
  }
  
  function cancel()
  {
      document.deleteAll.action="admin/index.jsp";
      document.deleteAll.submit();
  }
  
  function checkAll()
  {
      for (var c = 0; c < document.deleteAll.elements.length; c++)
      {  
          if (document.deleteAll.elements[c].type == "checkbox")
          {
              document.deleteAll.elements[c].checked = true;
          }
      }
  }
  
  function uncheckAll()
  {
      for (var c = 0; c < document.deleteAll.elements.length; c++)
      {  
          if (document.deleteAll.elements[c].type == "checkbox")
          {
              
              document.deleteAll.elements[c].checked = false;
          }
      }
  }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='admin/index.jsp'>Admin</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Manage SNMP by Interface"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage SNMP by Interface" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<%
  int midNodeIndex = 1;
  
  if (nodes.size() > 1)
  {
    midNodeIndex = nodes.size()/2;
  }
%>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  
  <tr>
    <td> &nbsp; </td>  
    
    <td>
    <h3>Manage SNMP Data Collection per Interface</h3>

    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <td colspan="3"> 
	<P>In the datacollection-config.xml file, for each different collection scheme there is a
	parameter called <code>snmpStorageFlag</code>. If this value is set to "primary", then
	only values pertaining to the node as a whole or the primary SNMP interface will be
	stored in the system. If this value is set to "all", then all interfaces for which values are
	collected will be stored.
	</P>
	<P>If this parameter is set to "select", then the interfaces for which data is stored can be
	selected. By default, only information from Primary and Secondary SNMP interfaces will be stored,
	but by using this interface, other non-IP interfaces can be chosen.
	</P>
	<P>
	Simply select the node of interest below, and follow the instructions on the following page.
	</P>
        </td>
      </tr>
	
      <TR>
      <td>&nbsp;</td>
      </tr>

   </tr> 
      
   <% if (nodes.size() > 0) { %>
   <tr>
        <td width="49%" align="left" valign="top">
          <table border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="5%" align="center"><b>Node ID</b></td>
              <td width="10%" align="center"><b>Node Label</b></td>
            </tr>
            <%=buildTableRows(nodes, 0, midNodeIndex)%>
            
          </table>
          <% } /*end if*/ %>
        </td>
        
        <td>
          &nbsp;&nbsp;
        </td>
        
      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
        <td width="49%" align="left" valign="top">
          <table border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="5%" align="center"><b>Node ID</b></td>
              <td width="10%" align="center"><b>Node Label</b></td>
            </tr>
            
            <%=buildTableRows(nodes, midNodeIndex, nodes.size())%>
               
          </table>
        </td>
        <% } /*end if */ %>
   </tr>
      
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>
</body>
</html>
<%!
      public String buildTableRows(List nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                SnmpManagedNode curNode = (SnmpManagedNode)nodes.get(i);
                String nodelabel = NetworkElementFactory.getNodeLabel(curNode.getNodeID());
		int nodeid = curNode.getNodeID();
                 
          row.append("<tr>\n");
          row.append("<td width=\"5%\" align=\"center\">");
	  row.append(nodeid);
          row.append("</td>\n");
          row.append("<td width=\"10%\" align=\"left\">");
          row.append("<a href=\"admin/snmpselect.jsp?node=");
	  row.append(nodeid);
          row.append("\">");
	  row.append(nodelabel);
          row.append("</a>");
          row.append("</td>\n");
          row.append("</tr>\n");
          } /* end i for */
          
          return row.toString();
      }
      
%>

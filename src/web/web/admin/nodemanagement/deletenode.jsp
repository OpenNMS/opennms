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
// 2004 Jan 15: Added a "delete node" page.
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

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,java.util.*,org.opennms.web.element.NetworkElementFactory" %>

<%
    int nodeId = -1;
    String nodeIdString = request.getParameter("node");

    if (nodeIdString == null) {
        throw new org.opennms.web.MissingParameterException("node");
    }

    try {
        nodeId = Integer.parseInt(nodeIdString);
    } catch (NumberFormatException numE)  {
        throw new ServletException(numE);
    }
    
    if (nodeId < 0 )
        throw new ServletException("Invalid node ID.");
        
    //get the database node info
    Node node_db = NetworkElementFactory.getNode(nodeId);
    if (node_db == null) {
        throw new ServletException("No such node in database.");
    }
%>

<html>
<head>
  <title>Node Management | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<script language="Javascript" type="text/javascript" >

  function applyChanges()
  {
        var hasCheckedItems = false;
        for (var i = 0; i < document.deleteNode.elements.length; i++)
        {
                if (document.deleteNode.elements[i].type == "checkbox")
                {
                        if (document.deleteNode.elements[i].checked)
                        {
                                hasCheckedItems = true;
                                break;
                        }
                }
        }
                
        if (hasCheckedItems)
        {
                if (confirm("Are you sure you want to proceed? This action will permanently delete the checked items and cannot be undone."))
                {
                        document.deleteNode.submit();
                }
        }
        else
        {
                alert("No node or data item is selected!");
        }
  }
  
  function cancel()
  {
      document.deleteNode.action="admin/nodemanagement/index.jsp?node=<%=nodeId%>";
      document.deleteNode.submit();
  }
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Node Management"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Delete Node" />
  <jsp:param name="location" value="Node Management" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp;&nbsp; </td>

    <td width="100%" valign="top">
       <h2>Node: <%=node_db.getLabel()%></h2>
       <hr>
    </td>
  </tr>
</table>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td> &nbsp;&nbsp; </td>  
    
    <td><table width="100%" cellspacing="0" cellpadding="0" border="0">
         <tr>
          <td> &nbsp;&nbsp; </td>  
          <td colspan="3"> 
	  <p>To permanently delete a node (and all associated interfaces, services, outages, events and notifications), 
             check the "Node" box and  select "Delete". 
          </p>
	  <p>Checking the "Data" box will delete the SNMP performance and response time directories from the system as well.
             Note that it is possible for the directory to be deleted <i>before</i> the fact that the node has been removed 
             has fully propagated through the system. Thus the system may recreate the directory for a single update after
             this action. In that case, the directory will need to be removed manually.
	  </p>
          <p><b>Note:</b> If the IP address of any of the node's interfaces is still configured for discovery, the node  
             will be discovered again. To prevent this, either remove the IP address from the discovery range or unmanage  
             the device instead of deleting it.
          </p>
          </td>
          <td> &nbsp;&nbsp; </td>  
        </tr>
      </table>
    </td>
  </tr>
</table>
<hr> 
  
<form method="POST" name="deleteNode" action="admin/deleteSelNodes">
<table width="70%" cellspacing="0" cellpadding="0" border="0">
  <tr>
	<td>&nbsp;</td>
        <td align="left" valign="center">
          <input type="checkbox" name="nodeCheck" value='<%= nodeId %>'>Node
        </td>
	<td>&nbsp;</td>
        <td align="left" valign="center">
          <input type="checkbox" name="nodeData" value='<%= nodeId %>'>Data
        </td>
        <td align="left" valign="center">
          Node: <%=node_db.getLabel()%>
        </td>
        <br>
   </tr>
   <tr><td>&nbsp;</td></tr>
   <tr><td>&nbsp;</td></tr>
   <tr>
	<td>&nbsp;</td>
        <td align="left" valign="center" colspan="5">
          <input type="button" value="Delete" onClick="applyChanges()">
          <input type="button" value="Cancel" onClick="cancel()">
        <td>
        <br>
   </tr>
</table>
</form>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="Node Management" />
</jsp:include>
</body>
</html>


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
    String nodeIdString = request.getParameter( "node" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node" );
    }

    int nodeId = Integer.parseInt( nodeIdString );

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
      if (confirm("Are you sure you want to proceed? This action can be undone by returning to this page."))
      {
          document.chooseSnmpNodes.submit();
      }
  }
  
  function cancel()
  {
      document.chooseSnmpNodes.action="admin/index.jsp";
      document.chooseSnmpNodes.submit();
  }
  
  function checkAll()
  {
      for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++)
      {  
          if (document.chooseSnmpNodes.elements[c].type == "checkbox")
          {
              document.chooseSnmpNodes.elements[c].checked = true;
          }
      }
  }
  
  function uncheckAll()
  {
      for (var c = 0; c < document.chooseSnmpNodes.elements.length; c++)
      {  
          if (document.chooseSnmpNodes.elements[c].type == "checkbox")
          {
              
              document.chooseSnmpNodes.elements[c].checked = false;
          }
      }
  }
  
</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("admin/index.jsp") + "'>Admin</a>"; %>
<% String breadcrumb2 = "Select SNMP Interfaces"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Select SNMP Interfaces" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<FORM METHOD="POST" name="chooseSnmpNodes" action="admin/changeCollectStatus">

<input type="hidden" name="node" value="<%=nodeId%>" />


<table width="100%" cellspacing="0" cellpadding="0" border="0">
  
  <tr>
    <td> &nbsp; </td>  
    
    <td>
    	<h3>Choose SNMP Interfaces for Data Collection</h3>

    	<table width="100%" cellspacing="0" cellpadding="0" border="0">
      	<tr>
        	<td colspan="3"> 
		<P>Listed below are all the interfaces discovered for the selected node. If
		snmpStorageFlag is set to "select" for a collection scheme that includes
		the interface marked as "Primary", only the interfaces checked below will have
		their collected SNMP data stored. This has no effect if snmpStorageFlag is
		set to "primary" or "all".
		</P>
		<P>
		In order to change what interfaces are scheduled for collection, simple check
		or uncheck the box beside the interface(s) you wish to change, and then
		select "Update Collection".
		</P>
        	<P><b>Note:</b> Interfaces marked as Primary or Secondary will always be selected
		for data collection. To remove them, please edit the IP address range in the
		collectd configuration.
        	</P>
        	</td>
      	</tr>
	
      	<TR>
      	<td>&nbsp;</td>
      	</tr>

   	<%=listNodeName(nodes, nodeId, nodes.size())%>
      
   	<tr>
        	<td align="left" valign="top">
       			<% if (interfaces.size() > 0) { %>
          		<table border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            		<tr bgcolor="#999999">
              			<td width="5%" align="center"><b>ifIndex</b></td>
              			<td width="10%" align="center"><b>IP Address</b></td>
              			<td width="10%" align="center"><b>IP Hostname</b></td>
              			<td width="5%" align="center"><b>ifType</b></td>
              			<td width="10%" align="center"><b>ifDescription</b></td>
              			<td width="10%" align="center"><b>ifName</b></td>
              			<td width="10%" align="center"><b>SNMP Status</b></td>
              			<td width="5%" align="center"><b>Collect?</b></td>
            		</tr>
            		<%=buildTableRows(interfaces, nodeId, interfaces.size())%>
            
          		</table>
          		<% } /*end if*/ %>
       		</td>
        
       		<td>
       		&nbsp;&nbsp;
       		</td>
        
   	</tr>
      
   	<tr>
        	<td align="left" valign="center" colspan="5">
          	&nbsp;<br>
          	<input type="button" value="Update Collection" onClick="applyChanges()">
          	<input type="button" value="Cancel" onClick="cancel()"> 
          	<input type="button" value="Select All" onClick="checkAll()">
          	<input type="button" value="Unselect All" onClick="uncheckAll()">
          	<input type="reset">
        	</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
    	</tr>
	</table>
</td>
</tr>
</table>
</FORM>

<br>

<jsp:include page="/includes/footer.jsp" flush="true" >
  <jsp:param name="location" value="admin" />
</jsp:include>
</body>
</html>

<%!
      public String listNodeName(List nodes, int intnodeid, int nodesize)
      	throws java.sql.SQLException
      {
         StringBuffer nodename = new StringBuffer();
                
         for (int i = 0; i < nodesize; i++)
	 {
         	SnmpManagedNode curNode = (SnmpManagedNode)nodes.get(i);
		int curnodeid = curNode.getNodeID();
		if (curnodeid == intnodeid)
		{
	 		String curNodeLabel = curNode.getNodeLabel(); 
         		nodename.append("<tr><td>");
         		nodename.append("<B>Node ID</B>: ");
         		nodename.append(intnodeid);
         		nodename.append("</td></tr>\n");
         		nodename.append("<tr><td>");
         		nodename.append("<B>Node Label</B>: ");
         		nodename.append(curNodeLabel);
      	 		nodename.append("</td></tr>\n");
         		nodename.append("<tr><td>&nbsp;</td></tr>\n");
		}
	}
          
         return nodename.toString();
      }
      
%>

<%!
      public String buildTableRows(List interfaces, int intnodeid, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = 0; i < stop; i++)
          {
                
                SnmpManagedInterface curInterface = (SnmpManagedInterface)interfaces.get(i);
		int curnodeid = curInterface.getNodeid();
		String collstatus = null;
		String chkstatus = null;
                if (curnodeid == intnodeid)
		{
		String statustest = curInterface.getStatus();
		String key = intnodeid + "+" + curInterface.getIfIndex();
		if (statustest.equals("P"))
		{
			collstatus = "Primary";
			chkstatus = "checked";
		}
		else if (statustest.equals("S"))
		{
			collstatus = "Secondary";
			chkstatus = "checked";
		}
		else if (statustest.equals("C"))
		{
			collstatus = "Collected";
			chkstatus = "checked";
		}
		else
		{
			collstatus = "Not Collected";
			chkstatus = "unchecked";
		}
          	row.append("<tr>\n");
          	row.append("<td width=\"5%\" align=\"center\">");
	  	row.append(curInterface.getIfIndex());
          	row.append("</td>\n");
          	row.append("<td width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getAddress());
          	row.append("</td>\n");
          	row.append("<td width=\"20%\" align=\"left\">");
	  	row.append(curInterface.getIpHostname());
          	row.append("</td>\n");
          	row.append("<td width=\"5%\" align=\"center\">");
	  	row.append(curInterface.getIfType());
          	row.append("</td>\n");
          	row.append("<td width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getIfDescr());
          	row.append("</td>\n");
          	row.append("<td width=\"10%\" align=\"center\">");
	  	row.append(curInterface.getIfName());
          	row.append("</td>\n");
          	row.append("<td width=\"10%\" align=\"center\">");
	  	row.append(collstatus);
          	row.append("</td>\n");
          	row.append("<td width=\"5%\" align=\"center\">");
          	row.append("<input type=\"checkbox\" name=\"collTypeCheck\" value=\"").append(key).append("\" ").append(chkstatus).append(" >");
          	row.append("</td>\n");
      	   	row.append("</tr>\n");
		}
          } /* end i for */
          
          return row.toString();
      }
      
%>

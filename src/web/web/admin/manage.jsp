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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 10: Removed the "http://" from UEIs and references to bluebird.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="java.io.File,java.util.*,org.opennms.web.element.NetworkElementFactory,org.opennms.web.admin.nodeManagement.*" %>

<%!
    int interfaceIndex;
    int serviceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List nodes = null;
    Integer lineItems= new Integer(0);
    
    //EventConfFactory eventFactory = EventConfFactory.getInstance();
    
    interfaceIndex = 0;
    serviceIndex = 0;
    
    if (userSession != null)
    {
		  	nodes = (List)userSession.getAttribute("listAll.manage.jsp");
        lineItems = (Integer)userSession.getAttribute("lineItems.manage.jsp");
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
      if (confirm("Are you sure you want to proceed? It may take several minutes to update the database based on the changes made."))
      {
          document.manageAll.submit();
      }
  }
  
  function cancel()
  {
      document.manageAll.action="admin/index.jsp";
      document.manageAll.submit();
  }
  
  function checkAll()
  {
      for (var c = 0; c < document.manageAll.elements.length; c++)
      {  
          if (document.manageAll.elements[c].type == "checkbox")
          {
              document.manageAll.elements[c].checked = true;
          }
      }
  }
  
  function uncheckAll()
  {
      for (var c = 0; c < document.manageAll.elements.length; c++)
      {  
          if (document.manageAll.elements[c].type == "checkbox")
          {
              
              document.manageAll.elements[c].checked = false;
          }
      }
  }
  
  function updateServices(interfaceIndex, serviceIndexes)
  {
      for (var i = 0; i < serviceIndexes.length; i++)
      {
          document.manageAll.serviceCheck[serviceIndexes[i]].checked = document.manageAll.interfaceCheck[interfaceIndex].checked;
      }
  }
  
  function verifyManagedInterface(interfaceIndex, serviceIndex)
  {
      //if the service is currently unmanged then the user is trying to manage it,
      //but we need to make sure its interface is managed before we let the service be managed
      if (!document.manageAll.interfaceCheck[interfaceIndex].checked)
      {
          if (document.manageAll.serviceCheck[serviceIndex].checked)
          {
              alert("The interface that this service is on is not managed. Please manage the interface to manage the service.");
              document.manageAll.serviceCheck[serviceIndex].checked = false;
              return false;
          }
      }
      
      return true;
  }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Manage/Unmanage Interfaces"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage/Unmanage Interfaces and Services" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<FORM METHOD="POST" name="manageAll" action="admin/manageNodes">

<%
  int halfway = 0;
  int midCount = 0;
  int midNodeIndex = 0;
  
  if (lineItems.intValue() > 0)
  {
    halfway = lineItems.intValue()/2;
    for (int nodeCount = 0; nodeCount < nodes.size(); nodeCount++)
    {
        if (midCount < halfway)
        {
            midCount++; //one row for each interface
            ManagedInterface curInterface = (ManagedInterface)nodes.get(nodeCount);
            midCount += curInterface.getServiceCount();
        }
        else 
        {
            midNodeIndex = nodeCount;
            break;
        }
    }
  }
%>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  
  <tr>
    <td> &nbsp; </td>  
    
    <td>
    <h3>Manage and Unmanage Interfaces and Services</h3>

    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <td colspan="3"> 
          <p>The two tables below represent each managed and unmanged node, interface, and service combination. The 'Status' column indicates if the interface or
          service is managed or not, with checked rows meaning the interface or service is managed, and unchecked meaning not managed. Each different interface
          has a dark grey row and no service column, and each service on that interface is listed below on light grey rows.</p>
          <p>Managing or Unmanaging an interface will automatically mark each service on that interface as managed or unmanaged accordingly. A service cannot be
          managed if its interface is not managed.</p>
        </td>
      </tr>
      
      
      <tr>
        <!--
        <td align="left" valign="center">
          <table>
            <tr>
              <td valign="top">Notify the Reporting group via email of the following events:</td>
              <td valign="top">
                <input type="checkbox" name="notifyNodeGainedInterface" <%--=(eventFactory.eventHasNotice("uei.opennms.org/nodes/nodeGainedInterface", "Email-Reporting") ? "checked" : "")--%>> Node Gained Interface <br>
                <input type="checkbox" name="notifyNodeGainedService" <%--=(eventFactory.eventHasNotice("uei.opennms.org/nodes/nodeGainedService", "Email-Reporting") ? "checked" : "")--%>> Node Gained Service
              </td>
            </tr>
          </table>
        </td>
        <td>&nbsp;</td>-->
        <td align="left" valign="center">
          <input type="button" value="Apply Changes" onClick="applyChanges()">
          <input type="button" value="Cancel" onClick="cancel()">
          <input type="button" value="Select All" onClick="checkAll()">
          <input type="button" value="Unselect All" onClick="uncheckAll()">
          <input type="reset"><br>&nbsp;
        </td>
      </tr>
      
      <% if (nodes.size() > 0) { %>
      <tr>
        <td align="left" valign="top">
          <table border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="5%"><b>Status</b></td>
              <td width="10%"><b>Node Label</b></td>
              <td width="5%"><b>Interface</b></td>
              <td width="5%"><b>Service</b></td>
            </tr>
            
            <%=buildManageTableRows(nodes, 0, midNodeIndex)%>
            
          </table>
          <% } /*end if*/ %>
        </td>
        
        <td>
          &nbsp;&nbsp;
        </td>
        
      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
        <td align="left" valign="top">
          <table border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="5%"><b>Status</b></td>
              <td width="10%"><b>Node Label</b></td>
              <td width="5%"><b>Interface</b></td>
              <td width="5%"><b>Service</b></td>
            </tr>
            
            <%=buildManageTableRows(nodes, midNodeIndex, nodes.size())%>
               
          </table>
        </td>
        <% } /*end if */ %>
      </tr>
      
      <tr>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td align="left" valign="center" colspan="3">
          &nbsp;<br>
          <input type="button" value="Apply Changes" onClick="applyChanges()">
          <input type="button" value="Cancel" onClick="cancel()"> |
          <input type="button" value="Select All" onClick="checkAll()">
          <input type="button" value="Unselect All" onClick="uncheckAll()">
          <input type="reset">
        </td>
      </tr>
    
    </table>
    </td>
    
    <td> &nbsp; </td>
  
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
      public String buildManageTableRows(List nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer rows = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                ManagedInterface curInterface = (ManagedInterface)nodes.get(i);
                String nodelabel = NetworkElementFactory.getNodeLabel(curInterface.getNodeid());
		String intKey = curInterface.getNodeid() + "-" + curInterface.getAddress();
                StringBuffer serviceArray = new StringBuffer("[");
                String prepend = "";
                for (int serviceCount = 0; serviceCount < curInterface.getServiceCount(); serviceCount++)
                {
                    serviceArray.append(prepend).append(serviceIndex+serviceCount);
                    prepend = ",";
                }
                serviceArray.append("]");
                
                rows.append(buildInterfaceRow(intKey, 
                                              interfaceIndex, 
                                              serviceArray.toString(), 
                                              (curInterface.getStatus().equals("managed") ? "checked" : ""),
                                              nodelabel,
                                              curInterface.getAddress()));
                    
                  
                List interfaceServices = curInterface.getServices();
                for (int k = 0; k < interfaceServices.size(); k++) 
                {
                     ManagedService curService = (ManagedService)interfaceServices.get(k);
                     String serviceKey = curInterface.getNodeid() + "-" + curInterface.getAddress() + "-" + curService.getId();
                     rows.append(buildServiceRow(serviceKey,
                                                 interfaceIndex,
                                                 serviceIndex,
                                                 (curService.getStatus().equals("managed") ? "checked" : ""),
                                                 nodelabel,
                                                 curInterface.getAddress(),
                                                 curService.getName()));
                     serviceIndex++;
                
                } /*end k for */
                
                interfaceIndex++;
                
          } /* end i for */
          
          return rows.toString();
      }
      
      public String buildInterfaceRow(String key, int interfaceIndex, String serviceArray, String status, String nodeLabel, String address)
      {
          StringBuffer row = new StringBuffer( "<tr bgcolor=\"#999999\">");
          
          row.append("<td width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"interfaceCheck\" value=\"").append(key).append("\" onClick=\"javascript:updateServices(" + interfaceIndex + ", " + serviceArray + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td width=\"10%\">");
          row.append(nodeLabel);
          row.append("</td>").append("\n");
          row.append("<td width=\"5%\">");
          row.append(address);
          row.append("</td>").append("\n");
          row.append("<td width=\"5%\">").append("&nbsp;").append("</td></tr>").append("\n");
          
          return row.toString();
      }
      
      public String buildServiceRow(String key, int interfaceIndex, int serviceIndex, String status, String nodeLabel, String address, String service)
      {
          StringBuffer row = new StringBuffer( "<tr bgcolor=\"#cccccc\">");
          
          row.append("<td width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"serviceCheck\" value=\"").append(key).append("\" onClick=\"javascript:verifyManagedInterface(" + interfaceIndex + ", " + serviceIndex + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td width=\"10%\">").append(nodeLabel).append("</td>").append("\n");
          row.append("<td width=\"5%\">").append(address).append("</td>").append("\n");
          row.append("<td width=\"5%\">").append(service).append("</td></tr>").append("\n");
          
          return row.toString();
      }
%>

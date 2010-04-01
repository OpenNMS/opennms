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

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Manage/Unmanage Interfaces and Services" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Manage/Unmanage Interfaces" />
</jsp:include>


<script type="text/javascript" >

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


<form method="post" name="manageAll" action="admin/manageNodes">

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

    <h3>Manage and Unmanage Interfaces and Services</h3>

          <p>The two tables below represent each managed and unmanged node, interface, and service combination. The 'Status' column indicates if the interface or
          service is managed or not, with checked rows meaning the interface or service is managed, and unchecked meaning not managed. Each different interface
          has a dark grey row and no service column, and each service on that interface is listed below on light grey rows.</p>
          <p>Managing or Unmanaging an interface will automatically mark each service on that interface as managed or unmanaged accordingly. A service cannot be
          managed if its interface is not managed.</p>


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

          <input type="button" value="Apply Changes" onClick="applyChanges()">
          <input type="button" value="Cancel" onClick="cancel()">
          <input type="button" value="Select All" onClick="checkAll()">
          <input type="button" value="Unselect All" onClick="uncheckAll()">
          <input type="reset"><br>&nbsp;

	<br/>
      
      <% if (nodes.size() > 0) { %>
	<div id="contentleft">
          <table class="standardfirst">
            <tr>
              <td class="standardheader" width="5%">Status</td>
              <td class="standardheader" width="10%">Node Label</td>
              <td class="standardheader" width="5%">Interface</td>
              <td class="standardheader" width="5%">Service</td>
            </tr>
            
            <%=buildManageTableRows(nodes, 0, midNodeIndex)%>
            
          </table>
	</div>
          <% } /*end if*/ %>
        
      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
	<div id="contentright">
          <table class="standardfirst">
            <tr>
              <td class="standardheader" width="5%">Status</td>
              <td class="standardheader" width="10%">Node Label</td>
              <td class="standardheader" width="5%">Interface</td>
              <td class="standardheader" width="5%">Service</td>
            </tr>
            
            <%=buildManageTableRows(nodes, midNodeIndex, nodes.size())%>
               
          </table>
	</div>
        <% } /*end if */ %>

	<div class="spacer"><!-- --></div>
	<br/>

          <input type="button" value="Apply Changes" onClick="applyChanges()">
          <input type="button" value="Cancel" onClick="cancel()"> |
          <input type="button" value="Select All" onClick="checkAll()">
          <input type="button" value="Unselect All" onClick="uncheckAll()">
          <input type="reset">
</form>


<jsp:include page="/includes/footer.jsp" flush="true"/>

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
          
          row.append("<td class=\"standardheaderplain\" width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"interfaceCheck\" value=\"").append(key).append("\" onClick=\"javascript:updateServices(" + interfaceIndex + ", " + serviceArray + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td class=\"standardheaderplain\" width=\"10%\">");
          row.append(nodeLabel);
          row.append("</td>").append("\n");
          row.append("<td class=\"standardheaderplain\" width=\"5%\">");
          row.append(address);
          row.append("</td>").append("\n");
          row.append("<td class=\"standardheaderplain\" width=\"5%\">").append("&nbsp;").append("</td></tr>").append("\n");
          
          return row.toString();
      }
      
      public String buildServiceRow(String key, int interfaceIndex, int serviceIndex, String status, String nodeLabel, String address, String service)
      {
          StringBuffer row = new StringBuffer( "<tr bgcolor=\"#cccccc\">");
          
          row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"serviceCheck\" value=\"").append(key).append("\" onClick=\"javascript:verifyManagedInterface(" + interfaceIndex + ", " + serviceIndex + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td class=\"standard\" width=\"10%\">").append(nodeLabel).append("</td>").append("\n");
          row.append("<td class=\"standard\" width=\"5%\">").append(address).append("</td>").append("\n");
          row.append("<td class=\"standard\" width=\"5%\">").append(service).append("</td></tr>").append("\n");
          
          return row.toString();
      }
%>

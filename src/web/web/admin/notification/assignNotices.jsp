<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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

<%@page language="java" contentType="text/html" session="true" import="java.sql.*,java.util.*,org.opennms.netmgt.config.*,org.opennms.web.element.*" %>

<%!
    public void init() throws ServletException {
        try {
            NotificationFactory.init();
        }
        catch( Exception e ) { throw new ServletException(e); }
    }
    
    String notificationOptions = null;
    private int controlCount = 5; /*there are five buttons before the check boxes begin*/
    List allNodes = null;
    
    int serviceCountEachNode[] = null;
    String serviceRowsEachNode[] = null;
    int totalRows = 0;
    
    Map currentServiceStatii = null;
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
      document.manageAll.submit();
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
  
  function turnOn(serviceIndexes)
  {
      for (var i = 0; i < serviceIndexes.length; i++)
      {
          document.manageAll.elements[serviceIndexes[i]].checked = true;
      }
  }
  
  function turnOff(serviceIndexes)
  {
      for (var i = 0; i < serviceIndexes.length; i++)
      {
          document.manageAll.elements[serviceIndexes[i]].checked = false;
      }
  }

</script>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='admin/index.jsp'>Admin</a>"; %>
<% String breadcrumb2 = "Assign Notices"; %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Assign Nodes, Interfaces and Services to Notification Groups" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<!-- Body -->
<br>

<FORM METHOD="POST" name="manageAll" action="admin/updateServiceNotify">

<%
    //A mapping of all of the current service notify flags, will be compared to the form 
    //when the update servlet is called
    currentServiceStatii = new HashMap();
    session.setAttribute("service.notify.map", currentServiceStatii);
    
    controlCount = 5; /*there are five buttons before the check boxes begin*/
    totalRows = 0;
    
    allNodes = NotificationFactory.getInstance().getActiveNodes();
    serviceCountEachNode = new int[allNodes.size()];
    serviceRowsEachNode = new String[allNodes.size()];
    
    notificationOptions = buildNoticeOptions();
    
    for (int i = 0; i < allNodes.size(); i++)
    {
        int nodeID = ((Integer)allNodes.get(i)).intValue();
        
        Interface interfaces[] = NetworkElementFactory.getInterfacesOnNode(nodeID);
        for (int intCount = 0; intCount < interfaces.length; intCount++)
        {
	      		if (!interfaces[intCount].getIpAddress().equals("0.0.0.0"))
            {
	        				Service services[] = NetworkElementFactory.getServicesOnInterface(nodeID, interfaces[intCount].getIpAddress());
                  
                  serviceCountEachNode[i] = services.length + 1;
                  totalRows += serviceCountEachNode[i];
                  serviceRowsEachNode[i] = interfaceServiceList(nodeID, interfaces[intCount].getIpAddress(), services);
            }
        }
    }
%>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  
  <tr>
    <td> &nbsp; </td>  
    
    <td>
    <input type="button" value="Finish" onClick="applyChanges()">
    <input type="button" value="Cancel" onClick="cancel()"> | 
    <input type="button" value="Select All" onClick="checkAll()">
    <input type="button" value="Unselect All" onClick="uncheckAll()">
    <input type="reset"><br>
    
    <table width="100%" cellspacing="0" cellpadding="0" border="0">
      
      <% if (allNodes.size() > 0) { %>
      <tr>
        <td align="left" valign="top">
          <table width="50%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="30%"><b>Node Label</b></td>
              <td width="20%"><b>Interface</b></td>
              <td width="10%"><b>Service</b></td>
              <td width="40%"><b>Notify</b></td>
            </tr>
            
            <% int count = 0;
               int nodeCount = 0;
               for (nodeCount = 0; nodeCount < serviceRowsEachNode.length && count <= totalRows/2; nodeCount++)
               {
                  count += serviceCountEachNode[nodeCount];
             %>
                  <%=serviceRowsEachNode[nodeCount]%>
            <% } %>
          </table>
        </td>
        
        <td>
          &nbsp;&nbsp;
        </td>
        
        <td align="left" valign="top">
          <table width="50%" border="1" cellspacing="0" cellpadding="2" bordercolor="black">
            <tr bgcolor="#999999">
              <td width="30%"><b>Node Label</b></td>
              <td width="20%"><b>Interface</b></td>
              <td width="10%"><b>Service</b></td>
              <td width="40%"><b>Notify</b></td>
            </tr>
            
            <% for (int remaining = nodeCount; remaining < serviceRowsEachNode.length; remaining++)
               {   
            %>
                  <%=serviceRowsEachNode[remaining]%>
            <% } %>
          </table>
        </td>
      </tr>
      
    <% } %>
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
    private String buildNoticeOptions()
    {
        StringBuffer buffer = new StringBuffer("<option value=\"\">No Notification</option>");
        
        List names = NotificationFactory.getInstance().getNotificationNames();
        for (int i = 0; i < names.size(); i++)
        {
            String curName = (String)names.get(i);
            buffer.append("<option value=\"" + curName + "\">" + curName + "</option>");
        }
        
        return buffer.toString();
    }
    
    private String interfaceServiceList(int nodeID, String ipAddress, Service services[])
        throws SQLException
    {
        String interfaceRow = null;
        String serviceRow = new String();
        String interfaceSelect = null;
        String nodeLabel = NetworkElementFactory.getNodeLabel(nodeID);
        
        interfaceRow =  "<tr bgcolor=\"#999999\"> " +
                        "  <td width=\"30%\">" + nodeLabel + "</td> " +
                        "  <td width=\"20%\">" + ipAddress + "</td> " +
                        "  <td width=\"10%\">&nbsp;</td> ";
        
        List serviceArray = new ArrayList();
        controlCount+=2;
        
        for (int i = 0; i < services.length; i++)
        {
              //add each service keyed by <nodeid>,<ipaddress>,<serviceid>. Pretend that it is not being notified (even
              //if it is currently marked "Y", the servlet will update any checked services with a "Y" in this map and then
              //update the database according to the value stored in the map
              currentServiceStatii.put(nodeID+","+ipAddress+","+services[i].getServiceId(), "N");
              
              serviceRow += "<tr bgcolor=\"#cccccc\"> " +
                            "  <td width=\"30%\">" + nodeLabel + "</td> " +
                            "  <td width=\"20%\">" + ipAddress + "</td> " +
                            "  <td width=\"10%\">" + services[i].getServiceName() + "</td> " +
                            "  <td width=\"40%\">" + buildServiceCheck(nodeID, ipAddress, services[i]) + "</td></tr>";
              serviceArray.add(new Integer(controlCount++));
        }
        
        interfaceSelect = "<td width=\"40%\"><input type=\"button\" value=\"On\" onClick=\"turnOn("+serviceArray.toString()+")\">&nbsp;" +
                          "<input type=\"button\" value=\"Off\" onClick=\"turnOff("+serviceArray.toString()+")\"></td>";
        
        return interfaceRow + interfaceSelect + serviceRow;
    }
    
    private String buildServiceCheck(int nodeID, String ipAddress, Service service)
    {
          StringBuffer buffer = new StringBuffer("<input type=\"checkbox\" name=\"serviceCheck\" value=\"");
          
          buffer.append(nodeID + "," + ipAddress + "," + service.getServiceId());
          buffer.append("\" name=\"serviceCheck\"");
          buffer.append( ("Y".equals(service.getNotify()) ? "checked" : "") + ">");
          
          return buffer.toString();
    }
%>

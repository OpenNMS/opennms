<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
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
    List<ManagedInterface> interfaces = null;
    Integer lineItems= new Integer(0);
    
    //EventConfFactory eventFactory = EventConfFactory.getInstance();
    
    interfaceIndex = 0;
    serviceIndex = 0;
    
    if (userSession == null) {
	throw new ServletException("User session is null");
    }

    interfaces = (List<ManagedInterface>) userSession.getAttribute("interfaces.nodemanagement");
    if (interfaces == null) {
	throw new ServletException("Session attribute "
				   + "interfaces.nodemanagement is null");
    } else if (interfaces.size() < 1) {
    	throw new NoManagedInterfacesException("element/nodeList.htm");
    }
    lineItems = (Integer) userSession.getAttribute("lineItems.nodemanagement");
    if (lineItems == null) {
	throw new ServletException("Session attribute "
				   + "lineItems.nodemanagement is null");
    }
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Manage/Unmanage Interfaces and Services" />
  <jsp:param name="headTitle" value="Node Management" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="Node Management" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Node Management" />
</jsp:include>

<script type="text/javascript" >

  function applyChanges()
  {
      return confirm("Are you sure you want to proceed? It may take several minutes to update the database based on the changes made.");
  }
  
  function cancel()
  {
      document.manageAll.action="admin/nodemanagement/index.jsp";
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

<%
        int halfway = 0;
        int midCount = 0;
        int midInterfaceIndex = 0;
        String nodeLabel = null;
  
        if (lineItems.intValue() > 0)
        {
                ManagedInterface firstInterface = interfaces.get(0);
                nodeLabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(firstInterface.getNodeid());
    
                if ( interfaces.size() == 1)
                { 
                        midInterfaceIndex = 1;
                }
                else
                {
                        halfway = lineItems.intValue()/2;
                        for (int interfaceCount = 0; (interfaceCount < interfaces.size()) && (midCount < halfway); interfaceCount++)
                        {
                                if (midCount < halfway)
                                {
                                        midCount++; //one row for each interface
                                        ManagedInterface curInterface = (ManagedInterface)interfaces.get(interfaceCount);
                                        midCount += curInterface.getServiceCount();
                                }
                                else 
                                {
                                        midInterfaceIndex = interfaceCount;
                                        break;
                                }
                        }
                }

                if (midInterfaceIndex < 1)
                        midInterfaceIndex = interfaces.size();
        }
%>

<h3>Node: <%=nodeLabel%></h3>

<hr/>
    
<form method="post" name="manageAll" action="admin/manageNode" onsubmit="return applyChanges();">

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Manage and Unmanage Interfaces and Services</h3>
  </div>
  <div class="panel-body">
    <!--
    <p>
      The two tables below represent each managed and unmanged interface,
      and service combination.  The 'Managed' column indicates if the
      interface or service is managed or not, with checked rows meaning
      the interface or service is managed, and unchecked meaning not managed.
      Each different interface has a dark grey row and no service column,
      and each service on that interface is listed below on light grey rows.
    </p>

    <p>
      Managing or Unmanaging an interface will automatically mark each
      service on that interface as managed or unmanaged accordingly.  A
      service cannot be managed if its interface is not managed.
    </p>
    -->

    <p>
      The table below represent each managed and unmanged interface,
      and service combination.  The 'Managed' column indicates if the
      service is managed or not on the interface, with checked rows meaning
      the interface/service is managed, and unchecked meaning not managed.
    </p>

    <%
      ManagedInterface firstInterface = (ManagedInterface) interfaces.get(0);
      int nodeId = firstInterface.getNodeid();
    %>

    <input type="hidden" name="node" value="<%= nodeId %>"/>

    <div class="form-group">
      <input type="submit" class="btn btn-default" value="Apply Changes"/>
      <input type="button" class="btn btn-default" value="Cancel" onclick="cancel()"/>
      <input type="button" class="btn btn-default" value="Select All" onclick="checkAll()"/>
      <input type="button" class="btn btn-default" value="Unselect All" onclick="uncheckAll()"/>
      <input type="reset" class="btn btn-default" />
    </div>

    <div class="row">
    <% if (interfaces.size() > 0) { %>
      <div class="col-md-6">
        <table class="table table-condensed">
          <tr>
            <th class="text-center" width="5%">Managed</th>
            <th class="text-center" width="10%">Interface</th>
            <th class="text-center" width="10%">Service</th>
          </tr>

          <%=buildManageTableRows(interfaces, 0, midInterfaceIndex)%>
        </table>
      </div>
    <% } /*end if*/ %>

    <%-- See if there is a second column to draw --%>
    <% if (midInterfaceIndex < interfaces.size()) { %>
      <div class="col-md-6">
        <table class="table table-condensed">
          <tr>
            <th class="text-center" width="5%">Managed</th>
            <th class="text-center" width="10%">Interface</th>
            <th class="text-center" width="10%">Service</th>
          </tr>

          <%=buildManageTableRows(interfaces, midInterfaceIndex, interfaces.size())%>
        </table>
      </div>
    <% } /*end if */ %>
    </div>

    <div class="form-group">
      <input type="submit" class="btn btn-default" value="Apply Changes"/>
      <input type="button" class="btn btn-default" value="Cancel" onclick="cancel()"/>
      <input type="button" class="btn btn-default" value="Select All" onclick="checkAll()"/>
      <input type="button" class="btn btn-default" value="Unselect All" onclick="uncheckAll()"/>
      <input type="reset" class="btn btn-default" />
    </div>
  </div> <!-- panel-body -->
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

<%!
      public String buildManageTableRows(List<ManagedInterface> interfaces, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer rows = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                ManagedInterface curInterface = interfaces.get(i);
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
                                              curInterface.getAddress()));
                  
                List<ManagedService> interfaceServices = curInterface.getServices();
                for (int k = 0; k < interfaceServices.size(); k++) 
                {
                     ManagedService curService = interfaceServices.get(k);
                     String serviceKey = curInterface.getNodeid() + "-" + curInterface.getAddress() + "-" + curService.getId();
                     rows.append(buildServiceRow(serviceKey,
                                                 interfaceIndex,
                                                 serviceIndex,
                                                 (curService.getStatus().equals("managed") ? "checked" : ""),
                                                 curInterface.getAddress(),
                                                 curService.getName()));
                     serviceIndex++;
                
                } /*end k for */
                
                interfaceIndex++;
                
          } /* end i for */
          
          return rows.toString();
      }
      
      public String buildInterfaceRow(String key, int interfaceIndex, String serviceArray, String status, String address)
      {
          StringBuffer row = new StringBuffer( "<tr>");
          /*
          row.append("<td class=\"standardheaderplain\" width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"interfaceCheck\" value=\"").append(key).append("\" onclick=\"javascript:updateServices(" + interfaceIndex + ", " + serviceArray + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("</td>").append("\n");
          row.append("<td class=\"standardheaderplain\" width=\"10%\" align=\"center\">");
          row.append(address);
          row.append("</td>").append("\n");
          row.append("<td class=\"standardheaderplain\" width=\"10%\" align=\"center\">").append("&nbsp;").append("</td></tr>").append("\n");
          */ 
          row.append("<input type=\"hidden\" name=\"interfaceCheck\" value=\"").append(key).append("\" onclick=\"javascript:updateServices(" + interfaceIndex + ", " + serviceArray + ")\" ").append(status).append(" >");
          return row.toString();
      }
      
      public String buildServiceRow(String key, int interfaceIndex, int serviceIndex, String status, String address, String service)
      {
          StringBuffer row = new StringBuffer( "<tr>");
          
          row.append("<td class=\"text-center\" width=\"5%\">");
          row.append("<input type=\"checkbox\" name=\"serviceCheck\" value=\"").append(key).append("\" onclick=\"javascript:verifyManagedInterface(" + interfaceIndex + ", " + serviceIndex + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td class=\"text-center\" width=\"10%\">").append(address).append("</td>").append("\n");
          row.append("<td class=\"text-center\" width=\"10%\">").append(service).append("</td></tr>").append("\n");
          
          return row.toString();
      }
%>

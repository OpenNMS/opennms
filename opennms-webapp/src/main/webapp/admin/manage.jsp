<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.admin.nodeManagement.*
	"
%>

<%!
    int interfaceIndex;
    int serviceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List<ManagedInterface> nodes = null;
    Integer lineItems= new Integer(0);
    
    //EventConfFactory eventFactory = EventConfFactory.getInstance();
    
    interfaceIndex = 0;
    serviceIndex = 0;
    
    if (userSession != null)
    {
		  	nodes = (List<ManagedInterface>)userSession.getAttribute("listAll.manage.jsp");
        lineItems = (Integer)userSession.getAttribute("lineItems.manage.jsp");
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Manage/Unmanage Interfaces")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />


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
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
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

<div class="card">
  <div class="card-header">
    <span>Manage and Unmanage Interfaces and Services</span>
  </div>
  <div class="card-body">
          <p>The two tables below represent each managed and unmanged node, interface, and service combination. The 'Status' column indicates if the interface or
          service is managed or not, with checked rows meaning the interface or service is managed, and unchecked meaning not managed. Each different interface
          has a dark grey row and no service column, and each service on that interface is listed below on light grey rows.</p>
          <p>Managing or Unmanaging an interface will automatically mark each service on that interface as managed or unmanaged accordingly. A service cannot be
          managed if its interface is not managed.</p>

    <button type="button" class="btn btn-secondary" onClick="applyChanges()">Apply Changes</button>
    <button type="button" class="btn btn-secondary" onClick="cancel()">Cancel</button>
    <button type="button" class="btn btn-secondary" onClick="checkAll()">Select All</button>
    <button type="button" class="btn btn-secondary" onClick="uncheckAll()"> Unselect All</button>
    <button type="reset" class="btn btn-secondary">Reset</button>

    <div class="row top-buffer">
      <% if (nodes.size() > 0) { %>
	<div class="col-md-6">
          <table class="table table-sm table-striped table-hover">
            <tr>
              <th width="5%">Status</th>
              <th width="10%">Node Label</th>
              <th width="5%">Interface</th>
              <th width="5%">Service</th>
            </tr>
            
            <%=buildManageTableRows(nodes, 0, midNodeIndex)%>
            
          </table>
	</div> <!-- column -->
          <% } /*end if*/ %>
        
      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
	<div class="col-md-6">
          <table class="table table-sm table-striped table-hover">
            <tr>
              <th width="5%">Status</th>
              <th width="10%">Node Label</th>
              <th width="5%">Interface</th>
              <th width="5%">Service</th>
            </tr>
            
            <%=buildManageTableRows(nodes, midNodeIndex, nodes.size())%>
               
          </table>
	</div> <!-- column -->
        <% } /*end if */ %>
    </div> <!-- row -->

    <div class="top-buffer"></div>

    <button type="button" class="btn btn-secondary" onClick="applyChanges()">Apply Changes</button>
    <button type="button" class="btn btn-secondary" onClick="cancel()">Cancel</button>
    <button type="button" class="btn btn-secondary" onClick="checkAll()">Select All</button>
    <button type="button" class="btn btn-secondary" onClick="uncheckAll()"> Unselect All</button>
    <button type="reset" class="btn btn-secondary">Reset</button>
  </div> <!-- card-body -->
</div> <!-- panel -->

</form>


<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

<%!
      public String buildManageTableRows(List<ManagedInterface> nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer rows = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                ManagedInterface curInterface = (ManagedInterface)nodes.get(i);
                String nodelabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(curInterface.getNodeid());
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
                    
                  
                List<ManagedService> interfaceServices = curInterface.getServices();
                for (int k = 0; k < interfaceServices.size(); k++) 
                {
                     ManagedService curService = interfaceServices.get(k);
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
          StringBuffer row = new StringBuffer( "<tr>");
          
          row.append("<td class=\"text-center\">");
          row.append("<input type=\"checkbox\" name=\"interfaceCheck\" value=\"").append(key).append("\" onClick=\"javascript:updateServices(" + interfaceIndex + ", " + serviceArray + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td>");
          row.append(WebSecurityUtils.sanitizeString(nodeLabel));
          row.append("</td>").append("\n");
          row.append("<td>");
          row.append(address);
          row.append("</td>").append("\n");
          row.append("<td>").append("&nbsp;").append("</td></tr>").append("\n");
          
          return row.toString();
      }
      
      public String buildServiceRow(String key, int interfaceIndex, int serviceIndex, String status, String nodeLabel, String address, String service)
      {
          StringBuffer row = new StringBuffer( "<tr>");
          
          row.append("<td class=\"text-center\">");
          row.append("<input type=\"checkbox\" name=\"serviceCheck\" value=\"").append(key).append("\" onClick=\"javascript:verifyManagedInterface(" + interfaceIndex + ", " + serviceIndex + ")\" ").append(status).append(" >");
          row.append("</td>").append("\n");
          row.append("<td>").append(WebSecurityUtils.sanitizeString(nodeLabel)).append("</td>").append("\n");
          row.append("<td>").append(address).append("</td>").append("\n");
          row.append("<td>").append(WebSecurityUtils.sanitizeString(service)).append("</td></tr>").append("\n");
          
          return row.toString();
      }
%>

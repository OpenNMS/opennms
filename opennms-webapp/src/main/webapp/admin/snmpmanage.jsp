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
		org.opennms.web.admin.nodeManagement.*
	"
%>

<%!
    int interfaceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List<SnmpManagedNode> nodes = null;
    Integer lineItems= new Integer(0);
    
    interfaceIndex = 0;
    
    if (userSession == null) {
	throw new ServletException("session is null");
    }

    nodes = (List<SnmpManagedNode>)userSession.getAttribute("listAllnodes.snmpmanage.jsp");
    lineItems = (Integer)userSession.getAttribute("lineNodeItems.snmpmanage.jsp");

    if (nodes == null) {
	throw new ServletException("session attribute listAllnodes.snmpmanage.jsp is null");
    }
    if (lineItems == null) {
	throw new ServletException("session attribute lineNodeItems.snmpmanage.jsp is null");
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Manage SNMP by Interface")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Manage SNMP Data Collection per Interface</span>
  </div>
  <div class="card-body">
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
              <table class="table table-sm table-responsive">
                  <thead>
                <tr class="text-center">
                  <th>Node ID</th>
                  <th>Node Label</th>
                </tr>
                  </thead>
                  <tbody>
                <%=buildTableRows(nodes, 0, nodes.size())%>
                  </tbody>
              </table>
      <% }else{ %>
      <div class="alert alert-primary" role="alert">
          There are no SNMP Nodes
      </div>
      <% } /*end if-else*/ %>
  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

<%!
      public String buildTableRows(List<SnmpManagedNode> nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                SnmpManagedNode curNode = nodes.get(i);
                String nodelabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(curNode.getNodeID());
		int nodeid = curNode.getNodeID();
                 
          row.append("<tr>\n");
          row.append("<td class=\"text-center\">");
	  row.append(nodeid);
          row.append("</td>\n");
          row.append("<td>");
          row.append("<a href=\"admin/snmpInterfaces.jsp?node=");
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

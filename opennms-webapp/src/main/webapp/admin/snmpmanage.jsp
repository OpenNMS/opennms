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

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
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

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Manage SNMP Data Collection per Interface</h3>
  </div>
  <div class="panel-body">
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

    <div class="row">
       <% if (nodes.size() > 0) { %>
       <div class="col-md-6">
              <table class="table table-condensed table-bordered">
                <tr class="text-center">
                  <th>Node ID</th>
                  <th>Node Label</th>
                </tr>
                <%=buildTableRows(nodes, 0, midNodeIndex)%>
              </table>
       </div>
              <% } /*end if*/ %>

          <!--see if there is a second column to draw-->
          <% if (midNodeIndex < nodes.size()) { %>
        <div class="col-md-6">
              <table class="table table-condensed table-bordered">
                <tr class="text-center">
                  <th>Node ID</th>
                  <th>Node Label</th>
                </tr>
                <%=buildTableRows(nodes, midNodeIndex, nodes.size())%>
              </table>
        </div>
            <% } /*end if */ %>
    </div> <!-- row -->
  </div> <!-- panel-body -->
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

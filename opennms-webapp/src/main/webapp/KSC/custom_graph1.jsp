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

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="java.util.*,
		java.io.*,
		org.opennms.web.ServletInitializer,
		org.opennms.web.Util,
		org.opennms.web.performance.*,
		org.opennms.netmgt.config.kscReports.*,
		org.opennms.netmgt.config.KSC_PerformanceReportFactory
	"
%>


<%!
    public PerformanceModel model = null;
   
    public void init() throws ServletException {
        try {
            this.model = new PerformanceModel(ServletInitializer.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the PerformanceModel", t);
        }
    }
%>

<%
    PerformanceModel.QueryableNode[] nodes = this.model.getQueryableNodes();
    String[] domains = this.model.getQueryableDomains();
    String nodeId = request.getParameter("node");
    String dom = request.getParameter("domain");
    int node_intval;
    if (nodeId == null || "null".equals(nodeId)) {
        node_intval = -1;
    } else {
        node_intval = Integer.valueOf(nodeId).intValue();
    } 

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Key SNMP Customized Performance Reports" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="location" value="KSC Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='KSC/index.jsp'>KSC Reports</a>" />
  <jsp:param name="breadcrumb" value="Custom Graph" />
</jsp:include>

<%-- A script for validating Node ID Selection Form before submittal --%>
<script language="Javascript" type="text/javascript" >
  function validateNode()
  {
      var isChecked = false
      for( i = 0; i < document.choose_node.node.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node.node[i].selected)
         {
            isChecked=true;
         }
      }

      if (!isChecked)
      {
          alert("Please check the node that you would like to report on.");
      }
      return isChecked;
  }

  function validateDomain()
  {
      var isChecked = false
      for( i = 0; i < document.choose_domain.domain.length; i++ )
      {
          //make sure something is checked before proceeding
          if (document.choose_domain.domain[i].selected)
          {
              isChecked=true;
          }
      }

      if (!isChecked)
      {
          alert("Please check the domain that you would like to report on.");
      }
      return isChecked;
  }
  
  function submitNodeForm()
  {
      if (validateNode())
      {
          document.choose_node.submit();
      }
  }

  function submitDomainForm()
  {
      if (validateDomain())
      {
          document.choose_domain.submit();
      }
  }
</script>

<h3 align="center">Customized Report - Graph Definition</h3>

<table width="100" align="center" cellpadding=10>
  <tr>
    <td>
      <h3 align="center">Select a Node</h3>
      <form method="get" name="choose_node" action="KSC/custom_graph2.jsp" >
          <input type="hidden" name="intf" value="<%=request.getParameter("intf")%>" >
          <table> 
              <tr>
                  <td>
                      <p>
                          <select name="node" size="10">
                              <% for( int i=0; i < nodes.length; i++ ) { %>
                                  <% if (node_intval == nodes[i].getNodeId()) { %>
                                      <option value="<%=nodes[i].getNodeId()%>" SELECTED><%=nodes[i].getNodeLabel()%></option>
                                  <% } else { %>
                                      <option value="<%=nodes[i].getNodeId()%>"><%=nodes[i].getNodeLabel()%></option>
                                  <% } %>
                              <% } %>
                          </select>
                      </p>
                  </td>
              </tr>
              <tr>
                  <td>
                      <p> <input type="button" value="Submit" onclick="submitNodeForm()" alt="Select Node and Proceed to Step 2"/> </p>
                  </td>
              </tr>
          </table> 
      </form>
    </td>
    <% if(domains.length > 0) { %>
      <td valign="top">--or--</td>
      <td>
        <h3 align="center">Select a Domain</h3>
        <form method="get" name="choose_domain" action="KSC/custom_graph2.jsp" >
          <input type="hidden" name="intf" value="<%=request.getParameter("intf")%>" >
            <table> 
                <tr>
                    <td>
                        <p>
                            <select name="domain" size="10">
                                <% for( int i=0; i < domains.length; i++ ) { %>
                                    <% if (dom.equals(domains[i])) { %>
                                        <option value="<%=domains[i]%>" SELECTED><%=domains[i]%></option>
                                    <% } else { %>
                                        <option value="<%=domains[i]%>"><%=domains[i]%></option>
                                    <% } %>
                                <% } %>
                            </select>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td>
                        <p> <input type="button" value="Submit" onclick="submitDomainForm()" alt="Select Domain and Proceed to Step 2"/> </p>
                    </td>
                </tr>
            </table> 
          </form>
      </td>
    <% } %>
  </tr>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>

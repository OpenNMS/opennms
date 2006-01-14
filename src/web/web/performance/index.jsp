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
	import="java.util.*,
		org.opennms.web.Util,
		org.opennms.web.performance.*,
		org.opennms.web.ServletInitializer
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Performance" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="location" value="performance" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="Performance" />
</jsp:include>

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

  function validateNodeAdhoc()
  {
      var isChecked = false
      for( i = 0; i < document.choose_node_adhoc.node.length; i++ )
      {
         //make sure something is checked before proceeding
         if (document.choose_node_adhoc.node[i].selected)
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

  function submitForm()
  {
      if (validateNode())
      {
          document.choose_node.submit();
      }
  }

  function submitFormAdhoc()
  {
      if (validateNodeAdhoc())
      {
          document.choose_node_adhoc.submit();
      }
  }
</script>

<div style="width: 40%; float: left;">
  <h3>Standard Performance Reports</h3>

  <p>
    Choose a node to generate a standard performance report on.
  </p>

  <form method="get" name="choose_node" action="performance/addIntfFromNode">
    <input type="hidden" name="endUrl"
	   value="performance/choosereportanddate.jsp" />

    <select name="node" size="10">
      <% for( int i=0; i < nodes.length; i++ ) { %>
        <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
      <% } %>
    </select>

    <br/>
    <br/>

    <input type="button" value="Start" onclick="submitForm()"/>
  </form>

  <h3>Custom Performance Reports</h3>

  <p>
    Choose a node to generate a custom performance report on.
  </p>

  <form method="get" name="choose_node_adhoc" action="performance/adhoc.jsp">
    <select name="node" size="10">
      <% for( int i=0; i < nodes.length; i++ ) { %>
        <option value="<%=nodes[i].nodeId%>"><%=nodes[i].nodeLabel%></option>
      <% } %>
    </select>

    <br/>
    <br/>

    <input type="button" value="Start" onclick="submitFormAdhoc()"/>
  </form>
</div>

<div style="width: 60%; float: left;">
  <h3>Network Performance Data</h3>

  <p>
    The <strong>Standard Performance Reports</strong> provide a stock way to
    easily visualize the critical SNMP data collected from managed nodes
    throughout your network.
  <p>

  <p>
    <strong>Custom Performance Reports</strong> can be used to produce a
    single graph that contains the data of your choice from a single
    interface or node.  You can select the timeframe, line colors, line
     styles, and title of the graph and you can bookmark the results.
  </p>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>

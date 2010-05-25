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
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.web.MissingParameterException
	"
%>

<%
    int nodeId = -1;
    String nodeIdString = request.getParameter("node");
    String task = request.getParameter("task");

    if (nodeIdString == null) {
        throw new MissingParameterException("node");
    }

    try {
        nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    } catch (NumberFormatException numE)  {
        throw new ServletException(numE);
    }
    
    if (nodeId < 0) {
        throw new ServletException("Invalid node ID.");
    }
        
    //get the database node info
    Node node_db = NetworkElementFactory.getNode(nodeId);
    if (node_db == null) {
        throw new ServletException("No such node in database.");
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Configure Path Outage" />
  <jsp:param name="headTitle" value="Node Management" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="Node Management" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure Path Outage" />
</jsp:include>

<style type="text/css">
LABEL
{
  font-weight: bold;
}
</style>
<script type="text/javascript" >

  function verifyIpAddress() {
    var prompt = new String("IP Address");
    var errorMsg = new String("");
    var ipValue = new String(document.setCriticalPath.criticalIp.value);
    var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
    var ipArray = ipValue.match(ipPattern);
    if (ipValue == "0.0.0.0")
        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
    else if (ipValue == "255.255.255.255")
        errorMsg = prompt + ': ' + ipValue + ' is a special IP address and cannot be used here.';
    if (ipArray == null)
        errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
    else {
        for (i = 1; i < 5; i++) {
            thisSegment = ipArray[i];
            if (thisSegment > 255) {
                errorMsg = prompt + ': ' + ipValue + ' is not a valid IP address.';
                break;
            }
        }
    }
    if (errorMsg != ""){
        alert (errorMsg);
        document.setCriticalPath.action="admin/nodemanagement/setPathOutage.jsp?node=<%=nodeId%>&task=Enter a valid IP";
    } else {
        document.setCriticalPath.action="admin/setCriticalPath?task=Submit";
    }
    document.setCriticalPath.submit();
  }


  function Delete()
  {
      if (confirm("Are you sure you want to proceed? This action will delete any existing critical path for this node."))
      {
          document.setCriticalPath.action="admin/setCriticalPath?task=Delete";
      } else {
          document.setCriticalPath.action="admin/nodemanagement/index.jsp?node=<%=nodeId%>";
      }
      document.setCriticalPath.submit();

  }
  
  function cancel()
  {
      document.setCriticalPath.action="admin/nodemanagement/index.jsp?node=<%=nodeId%>";
      document.setCriticalPath.submit();
  }
</script>

<h2>Node: <%=node_db.getLabel()%></h2>

<% if (task != null) { %>
  <h2><%=task%></h2>
<% } %>

<hr/>

<p>
  Configuring a path outage consists of selecting an IP address/service pair
  which defines the critical path to this node.  When a node down condition
  occurs for this node, the critical path will be tested. If it fails to
  respond, the node down notifications will be suppressed. The critical path
  service is typically ICMP, and at this time ICMP is the only critical path
  service supported.
  
</p>

<br/> 
  
<form method="post" name="setCriticalPath" action="admin/setCriticalPath">

<input name="node" value=<%=nodeId%> type="hidden"/>

<p>
<label for="criticalIp">Critical path IP address in xxx.xxx.xxx.xxx format:</label><br/>
<input id="criticalIp" type="text" name="criticalIp" size="17" maxlength="15" />
</p>

<p>
<label for="criticalSvc">Critical path service:</label><br/>

  <select id="criticalSvc" name="criticalSvc" value="ICMP" size="1">
        <option value="ICMP">ICMP</option>
  </select>
</p>

<p>
<input type="submit" name="task" value="Submit" onClick="verifyIpAddress()"/>
&nbsp;
<input type="submit" name="task" value="Cancel" onClick="cancel()"/>
</p>

<br/>

<h2>Delete critical path for this node</h2>
<br/>
<p>
<input type="submit" name="task" value="Delete" onClick="Delete()"/>
</p>

</form>

<jsp:include page="/includes/footer.jsp" flush="true"/>

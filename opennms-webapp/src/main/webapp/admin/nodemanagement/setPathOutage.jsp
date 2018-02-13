<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.OnmsNode,
		org.opennms.web.servlet.MissingParameterException
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
    OnmsNode node_db = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
    if (node_db == null) {
        throw new ServletException("No such node in database.");
    }
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Configure Path Outage" />
  <jsp:param name="headTitle" value="Node Management" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="Node Management" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Configure Path Outage" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="ipaddress-js" />
</jsp:include>

<style type="text/css">
LABEL
{
  font-weight: bold;
}
</style>
<script type="text/javascript" >

  function verifyIpAddress() {
    var ipValue = new String(document.setCriticalPath.criticalIp.value);

    if (!isValidIPAddress(ipValue)) {
        alert (ipValue + " is not a valid IP address!");
        document.setCriticalPath.action="admin/nodemanagement/setPathOutage.jsp?node=<%=nodeId%>&task=Enter a valid IP address";
        return false;
    } else {
        document.setCriticalPath.action="admin/setCriticalPath?task=Submit";
        return true;
    }
  }


  function deletePathOutage()
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

<h3>Node: <%=node_db.getLabel()%></h3>

<% if (task != null) { %>
  <h2><%=task%></h2>
<% } %>

<p>
  Configuring a path outage consists of selecting an IP address/service pair
  which defines the critical path to this node.  When a node down condition
  occurs for this node, the critical path will be tested. If it fails to
  respond, the node down notifications will be suppressed. The critical path
  service is typically ICMP, and at this time ICMP is the only critical path
  service supported.
  
</p>

<form role="form" method="post" class="form-inline" name="setCriticalPath" action="admin/setCriticalPath" onsubmit="return verifyIpAddress();">

<input name="node" value=<%=nodeId%> type="hidden"/>

<p>
<label for="criticalIp">Critical path IP address in xxx.xxx.xxx.xxx or xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format:</label><br/>
<input id="criticalIp" class="form-control" type="text" name="criticalIp" size="57" maxlength="55" />
</p>

<p>
<label for="criticalSvc">Critical path service:</label><br/>

  <select id="criticalSvc" class="form-control" name="criticalSvc" value="ICMP">
        <option value="ICMP">ICMP</option>
  </select>
</p>

<p>
<input type="submit" class="btn btn-default" name="task" value="Submit"/>
&nbsp;
<input type="button" class="btn btn-default" name="task" value="Cancel" onClick="cancel()"/>
</p>

<h3>Delete critical path for this node</h3>

<p>
<input type="button" class="btn btn-default" name="task" value="Delete" onClick="deletePathOutage()"/>
</p>

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Node Management")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Path Outage")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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

<h3>Node: <%=WebSecurityUtils.sanitizeString(node_db.getLabel())%></h3>

<% if (task != null) { %>
  <h2><%= WebSecurityUtils.sanitizeString(task) %></h2>
<% } %>

<p>
  Configuring a path outage consists of selecting an IP address/service pair
  which defines the critical path to this node.  When a node down condition
  occurs for this node, the critical path will be tested. If it fails to
  respond, the node down notifications will be suppressed. The critical path
  service is typically ICMP, and at this time ICMP is the only critical path
  service supported.
  
</p>

<form role="form" method="post" class="form mb-2 col-md-6" name="setCriticalPath" action="admin/setCriticalPath" onsubmit="return verifyIpAddress();">

<input name="node" value=<%=nodeId%> type="hidden"/>

<div class="form-group">
<label for="criticalIp">Critical path IP address in xxx.xxx.xxx.xxx or xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx format:</label>
<input id="criticalIp" class="form-control" type="text" name="criticalIp" size="57" maxlength="55" />
</div>

<div class="form-group">
<label for="criticalSvc">Critical path service:</label>

  <select id="criticalSvc" class="form-control custom-select" name="criticalSvc" value="ICMP">
        <option value="ICMP">ICMP</option>
  </select>
</div>

<div class="form-group">
<input type="submit" class="btn btn-secondary" name="task" value="Submit"/>
<input type="button" class="btn btn-secondary" name="task" value="Cancel" onClick="cancel()"/>
</div>

<h3>Delete critical path for this node</h3>

<input type="button" class="btn btn-secondary" name="task" value="Delete" onClick="deletePathOutage()"/>

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

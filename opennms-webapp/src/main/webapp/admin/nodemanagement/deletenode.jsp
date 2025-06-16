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
	import="
		org.opennms.netmgt.model.OnmsNode,
		org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.servlet.MissingParameterException
	"
%>

<%
    int nodeId = -1;
    String nodeIdString = request.getParameter("node");

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
          .breadcrumb("Node Management")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

  function applyChanges()
  {
        var hasCheckedItems = false;
        for (var i = 0; i < document.deleteNode.elements.length; i++)
        {
                if (document.deleteNode.elements[i].type == "checkbox")
                {
                        if (document.deleteNode.elements[i].checked)
                        {
                                hasCheckedItems = true;
                                break;
                        }
                }
        }
                
        if (hasCheckedItems)
        {
                // Return true if we want the form to submit, false otherwise
                return confirm("Are you sure you want to proceed? This action will permanently delete the checked items and cannot be undone.");
        }
        else
        {
                alert("No node or data item is selected!");
                // Return false so that the form is not submitted
                return false;
        }
  }
</script>

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Node: <%=node_db.getLabel()%></span>
      </div>
      <div class="card-body">
        <form method="post" name="deleteNode" action="admin/deleteSelNodes" onSubmit="return applyChanges();">
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
          <div class="form-group">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" name="nodeCheck" id="nodeCheck" value='<%= nodeId %>'>
              <label class="form-check-label" for="nodeCheck">Node</label>
            </div>
          </div>

          <div class="form-group">
            <div class="form-check">
              <input class="form-check-input" type="checkbox" name="nodeData" id="nodeData" value='<%= nodeId %>'>
              <label class="form-check-label" for="nodeData">Data</label>
            </div>
          </div>

          <div class="form-group">
            <input type="submit" class="btn btn-secondary" value="Delete">
            <a href="admin/nodemanagement/index.jsp?node=<%=nodeId%>" class="btn btn-secondary">Cancel</a>
          </div>
        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
  
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Node: <%=node_db.getLabel()%></span>
      </div>
      <div class="card-body">
        <p>
          To permanently delete a node (and all associated interfaces, services,
          outages, events and notifications), check the "Node" box and select "Delete".
        </p>

        <p>
          Checking the "Data" box will delete the SNMP performance and response
          time directories from the system as well.  Note that it is possible for
          the directory to be deleted <i>before</i> the fact that the node has been
          removed has fully propagated through the system. Thus the system may
          recreate the directory for a single update after this action. In that
          case, the directory will need to be removed manually.
        </p>

        <p>
          <strong>Note:</strong> If the IP address of any of the node's interfaces
          is still configured for discovery and still responding to pings, the node will
          be discovered again. To prevent this, either remove the IP address from the
          discovery range or unmanage the device instead of deleting it.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

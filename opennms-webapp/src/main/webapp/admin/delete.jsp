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
		org.opennms.web.admin.nodeManagement.*,
		org.opennms.core.utils.WebSecurityUtils
	"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%!
    int interfaceIndex;
    int serviceIndex;
%>

<%
    HttpSession userSession = request.getSession(false);
    List<ManagedNode> nodes = null;
    
    interfaceIndex = 0;
    serviceIndex = 0;
    
    if (userSession != null)
    {
        nodes = (List<ManagedNode>)userSession.getAttribute("listAll.delete.jsp");
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Delete Nodes")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Delete Nodes")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

  function applyChanges()
  {
        var hasCheckedItems = false;
        for (var i = 0; i < document.deleteAll.elements.length; i++)
        {
                if (document.deleteAll.elements[i].type == "checkbox")
                {
                        if (document.deleteAll.elements[i].checked)
                        {
                                hasCheckedItems = true;
                                break;
                        }
                }
        }
                
        if (hasCheckedItems)
        {
                if (confirm("Are you sure you want to proceed? This action will permanently delete the checked nodes and cannot be undone."))
                {
                        document.deleteAll.submit();
                }
        }
        else
        {
                alert("No nodes and data items are selected!");
        }
  }
  
  function cancel()
  {
      document.deleteAll.action="admin/index.jsp";
      document.deleteAll.submit();
  }
  
  function checkAll()
  {
      for (var c = 0; c < document.deleteAll.elements.length; c++)
      {  
          if (document.deleteAll.elements[c].type == "checkbox")
          {
              document.deleteAll.elements[c].checked = true;
          }
      }
  }
  
  function uncheckAll()
  {
      for (var c = 0; c < document.deleteAll.elements.length; c++)
      {  
          if (document.deleteAll.elements[c].type == "checkbox")
          {
              
              document.deleteAll.elements[c].checked = false;
          }
      }
  }
  
</script>

<form method="post" name="deleteAll" action="admin/deleteSelNodes">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
<%
  int midNodeIndex = 1;
  
  if (nodes.size() > 1)
  {
    midNodeIndex = nodes.size()/2;
  }
%>

<div class="card">
  <div class="card-header">
    <span>Delete Nodes</span>
  </div>
  <div class="card-body">
	<P>The nodes present in the system are listed below. To permanently delete a node (and all associated
	   interfaces, services, outages, events and notifications), check the "Delete?" box beside the node's ID and
           select "Delete Nodes". You may check more than one.
        </P>
	<P>Checking the "Data?" box will delete the SNMP performance and response time directories from the system as well.
	   Note that it is possible for the directory to be deleted <i>before</i> the fact that the node has been removed has
           fully propagated through the system. Thus the system may recreate the directory for a single update after
           this action. In that case, the directory will need to be removed manually.
	</P>
        <P><b>Note:</b> If the IP address of any of the node's interfaces is still configured for discovery
	   and still responds to pings, the node will be discovered again. To prevent this, either remove the IP address
           from the discovery range or unmanage the device instead of deleting it.
        </P>

        <div class="form-group">
          <input type="button" class="btn btn-secondary" value="Delete Nodes" onClick="applyChanges()">
          <input type="button" class="btn btn-secondary" value="Cancel" onClick="cancel()">
          <input type="button" class="btn btn-secondary" value="Select All" onClick="checkAll()">
          <input type="button" class="btn btn-secondary" value="Unselect All" onClick="uncheckAll()">
          <input type="reset" class="btn btn-secondary">
        </div>
      
      <div class="row form-group">
   <% if (nodes.size() > 0) { %>
	<div class="col-md-6" id="contentleft">
          <table class="table table-sm">
            <tr>
              <th class="text-center" width="5%">Delete?</th>
              <th class="text-center" width="5%">Data?</th>
              <th class="text-center" width="5%">Node ID</th>
              <th width="10%">Node Label</th>
            </tr>

            <%=buildTableRows(nodes, 0, midNodeIndex)%>
          </table>
	</div>
          <% } /*end if*/ %>

      <!--see if there is a second column to draw-->
      <% if (midNodeIndex < nodes.size()) { %>
	<div class="col-md-6" id="contentright">
          <table class="table table-sm">
            <tr>
              <th class="text-center" width="5%">Delete?</th>
              <th class="text-center" width="5%">Data?</th>
              <th class="text-center" width="5%">Node ID</th>
              <th width="10%">Node Label</th>
            </tr>

            <%=buildTableRows(nodes, midNodeIndex, nodes.size())%>

          </table>
	</div>
        <% } /*end if */ %>
      </div> <!-- row -->

      <div class="form-group">
          <input type="button" class="btn btn-secondary" value="Delete Nodes" onClick="applyChanges()">
          <input type="button" class="btn btn-secondary" value="Cancel" onClick="cancel()">
          <input type="button" class="btn btn-secondary" value="Select All" onClick="checkAll()">
          <input type="button" class="btn btn-secondary" value="Unselect All" onClick="uncheckAll()">
          <input type="reset" class="btn btn-secondary">
      </div>
  </div> <!-- card-body -->
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

<%!
      public String buildTableRows(List<ManagedNode> nodes, int start, int stop)
      	throws java.sql.SQLException
      {
          StringBuffer row = new StringBuffer();
          
          for (int i = start; i < stop; i++)
          {
                
                ManagedNode curNode = nodes.get(i);
                String nodelabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(curNode.getNodeID());
		int nodeid = curNode.getNodeID();
                 
          row.append("<tr>\n");
          row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"nodeCheck\" value=\""+ nodeid +"\" >");
          row.append("</td>\n");
          row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
          row.append("<input type=\"checkbox\" name=\"nodeData\" value=\""+ nodeid +"\" >");
          row.append("</td>\n");
          row.append("<td class=\"standard\" width=\"5%\" align=\"center\">");
	  row.append(nodeid);
          row.append("</td>\n");
          row.append("<td class=\"standard\" width=\"10%\" align=\"left\">");
	  row.append(WebSecurityUtils.sanitizeString(nodelabel));
          row.append("</td>\n");
          row.append("</tr>\n");
          } /* end i for */
          
          return row.toString();
      }
      
%>

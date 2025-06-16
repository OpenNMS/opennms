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
		org.opennms.core.spring.BeanUtils,
		org.opennms.netmgt.dao.api.NodeLabel,
		org.opennms.netmgt.model.OnmsNode.NodeLabelSource,
		org.opennms.web.servlet.MissingParameterException,
		org.opennms.core.utils.WebSecurityUtils,
		java.util.*
	"
%>

<%!
    EnumMap<NodeLabelSource,String> typeMap;

    public void init() {
        typeMap = new EnumMap<NodeLabelSource,String>(NodeLabelSource.class);
        typeMap.put(NodeLabelSource.USER,        "User defined" );
        typeMap.put(NodeLabelSource.NETBIOS,     "Windows/NETBIOS Name" );
        typeMap.put(NodeLabelSource.HOSTNAME,    "DNS Hostname" );
        typeMap.put(NodeLabelSource.SYSNAME,     "SNMP System Name" );
        typeMap.put(NodeLabelSource.ADDRESS,     "IP Address" );
        typeMap.put(NodeLabelSource.UNKNOWN,     "Unknown" );
    }
%>

<%

    NodeLabel nodeLabel = BeanUtils.getBean("daoContext", "nodeLabel", NodeLabel.class);

    String nodeIdString = request.getParameter( "node" );

    
    if( nodeIdString == null ) {
        throw new MissingParameterException( "node" );
    }

    int nodeId = WebSecurityUtils.safeParseInt( nodeIdString );

    NodeLabel currentLabel = nodeLabel.retrieveLabel( nodeId );
    NodeLabel autoLabel = nodeLabel.computeLabel( nodeId );

    if( currentLabel == null || autoLabel == null ) {
        // XXX handle this WAY better, very awful
        throw new ServletException( "No such node in database" );
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Change Node Label")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Change Node Label")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Current Label</span>
      </div>
      <div class="card-body">
        <p>
          <a href="element/node.jsp?node=<%=nodeId%>" title="More information for this node"><%=WebSecurityUtils.sanitizeString(currentLabel.getLabel())%></a> (<%=typeMap.get(currentLabel.getSource())%>)
        </p>
      </div>
    </div> <!-- panel -->
  </div> <!-- column -->

  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Choose a New Label</span>
      </div>
      <div class="card-body">
        <p>
          You can either specify a name or allow the system to automatically
          select the name.
        </p>

        <form role="form" action="admin/nodeLabelChange" method="post">
          <input type="hidden" name="node" value="<%=nodeId%>" />

            <label>User Defined</label>
            <br/>
            <input type="radio" name="labeltype" class="mr-1" value="user" <%=(currentLabel.getSource() == NodeLabelSource.USER) ? "checked" : ""%> />
            <input type="text" name="userlabel" value="<%=WebSecurityUtils.sanitizeString(currentLabel.getLabel())%>" maxlength="255" size="32"/>

          <br/>
          <br/>

            <label>Automatic</label>
            <br/>
            <input type="radio" name="labeltype" class="mr-1" value="auto" <%=(currentLabel.getSource() != NodeLabelSource.USER) ? "checked" : ""%> />
            <%=WebSecurityUtils.sanitizeString(autoLabel.getLabel())%> (<%=typeMap.get(autoLabel.getSource())%>)

          <br/>
          <br/>

          <div class="form-group">
            <input type="submit" class="btn btn-secondary" value="Change Label" />
            <input type="reset" class="btn btn-secondary" />
          </div>

        </form>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
<%@page language="java"	contentType="text/html" session="true"%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>
<%
  String nodeId = WebSecurityUtils.sanitizeString(request.getParameter("node"));
  String nodeLabel = WebSecurityUtils.sanitizeString(request.getParameter("nodelabel"));

  if (nodeId == null) {
    throw new org.opennms.web.servlet.MissingParameterException("node");
  }
  if (nodeLabel == null) {
    throw new org.opennms.web.servlet.MissingParameterException("nodelabel");
  }
%>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Select SNMP Interfaces")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Select SNMP Interfaces")
          .ngApp("onms-interfaces-config")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="angular-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="onms-interfaces-config" />
</jsp:include>

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Choose SNMP Interfaces for Data Collection</span>
      </div>
      <div class="card-body">
        <p>
        Listed below are all the known interfaces for the selected node. If
        <strong>snmpStorageFlag</strong> is set to <strong>select</strong> for a collection scheme that includes
        the interface marked as <strong>Primary</strong>, only the interfaces checked below will have
        their collected SNMP data stored. This has no effect if <strong>snmpStorageFlag</strong> is
        set to <strong>primary</strong> or <strong>all</strong>.
        </p>
        <p>
        In order to change what interfaces are scheduled for collection, simply click
        the checkbox shown on the collect column, and the change will immediately update the database.
        </p>
        <p>
	    <strong>Node ID</strong>: <%=nodeId%><br/>
	    <strong>Node Label</strong>: <%=nodeLabel%><br/>
        </p>
        <p>
        <div id="onms-interfaces-config" ng-app="onms.default.apps">
          <div growl></div>
          <onms-interfaces-config node="<%=nodeId%>"/>
        </div>
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

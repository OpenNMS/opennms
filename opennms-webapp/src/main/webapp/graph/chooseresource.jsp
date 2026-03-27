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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>
<%
  String node = request.getParameter("node");
  if (node == null) {
    String parentResource = request.getParameter("parentResourceId");
    if (parentResource != null) {
      java.util.regex.Matcher m = java.util.regex.Pattern.compile("node\\[([^\\]]+)\\]").matcher(parentResource);
      if (m.find()) {
        node = m.group(1);
      }
    }
  }
  String reports = request.getParameter("reports");
  String endUrl = request.getParameter("endUrl");
  pageContext.setAttribute("node", node == null ? "null" : node);
  pageContext.setAttribute("reports", reports == null ? "" : reports);
  pageContext.setAttribute("endUrl", endUrl == null ? "" : endUrl);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Choose")
          .headTitle("Resource Graphs")
          .headTitle("Reports")
          .breadcrumb("Reports", "report/index.jsp")
          .breadcrumb("Resource Graphs", "graph/index.jsp")
          .breadcrumb("Choose")
          .ngApp("onms-resources")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="" ng-controller="NodeResourcesCtrl" ng-init="init('${e:forJavaScript(node)}','${e:forJavaScript(reports)}','${e:forJavaScript(endUrl)}')">

  <div growl></div>

  <h4>
    Node: <strong><a href="{{ nodeLink }}">{{ nodeLabel }}</a></strong>
  </h4>

  <!-- Loading Message -->
  <div class="jumbotron" ng-show="!loaded">
    <div class="container">
      <h1>Loading Resources</h1>
      <p>Please wait while all the resources are loaded into your browser.</p>
      <p>This could take time, depending on the number of resources for this node.</p>
    </div>
  </div>

  <div class="jumbotron" ng-show="loaded && !hasResources">
    <div class="container">
      <h1>There are no resources for this node</h1>
      <p>Please check the data collection configuration for this node.</p>
    </div>
  </div>

  <div class="row" ng-show="hasResources">
    <div class="col-md-6">
      <h4>Resources</h4>
    </div>
    <div class="col-md-6">
      <form class="form-inline pull-right mb-4">
        <div class="input-group mr-4">
          <div class="input-group-prepend">
            <div class="input-group-text">
              <span class="fas fa-magnifying-glass"></span>
            </div>
          </div>
          <input class="form-control" type="text" placeholder="Search/Filter Resources" ng-model="searchQuery"></input>
          <div class="input-group-prepend" ng-show="searchQuery.length > 0">
            <div class="input-group-text">
              <span class="fas fa-xmark" ng-click="searchQuery = ''"></span>
            </div>
          </div>
        </div>
        <div class="btn-group">
          <button type="button" class="btn btn-secondary" ng-click="checkAll(true)"><i class="fas fa-check"></i> Select All</button>
          <button type="button" class="btn btn-secondary" ng-click="checkAll(false)"><i class="fas fa-xmark"></i> Clear All</button>
          <button type="button" class="btn btn-secondary" ng-click="graphSelected()"><i class="fas fa-table-cells-large"></i> Graph Selection</button>
          <button type="button" class="btn btn-secondary" ng-click="graphAll()"><i class="fas fa-table-cells"></i> Graph All</button>
        </div>
      </form>
    </div>
  </div>

  <div class="row" ng-show="hasResources">
    <div class="col-md-12">
      <uib-accordion close-others="false">
        <div ng-click="isCollapsed[type] = !isCollapsed[type]" uib-accordion-group is-open="true" class="card-default" ng-repeat="(type, group) in filteredResources" ng-show="group.length > 0">
          <uib-accordion-heading>
            {{ type }} <i class="pull-right fa" ng-class="{'fa-chevron-down': isCollapsed[type], 'fa-chevron-right': !isCollapsed[type]}"></i>
          </uib-accordion-heading>
            <div class="checkbox" ng-repeat="resource in group | orderBy:'label'">
              <label><input type="checkbox" class="mr-1" ng-model="resource.selected"/>{{ resource.label }}</label>
              <a ng-if="resource.hasIngressFlows===true && resource.hasEgressFlows===true" ng-href="{{resource.flowGraphUrl}}" target="_blank" title="Open flow graphs"><span class="badge badge-secondary" title="Flows: ingress/egress flow data available"><i class="fas fa-right-left"></i>&nbsp;flow data</span></a>
              <a ng-if="resource.hasIngressFlows===true && resource.hasEgressFlows===false" ng-href="{{resource.flowGraphUrl}}" target="_blank" title="Open flow graphs"><span class="badge badge-secondary" title="Flows: ingress flow data available"><i class="fas fa-arrow-left-long"></i>&nbsp;flow data</span></a>
              <a ng-if="resource.hasIngressFlows===false && resource.hasEgressFlows===true" ng-href="{{resource.flowGraphUrl}}" target="_blank" title="Open flow graphs"><span class="badge badge-secondary" title="Flows: egress flow data available"><i class="fas fa-arrow-right-long"></i>&nbsp;flow data</span></a>
            </div>
        </div>
      </uib-accordion>
    </div>
  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="angular-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="onms-resources" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

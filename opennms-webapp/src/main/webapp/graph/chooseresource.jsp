<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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
  pageContext.setAttribute("node", node == null ? "null" : "'" + node + "'");
  pageContext.setAttribute("reports", reports == null ? "null" : "'" + reports + "'");
  pageContext.setAttribute("endUrl", endUrl == null ? "null" : "'" + endUrl + "'");
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Choose Resource" />
  <jsp:param name="headTitle" value="Choose" />
  <jsp:param name="headTitle" value="Resource Graphs" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='graph/index.jsp'>Resource Graphs</a>" />
  <jsp:param name="breadcrumb" value="Choose" />
</jsp:include>

<div class="container-fluid" ng-app="onms-resources" ng-controller="NodeResourcesCtrl" ng-init="init(${node},${reports},${endUrl})">

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
      <form class="form-inline pull-right">
        <div class="input-group">
          <span class="input-group-addon">
            <span class="glyphicon glyphicon-search"></span>
          </span>
          <input class="form-control" type="text" placeholder="Search/Filter Resources" ng-model="searchQuery"></input>
          <span class="input-group-addon" ng-show="searchQuery.length > 0">
            <span class="glyphicon glyphicon-remove" ng-click="searchQuery = ''"></span>
          </span>
        </div>
        <div class="btn-group">
          <button type="button" class="btn btn-default" ng-click="checkAll(true)">Select All <span class="glyphicon glyphicon-check"/></button>
          <button type="button" class="btn btn-default" ng-click="checkAll(false)">Clear All <span class="glyphicon glyphicon-remove"/></button>
          <button type="button" class="btn btn-default" ng-click="graphSelected()">Graph Selection <span class="glyphicon glyphicon-th-large"/></button>
          <button type="button" class="btn btn-default" ng-click="graphAll()">Graph All <span class="glyphicon glyphicon-th"/></button>
        </div>
      </form>
    </div>
  </div>

  <div class="row" ng-show="hasResources">
    <div class="col-md-12">
      <uib-accordion close-others="false">
        <div ng-click="isCollapsed[type] = !isCollapsed[type]" uib-accordion-group is-open="true" class="panel-default" ng-repeat="(type, group) in filteredResources" ng-show="group.length > 0">
          <uib-accordion-heading>
            {{ type }} <i class="pull-right glyphicon" ng-class="{'glyphicon-chevron-down': isCollapsed[type], 'glyphicon-chevron-right': !isCollapsed[type]}"></i>
          </uib-accordion-heading>
            <div class="checkbox" ng-repeat="resource in group | orderBy:'label'">
              <label><input type="checkbox" ng-model="resource.selected"/>{{ resource.label }}</label>
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

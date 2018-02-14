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

<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.springframework.security.AclUtils"%>

<% pageContext.setAttribute("nodeId", request.getParameter("node")); %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Modify Asset" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Asset" />
  <jsp:param name="breadcrumb" value="<a href ='asset/index.jsp'>Assets</a>" />
  <jsp:param name="breadcrumb" value="Modify" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="jquery-ui-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="bootbox-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="onms-assets" />
</jsp:include>

<%
  AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());

  Integer nodeId = null;

  try {
    nodeId = Integer.valueOf(request.getParameter("node"));
  } catch (NumberFormatException e) {
%>
    <h2>Error parsing node parameter.</h2>
<%
  }

  if (nodeId != null) {
    if (accessChecker.isNodeAccessible(nodeId)) {
%>
      <div class="container-fluid" ng-app="onms-assets" ng-controller="NodeAssetsCtrl" ng-init="init(${nodeId})">

        <div growl></div>

        <h4>
          Node: <strong><a href="element/node.jsp?node=${nodeId}">{{ nodeLabel }}</a></strong>
        </h4>
        <p>
          Last modified by {{ master['lastModifiedBy'] }} at {{ master['lastModifiedDate'] | date }}
        </p>

        <form name="assetForm" novalidate>
          <div class="row" ng-repeat="row in config.rows">
            <div ng-class="col.class" ng-repeat="col in row.columns">
              <div class="panel panel-default" ng-repeat="panel in col.panels">
                <div class="panel-heading">
                  <h3 class="panel-title">{{ panel.title }}</h3>
                </div>
                <div class="panel-body">
                  <div class="form-horizontal" ng-repeat="field in panel.fields">
                    <div class="form-group" ng-class="{ 'has-error': assetForm[field.model].$invalid && !assetForm[field.model].$pristine, 'has-warning': assetForm[field.model].$dirty }">
                      <label class="control-label col-md-3" for="{{ field.model }}" uib-tooltip="{{ field.tooltip  }}">{{ field.label }}</label>
                      <div class="col-md-9">
                        <%-- Static/ReadOnly fields --%>
                        <p class="form-control-static" ng-if="field.type=='static'">{{ asset[field.model] }}</p>
                        <%-- Standard fields with typeahead suggestions --%>
                        <input type="text" class="form-control" id="{{ field.model }}" name="{{ field.model }}" ng-model="asset[field.model]" ng-if="field.type=='text'"
                          typeahead-editable="true" typeahead-min-length="0" ng-pattern="field.pattern"
                          uib-typeahead="suggestion for suggestion in getSuggestions(field.model) | filter:$viewValue"></input>
                        <%-- Password fields --%>
                        <input type="password" class="form-control" ng-model="asset[field.model]" ng-if="field.type=='password'"></input>
                        <%-- Textarea fields --%>
                        <textarea class="form-control" style="height: 20em;" ng-model="asset[field.model]" ng-if="field.type=='textarea'"></textarea>
                        <%-- Date fields with Popup Picker --%>
                        <p class="input-group" ng-if="field.type=='date'">
                          <input type="text" class="form-control" uib-datepicker-popup="{{ dateFormat }}" is-open="field.open" ng-model="asset[field.model]" placeholder="Specify date using this format: {{ dateFormat }}" />
                          <span class="input-group-btn">
                            <button type="button" class="btn btn-default" ng-click="field.open=true"><i class="glyphicon glyphicon-calendar"></i></button>
                          </span>
                        </p>
                        <%-- List/Select fields --%>
                        <select class="form-control" ng-model="asset[field.model]" ng-if="field.type=='select'">
                          <option ng-repeat="value in field.options">{{value}}</option>
                        </select>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="row">
            <div class="col-md-6">
              <div class="btn-group">
                <button type="button" class="btn btn-default" ng-click="save()" id="save-asset" ng-disabled="assetForm.$invalid">Save Asset Record&nbsp;&nbsp;&nbsp;
                  <span class="glyphicon glyphicon-save"><span>
                </button>
                <button type="button" class="btn btn-default" ng-click="reset()" id="reset-asset">Reset&nbsp;&nbsp;&nbsp;
                  <span class="glyphicon glyphicon-refresh"><span>
                </button>
              </div>
            </div>
          </div>
        </form>
      </div>
<%
    } else {
%>
      <h2>Access denied.</h2>
<%
    }
  }
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>


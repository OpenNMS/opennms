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

<% pageContext.setAttribute("nodeId", request.getParameter("node")); %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="norequirejs" value="true" />
  <jsp:param name="disableCoreWeb" value="true" />
  <jsp:param name="title" value="Modify Asset" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Asset" />
  <jsp:param name="breadcrumb" value="<a href ='asset/index.jsp'>Assets</a>" />
  <jsp:param name="breadcrumb" value="Modify" />
  <jsp:param name="link" value='<link rel="stylesheet" type="text/css" href="lib/angular-growl-v2/build/angular-growl.css" />' />
  <jsp:param name="script" value='<script type="text/javascript" src="lib/angular/angular.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="lib/angular-growl-v2/build/angular-growl.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="lib/bootbox/bootbox.js"></script>' />
  <jsp:param name="script" value='<script type="text/javascript" src="js/onms-assets/app.js"></script>' />
</jsp:include>

<div class="container-fluid" ng-app="onms-assets" ng-controller="NodeAssetsCtrl" ng-init="init(${nodeId})">

  <div growl></div>

  <h4>
    Node: <strong><a href="element/node.jsp?node=${nodeId}">{{ nodeLabel }}</a></strong>
  </h4>

  <form name="assetForm" novalidate>
    <div class="row" ng-repeat="row in config.rows">
      <div ng-class="col.class" ng-repeat="col in row.columns">
        <div class="panel panel-default" ng-repeat="panel in col.panels">
          <div class="panel-heading">
            <h3 class="panel-title">{{ panel.title }}</h3>
          </div>
          <div class="panel-body">
            <div class="form-horizontal" ng-repeat="field in panel.fields"> 
              <asset-field field="field" asset="asset" form="assetForm"></asset-field>
            </div> 
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-md-6">
        <div class="btn-group btn-group-lg">
          <button type="button" class="btn btn-default" ng-click="save()" id="save-asset" ng-disabled="assetForm.$invalid">Save Asset Record&nbsp;&nbsp;&nbsp;
            <span class="glyphicon glyphicon-save"><span>
          </button>
          <button type="button" class="btn btn-default" ng-click="reset()" id="reset-asset" ng-disabled="assetForm.$invalid">Reset&nbsp;&nbsp;&nbsp;
            <span class="glyphicon glyphicon-refresh"><span>
          </button>
        </div>
      </div>
    </div>
  </form>

</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>


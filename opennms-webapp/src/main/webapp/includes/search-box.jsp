<%@page language="java"
        contentType="text/html"
        session="true"
%>

<script type="text/javascript" src="lib/angular/angular.js"></script>
<script type="text/javascript" src="lib/angular-bootstrap/ui-bootstrap-tpls.js"></script>
<script type="text/javascript" src="js/onms-search/app.js"></script>

<div ng-app="onms-search" ng-controller="SearchCtrl">

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title"><a href="graph/index.jsp">Resource Graphs</a></h3>
    </div>
    <div class="panel-body">
      <form class="form" role="form">
        <div class="input-group">
          <input type="text" class="form-control" ng-model="asyncNode" placeholder="Type the node label"
            uib-typeahead="node as node.label for node in getNodes($viewValue)" typeahead-editable="false"
            typeahead-loading="nodeLoadingNodes" typeahead-no-results="nodeNoResults" typeahead-min-length="1"
            typeahead-on-select="goToChooseResources($item)"/>
          <span class="input-group-addon"><span class="glyphicon glyphicon-search"/></span>
          <i ng-show="nodeLoadingNodes" class="glyphicon glyphicon-refresh"></i>
          <p class="help-block" ng-show="nodeNoResults">
            <i class="glyphicon glyphicon-remove"></i> No Results Found
          </p>
        </div>
      </form>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title"><a href="graph/index.jsp">KSC Reports</a></h3>
    </div>
    <div class="panel-body">
      <form class="form" role="form">
        <div class="input-group">
          <input type="text" class="form-control" ng-model="asyncKsc" placeholder="Type the KSC report name"
            uib-typeahead="ksc as ksc.label for ksc in getKscReports($viewValue)" typeahead-editable="false"
            typeahead-loading="kscLoadingNodes" typeahead-no-results="kscNoResults" typeahead-min-length="1"
            typeahead-on-select="goToKscReport($item)"/>
          <span class="input-group-addon"><span class="glyphicon glyphicon-search"/></span>
          <i ng-show="kscLoadingNodes" class="glyphicon glyphicon-refresh"></i>
          <p class="help-block" ng-show="nodeNoResults">
            <i class="glyphicon glyphicon-remove"></i> No Results Found
          </p>
        </div>
      </form>
    </div>
  </div>

</div>

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2017 The OpenNMS Group, Inc.
*/

'use strict';

const $ = require('vendor/jquery-js');
const angular = require('vendor/angular-js');
const bootbox = require('vendor/bootbox-js');
require('lib/onms-http');

import Util from 'lib/util';

angular.module('onms-ksc-wizard', [
  'onms.http',
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(3000);
  growlProvider.globalPosition('bottom-center');
}])

.filter('startFrom', function() {
  return function(input, start) {
    var s = parseInt(start, 10);
    if (input) {
      return input.length < s ? input : input.slice(s);
    }
    return [];
  };
})

.controller('KSCResourceCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.level = 0; // 0: Top-Level, 1: Resource-Level, 2: Graph-Level

  $scope.selectedNode = null;
  $scope.selectedResource = null;

  $scope.resources = [];
  $scope.filteredResources = [];
  $scope.pageSize = 10;
  $scope.maxSize = 5;
  $scope.totalItems = 0;

  $scope.goBack = function() {
    $scope.setLevel($scope.level - 1, true);
  };

  $scope.setLevel = function(level, reset) {
    $scope.resourceFilter = '';
    $scope.level = level;
    var resources = [];
    switch (level) {
      case 0:
        $http.get('rest/resources?depth=0').success(function(data) {
          $scope.resources = data.resource;
          $scope.filteredResources = angular.copy($scope.resources);
          $scope.updateResources();
        });
        break;
      case 1:
        if (reset) {
          $scope.selectedResource = angular.copy($scope.selectedNode);
        } else {
          $scope.selectedNode = angular.copy($scope.selectedResource);
        }
        $http.get('rest/resources/' + $scope.getSelectedId()).success(function(data) {
          $scope.resources = data.children.resource;
          $scope.filteredResources = angular.copy($scope.resources);
          $scope.updateResources();
        });
        break;
      default:
        $scope.resources = [];
        $scope.filteredResources = [];
        break;
    }
  };

  $scope.getSelectedId = function() {
    return escape($scope.selectedResource.id.replace('%3A',':'));
  };

  $scope.chooseResource = function() {
    if ($scope.selectedResource === null) {
      growl.warning('Please select a resource from the list.');
      return; 
    }
    $window.document.location.href = 'KSC/customGraphEditDetails.htm?resourceId=' + $scope.getSelectedId();
  };

  $scope.selectResource = function(resource) {
    $scope.selectedResource = resource;
  };

  $scope.viewResource = function() {
    if ($scope.selectedResource === null) {
      growl.warning('Please select a resource from the list.');
      return; 
    }
    $scope.setLevel($scope.level + 1);
  };

  $scope.updateResources = function() {
    $scope.currentPage = 1;
    $scope.totalItems = $scope.filteredResources.length;
    $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
  };

  $scope.$watch('resourceFilter', function() {
    $scope.filteredResources = $filter('filter')($scope.resources, $scope.resourceFilter);
    $scope.updateResources();
  });

  $scope.setLevel(0);
}])

.controller('KSCWizardCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.resources = [];
  $scope.filteredResources = [];
  $scope.pageSize = 10;
  $scope.maxSize = 5;
  $scope.totalItems = 0;

  $scope.reportSelected = null;
  $scope.reports = [];
  $scope.filteredReports = [];
  $scope.kscPageSize = 10;
  $scope.kscMaxSize = 5;
  $scope.kscTotalItems = 0;

  $scope.actionUrl = Util.getBaseHref() + 'KSC/formProcMain.htm';

  $scope.reloadConfig = function() {
    bootbox.dialog({
      message: 'Are you sure you want to do this?<br/>',
      title: 'Reload KSC Configuration',
      buttons: {
        reload: {
          label: 'Yes',
          className: 'btn-success',
          callback: function() {
            $http.put('rest/ksc/reloadConfig')
              .success(function() {
                growl.success('The configuration has been reloaded.');
              })
              .error(function(msg) {
                growl.error(msg);
              });
          }
        },
        main: {
          label: 'No',
          className: 'btn-danger'
        }
      }
    });
  };

  $scope.viewReport = function() {
    if ($scope.reportSelected === null) {
      growl.warning('Please select the report you want to view.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=View&report=' + $scope.reportSelected.id;
  };

  $scope.customizeReport = function() {
    if ($scope.reportSelected === null) {
      growl.warning('Please select the report you want to customize.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=Customize&report=' + $scope.reportSelected.id;
  };

  $scope.createReport = function() {
    $window.location.href = $scope.actionUrl + '?report_action=Create';
  };

  $scope.createReportFromExisting = function() {
    if ($scope.reportSelected === null) {
      growl.warning('Please select the report you want to copy.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=CreateFrom&report=' + $scope.reportSelected.id;
  };

  $scope.deleteReport = function() {
    if ($scope.reportSelected === null) {
      growl.warning('Please select the report you want to delete.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=Delete&report=' + $scope.reportSelected.id;
  };

  $scope.selectReport = function(id) {
    $scope.reportSelected = id;
  };

  $scope.selectResource = function(resource) {
    var baseUrl = Util.getBaseHref() + 'KSC/customView.htm';
    if (resource.name.indexOf(':') > 0) {
      $window.location.href = baseUrl + '?type=nodeSource&report=' + resource.name;
    } else {
      $window.location.href = baseUrl + '?type=node&report=' + resource.name;
    }
  };

  $scope.updateResources = function() {
    $scope.currentPage = 1;
    $scope.totalItems = $scope.filteredResources.length;
    $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
  };

  $scope.updateReports = function() {
    $scope.kscCurrentPage = 1;
    $scope.kscTotalItems = $scope.filteredReports.length;
    $scope.kscNumPages = Math.ceil($scope.kscTotalItems / $scope.kscPageSize);
  };

  $http.get('rest/resources?depth=0').success(function(data) {
    $scope.resources = data.resource;
    $scope.filteredResources = angular.copy($scope.resources);
    $scope.updateResources();
  });

  $http.get('rest/ksc').success(function(data) {
    $scope.reports = data.kscReport;
    $scope.filteredReports = angular.copy($scope.reports);
    $scope.updateReports();
  });

  $scope.$watch('resourceFilter', function() {
    $scope.filteredResources = $filter('filter')($scope.resources, $scope.resourceFilter);
    $scope.updateResources();
  });

  $scope.$watch('reportFilter', function() {
    $scope.filteredReports = $filter('filter')($scope.reports, $scope.reportFilter);
    $scope.updateReports();
  });

}]);

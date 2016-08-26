/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-ksc', [
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(3000);
  growlProvider.globalPosition('bottom-center');
}])

.filter('startFrom', function() {
  return function(input, start) {
    start = +start; // convert it to integer
    if (input) {
      return input.length < start ? input : input.slice(start);
    }
    return [];
  };
})

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

  $scope.actionUrl = 'KSC/formProcMain.htm';

  $scope.reloadConfig = function() {
    if (confirm("Are you sure you want to do this?")) {
      $window.location.href = 'KSC/index.htm?reloadConfig=true';
    }
  };

  $scope.viewReport = function() {
    if ($scope.reportSelected == null) {
      growl.warning('Please select the report you want to view.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=View&report=' + $scope.reportSelected.id;
  };

  $scope.customizeReport = function() {
    if ($scope.reportSelected == null) {
      growl.warning('Please select the report you want to customize.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=Customize&report=' + $scope.reportSelected.id;
  };

  $scope.createReport = function() {
    $window.location.href = $scope.actionUrl + '?report_action=Create';
  };

  $scope.createReportFromExisting = function() {
    if ($scope.reportSelected == null) {
      growl.warning('Please select the report you want to copy.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=CreateFrom&report=' + $scope.reportSelected.id;
  };

  $scope.deleteReport = function() {
    if ($scope.reportSelected == null) {
      growl.warning('Please select the report you want to delete.');
      return;
    }
    $window.location.href = $scope.actionUrl + '?report_action=Delete&report=' + $scope.reportSelected.id;
  };

  $scope.selectReport = function(id) {
    $scope.reportSelected = id;
  };

  $scope.selectResource = function(resource) {
    $window.location.href = "KSC/customView.htm?type=node&report=" + resource.name;
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
    $scope.hasResources = data.resource.length > 0;
    $scope.resources = data.resource;
    $scope.filteredResources = $scope.resources;
    $scope.updateResources();
  });

  $http.get('rest/ksc').success(function(data) {
    $scope.hasResources = data.kscReport.length > 0;
    $scope.reports = data.kscReport;
    $scope.filteredReports = $scope.reports;
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

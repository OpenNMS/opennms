/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2017 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const templateUrl = require('./add-to-ksc.html');

angular.module('onms-ksc', [
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(5000);
  growlProvider.globalPosition('bottom-center');
}])

.controller('AddToKscModalInstanceCtrl', ['$scope', '$http', '$uibModalInstance', 'resourceLabel', 'graphTitle', function ($scope, $http, $uibModalInstance, resourceLabel, graphTitle) {

  $scope.resourceLabel = resourceLabel;
  $scope.graphTitle = graphTitle;
  $scope.kscReports = [];
  $scope.timespan = '1_day';
  $scope.timespans = [
    '1_hour',
    '2_hour',
    '4_hour',
    '6_hour',
    '8_hour',
    '12_hour',
    '1_day',
    '2_day',
    '7_day',
    '1_month',
    '3_month',
    '6_month',
    '1_year',
    'Today',
    'Yesterday',
    'Yesterday 9am-5pm',
    'Yesterday 5pm-10pm',
    'This Week',
    'Last Week',
    'This Month',
    'Last Month',
    'This Quarter',
    'Last Quarter',
    'This Year',
    'Last Year'
  ];

  $scope.ok = function () {
    $uibModalInstance.close({ report: $scope.kscReport, title: $scope.graphTitle, timespan: $scope.timespan });
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

  $http.get('rest/ksc').then(function(response) {
    $scope.kscReports = response.data.kscReport;
  });

}])

.controller('AddToKscCtrl', ['$scope', '$http', '$uibModal', 'growl', function($scope, $http, $uibModal, growl) {

  $scope.open = function(resourceId, resourceLabel, graphName, graphTitle) {
    $uibModal.open({
      animation: $scope.animationsEnabled,
      templateUrl: templateUrl,
      controller: 'AddToKscModalInstanceCtrl',
      resolve: {
        resourceLabel: function() { return resourceLabel; },
        graphTitle: function() { return graphTitle; }
      }
    }).result.then(function (data) {
      $scope.updateReport(data.report, resourceId, resourceLabel, graphName, data.title, data.timespan);
    });
  };

  $scope.updateReport = function(report, resourceId, resourceLabel, graphName, graphTitle, timespan) {
    var url = 'rest/ksc/' + report.id;
    $http.get(url).success(function (data) {
      var found = false;
      angular.forEach(data.kscGraph, function(r) {
        if (r.resourceId === resourceId && r.graphtype === graphName) {
          found = true;
        }
      });
      if (found) {
        growl.warning('The graph "' + graphTitle + '" for resource "' + resourceLabel + '" has been already added to "' + report.label + '"');
      } else {
        $http({
          url: url,
          method: 'PUT',
          params: {
            title: graphTitle,
            reportName: graphName,
            resourceId: resourceId,
            timespan: timespan
          }
        }).success(function() {
          growl.success('The graph "' + graphTitle + '" has been added to "' + report.label + '"');
        }).error(function(msg) {
          growl.error(msg);
        });
      }
    }).error(function(msg) {
      growl.error(msg);
    });
  };

}]);

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2017 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const templateUrl = require('./add-to-ksc.html');
require('lib/onms-http');

angular.module('onms-ksc', [
  'onms.http',
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
}])
.factory('flowsRestFactory', /* @ngInject */ function ($http, $q) {
  var resources = {};

  resources.getFlowGraphUrl = function(nodeId, ifIndex , start, end) {
      var deferred = $q.defer();
      $http({
        url: 'rest/flows/flowGraphUrl',
        method: 'GET',
        params: { exporterNode : nodeId,
                  ifIndex : ifIndex,
                  start : start,
                  end : end,
                  limit: 0 }
      }).success(function(data) {
        deferred.resolve(data);
      });
      return deferred.promise;
  };

  return resources;
})
.factory('graphSearchFactory', function ($rootScope, $filter) {
  var graphSearch = {};
  graphSearch.graphs = [];
  graphSearch.noMatchingGraphs = false;
  // Update search query and broadcast an update to controllers.
  graphSearch.updateSearchQuery = function(searchItem) {
      this.searchQuery = searchItem;
      this.updateGraphsWithSearchItem();
  };
   
  graphSearch.initialize = function(graphName, graphTitle) {
      graphSearch.graphs.push(graphName);
      graphSearch.graphs.push(graphTitle);
  }

  graphSearch.updateGraphsWithSearchItem = function() {
    $rootScope.$broadcast('handleSearchQuery');
    // Check if there is atleast one matching graph else update correponding controller.
    let matchingGraphs = $filter('filter')(graphSearch.graphs, graphSearch.searchQuery);
    if (!(matchingGraphs && matchingGraphs.length)) {
      graphSearch.noMatchingGraphs = true;
      $rootScope.$broadcast('handleNoMatchingGraphsFound');
    } else if(graphSearch.noMatchingGraphs){
      graphSearch.noMatchingGraphs = false;
      $rootScope.$broadcast('handleNoMatchingGraphsFound');
    }
  }

  return graphSearch;
})
.controller('graphSearchBoxCtrl', ['$scope', 'graphSearchFactory', function($scope, graphSearchFactory) {
 
  // Update search query in service.
  $scope.$watch('searchQuery', function () {
    if (!angular.isUndefined($scope.searchQuery)) {
      graphSearchFactory.updateSearchQuery($scope.searchQuery);
    }
  });
  
}])
.controller('checkFlowsCtrl', ['$scope', '$http', '$filter', 'flowsRestFactory', 'graphSearchFactory', function($scope, $http, $filter, flowsRestFactory ,
   graphSearchFactory) {

  $scope.flowCount = 0;
  $scope.flowsEnabled = false;
  $scope.hasFlows = false;
  $scope.nomatchingGraphs = false;
  $scope.flowGraphUrl = '';
  $scope.getFlowInfo = function(nodeId, ifIndex , start, end) {
    if (nodeId === 0 || ifIndex === 0) {
      return;
    }
    flowsRestFactory.getFlowGraphUrl(nodeId, ifIndex, start, end)
      .then(function (data) {
        $scope.flowGraphUrl = data.flowGraphUrl;
        $scope.flowCount = data.flowCount;
        if ($scope.flowGraphUrl) {
            $scope.flowsEnabled = true;
            if ( $scope.flowCount > 0) {
                     $scope.hasFlows = true;
            } else {
              $scope.flowGraphUrl = null;
            } 
        }
      });
  };
  // When graph search doesn't yield any results, set/reset noMatchingGraphs
  $scope.$on('handleNoMatchingGraphsFound', function () {
      $scope.nomatchingGraphs = graphSearchFactory.noMatchingGraphs;
  });
}])
.controller('graphSearchCtrl', ['$scope', '$timeout', '$filter', '$attrs', '$element', 'graphSearchFactory', function($scope, $timeout, $filter, $attrs, $element, graphSearchFactory) {

  let graphName = $attrs.graphname;
  let graphTitle = $attrs.graphtitle;
  // Update service with graphname and graphtitle.
  graphSearchFactory.initialize(graphName, graphTitle);
  $scope.enableGraph = true;
  // Handle search query update and enable graphs with matching search query.
  $scope.$on('handleSearchQuery', function () {
    let searchQuery = graphSearchFactory.searchQuery;
    // Filter on graphName or graphTitle.
    let matchingElements = $filter('filter')([graphName, graphTitle], searchQuery);
    if (matchingElements && matchingElements.length) {
      $scope.enableGraph = true;
      if(searchQuery) {
        // Send event for the specific div to check if they are in viewport.
        // Hack : Triggering event without adding it to timeout actually doesn't give rendering engine to finish re-arranging divs.
        // Sending with timeout(0) will prioritize rendering engine since it is already in event loop.
          $timeout(function () {
            angular.element($element).find('.graph-container').trigger('renderGraph');
          }, 0);
      }
    } else {
      $scope.enableGraph = false;
    }
  });
}]);

/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2022 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const templateUrl = require('./add-to-graph-collection.html');
require('lib/onms-http');
require('apps/onms-default-apps');

// The AngularJS helpers of the legacy graph results page (graph/results.htm): the
// "Add to Graph Collection" modal, the graph filter box and the flow graph links.
// The Vue resource graphs page has its own versions of all three.
angular.module('onms-graph-results', [
  'ngSanitize',
  'onms.http',
  'onms.default.apps',
  'ui.bootstrap',
  'angular-growl'
])

.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
}])
.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(5000);
  growlProvider.globalPosition('bottom-center');
}])
.controller('AddToGraphCollectionModalInstanceCtrl', ['$scope', '$http', '$uibModalInstance', 'resourceLabel', 'graphTitle', function ($scope, $http, $uibModalInstance, resourceLabel, graphTitle) {

  $scope.resourceLabel = resourceLabel;
  $scope.graphTitle = graphTitle;
  $scope.collections = [];
  $scope.timespan = '1_day';
  $scope.timespans = [];

  $scope.ok = function () {
    $uibModalInstance.close({ collection: $scope.collection, title: $scope.graphTitle, timespan: $scope.timespan });
  };

  $scope.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };

  $http.get('api/v2/graph-collections').then(function(response) {
    $scope.collections = response.data;
  });
  $http.get('api/v2/graph-collections/timespans').then(function(response) {
    $scope.timespans = response.data.map(function(timespan) { return timespan.id; });
  });

}])

// angular-growl shows its messages as HTML, whatever ngSanitize says, so anything a user
// named is escaped before it goes into one.
.factory('escapeHtml', function() {
  return function(value) {
    return String(value === undefined || value === null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  };
})

.controller('AddToGraphCollectionCtrl', ['$scope', '$http', '$uibModal', 'growl', 'escapeHtml', function($scope, $http, $uibModal, growl, escapeHtml) {

  $scope.open = function(resourceId, resourceLabel, graphName, graphTitle) {
    $uibModal.open({
      animation: $scope.animationsEnabled,
      templateUrl: templateUrl,
      controller: 'AddToGraphCollectionModalInstanceCtrl',
      resolve: {
        resourceLabel: function() { return resourceLabel; },
        graphTitle: function() { return graphTitle; }
      }
    }).result.then(function (data) {
      $scope.addGraph(data.collection, resourceId, resourceLabel, graphName, data.title, data.timespan);
    });
  };

  $scope.addGraph = function(collection, resourceId, resourceLabel, graphName, graphTitle, timespan) {
    const alreadyThere = (collection.graphs || []).some(function(graph) {
      return graph.resourceId === resourceId && graph.graphtype === graphName;
    });
    if (alreadyThere) {
      growl.warning('The graph "' + escapeHtml(graphTitle) + '" for resource "' + escapeHtml(resourceLabel) + '" has been already added to "' + escapeHtml(collection.title) + '"');
      return;
    }
    $http({
      url: 'api/v2/graph-collections/' + collection.id + '/graphs',
      method: 'POST',
      data: {
        kind: 'prefab',
        title: graphTitle,
        resourceId: resourceId,
        graphtype: graphName,
        timespan: timespan
      }
    }).then(function() {
      growl.success('The graph "' + escapeHtml(graphTitle) + '" has been added to "' + escapeHtml(collection.title) + '"');
    }, function(response) {
      growl.error(escapeHtml(response.data || 'The graph could not be added.'));
    });
  };
}])
.factory('flowsRestFactory', /* @ngInject */ function ($http, $q) {
  const resources = {};

  resources.getFlowGraphUrl = function(nodeId, ifIndex , start, end) {
      const deferred = $q.defer();
      $http({
        url: 'rest/flows/flowGraphUrl',
        method: 'GET',
        params: { exporterNode : nodeId,
                  ifIndex : ifIndex,
                  start : start,
                  end : end,
                  limit: 0 }
      }).then(function(response) {
        deferred.resolve(response.data);
      });
      return deferred.promise;
  };

  return resources;
})
.factory('graphSearchFactory', function ($rootScope, $filter) {
  const graphSearch = {};
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
    const matchingGraphs = $filter('filter')(graphSearch.graphs, graphSearch.searchQuery);
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

  const graphName = $attrs.graphname;
  const graphTitle = $attrs.graphtitle;
  // Update service with graphname and graphtitle.
  graphSearchFactory.initialize(graphName, graphTitle);
  $scope.enableGraph = true;
  // Handle search query update and enable graphs with matching search query.
  $scope.$on('handleSearchQuery', function () {
    const searchQuery = graphSearchFactory.searchQuery;
    // Filter on graphName or graphTitle.
    const matchingElements = $filter('filter')([graphName, graphTitle], searchQuery);
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

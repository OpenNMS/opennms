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
require('lib/onms-http');

const graphCollectionsTemplate = require('./template.graph-collections.html');
const nodesTemplate = require('./template.nodes.html');

import Util from 'lib/util';

// ngSanitize: the typeahead renders each match's label as HTML, sanitized only when
// $sanitize is injectable. Without it a collection title would run as markup.
angular.module('onms-search', [
  'ngSanitize',
  'onms.http',
  'ui.bootstrap'
])

.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
}])

.directive('onmsSearchNodes', function() {
  return {
    restrict: 'E',
    transclude: true,
    templateUrl: nodesTemplate,
    controller: 'NodeSearchCtrl'
  };
})

.directive('onmsSearchGraphCollections', function() {
  return {
    restrict: 'E',
    transclude: true,
    templateUrl: graphCollectionsTemplate,
    controller: 'GraphCollectionSearchCtrl'
  };
})

.controller('NodeSearchCtrl', ['$scope', '$window', '$http', function($scope, $window, $http) {

  $scope.getNodes = function(criteria) {
    return $http({
      url: 'rest/nodes',
      method: 'GET',
      params: { label: criteria, comparator: 'contains' }
    }).then(function(response) {
      return response.data.node;
    });
  };

  $scope.goToChooseResources = function(node) {
    $window.location.href = Util.getBaseHref() + 'graph/chooseresource.jsp?node=' + node.id;
  }

}])

.controller('GraphCollectionSearchCtrl', ['$scope', '$window', '$http', '$filter', function($scope, $window, $http, $filter) {

  $scope.getGraphCollections = function(criteria) {
    return $http({
      url: 'api/v2/graph-collections',
      method: 'GET'
    }).then(function(response) {
       return $filter('filter')(response.data, { title: criteria });
    });
  };

  $scope.goToGraphCollection = function(collection) {
    $window.location.href = Util.getBaseHref() + 'ui/index.html#/graph-collections/' + collection.id;
  }

}]);
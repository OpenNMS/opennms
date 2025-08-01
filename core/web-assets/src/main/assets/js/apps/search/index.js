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

const kscTemplate = require('./template.ksc.html');
const nodesTemplate = require('./template.nodes.html');

import Util from 'lib/util';

angular.module('onms-search', [
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

.directive('onmsSearchKsc', function() {
  return {
    restrict: 'E',
    transclude: true,
    templateUrl: kscTemplate,
    controller: 'KscSearchCtrl'
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

.controller('KscSearchCtrl', ['$scope', '$window', '$http', '$filter', function($scope, $window, $http, $filter) {

  $scope.getKscReports = function(criteria) {
    return $http({
      url: 'rest/ksc',
      method: 'GET'
    }).then(function(response) {
       return $filter('filter')(response.data.kscReport, criteria);
    });
  };

  $scope.goToKscReport = function(ksc) {
    $window.location.href = Util.getBaseHref() + 'KSC/customView.htm?type=custom&report=' + ksc.id;
  }

}]);
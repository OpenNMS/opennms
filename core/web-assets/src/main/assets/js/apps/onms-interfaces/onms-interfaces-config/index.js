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
require('apps/onms-default-apps');

const configTemplate = require('../templates/template.config.html');
const sortTemplate = require('../templates/template.sort.html');

angular.module('onms-interfaces-config', [
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

.filter('startFrom', function() {
  return function(input, start) {
    const s = parseInt(start, 10);
    if (input) {
      return input.length < s ? input : input.slice(s);
    }
    return [];
  };
})

.directive('onmsInterfacesConfig', function() {
  return {
    restrict: 'E',
    scope: {
      nodeId: '=node'
    },
    templateUrl: configTemplate,
    controller: 'NodeInterfacesConfigCtrl'
  };
})

.directive('sort', function() {
  return {
    restrict: 'A',
    transclude: true,
    templateUrl: sortTemplate,
    scope: {
      order: '=',
      by: '=',
      reverse : '='
    },
    link: function(scope, element, attrs) {
      scope.onClick = function () {
        if( scope.order === scope.by ) {
           scope.reverse = !scope.reverse 
        } else {
          scope.by = scope.order;
          scope.reverse = false; 
        }
      }
    }
  };
})

.controller('NodeInterfacesConfigCtrl', ['$scope', '$http', '$filter', 'growl', function($scope, $http, $filter, growl) {

  $scope.filters = { snmpInterface: null };
  $scope.snmpInterfaces = [];
  $scope.filteredSnmpInterfaces = [];
  $scope.snmpInterfacesPageSize = 10;
  $scope.snmpInterfacesMaxSize = 5;
  $scope.snmpInterfacesTotalItems = 0;
  $scope.snmpOrder = 'ifIndex';
  $scope.snmpReverse = false;

  $scope.loadSnmpInterfaces = function() {
    $http({
      url: 'rest/nodes/' + $scope.nodeId + '/snmpinterfaces',
      method: 'GET',
      params: { limit: 0 }
    }).then(function(response) {
      const data = response.data;
      $scope.snmpInterfaces = data.snmpInterface;
      $scope.setStylesForSnmpInterfaces();
      $scope.filteredSnmpInterfaces = $scope.snmpInterfaces;
      $scope.updateFilteredSnmpInterfaces();
    });
  };

  $scope.isCollectionEnabled = function(intf) {
    return intf.collectFlag === 'C' || intf.collectFlag === 'UC' || intf.collectFlag === 'PC';
  };

  $scope.updateCollection = function($event, intf) {
    $scope.enableCollection(intf, $event.target.checked);
  };

  $scope.enableCollection = function(intf, enable) {
    intf.collectFlag = enable ? 'UC' : 'UN';
    $http.put('rest/nodes/' + $scope.nodeId + '/snmpinterfaces/' + intf.ifIndex, 'collect=' + intf.collectFlag, {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    })
    .then(function() {
      var action = $scope.isCollectionEnabled(intf) ? 'enabled' : 'disabled';
      growl.success('Data collection flag was successfully ' + action + ' for interface ' + intf.ifName);
    }, function() {
      growl.error('Cannot set data collection flag for interface ' + intf.ifName);
    });
  };

  $scope.updateFilteredSnmpInterfaces = function() {
    $scope.snmpInterfacesCurrentPage = 1;
    $scope.snmpInterfacesTotalItems = $scope.filteredSnmpInterfaces.length;
    $scope.snmpInterfacesNumPages = Math.ceil($scope.snmpInterfacesTotalItems / $scope.snmpInterfacesPageSize);
  };

  $scope.setStylesForSnmpInterfaces = function() {
    angular.forEach($scope.snmpInterfaces, function(intf) {
      var cssClass = undefined;
      if (intf.ifAdminStatus !== 1) {
        cssClass = 'onms-interface-status-unknown';
      } else {
        if (intf.ifOperStatus === 1) {
          cssClass = 'onms-interface-status-up';
        } else {
          cssClass = 'onms-interface-status-down';
        }
      }
      intf.backgroundClass = cssClass;
    });
  };

  $scope.$watch('filters.snmpInterface', function() {
    $scope.filteredSnmpInterfaces = $filter('filter')($scope.snmpInterfaces, $scope.filters.snmpInterface);
    $scope.updateFilteredSnmpInterfaces();
  });

  $scope.loadSnmpInterfaces();

}]);

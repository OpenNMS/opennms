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

const mainTemplate = require('../templates/template.main.html');
const sortTemplate = require('../templates/template.sort.html');

require('./styles.css');

angular.module('onms-interfaces', [
  'onms.http',
  'onms.default.apps',
  'ui.bootstrap'
])

.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
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

.directive('onmsInterfaces', function() {
  return {
    restrict: 'E',
    scope: {
      nodeId: '=node'
    },
    templateUrl: mainTemplate,
    controller: 'NodeInterfacesCtrl'
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

.controller('NodeInterfacesCtrl', ['$scope', '$http', '$filter', '$window', function($scope, $http, $filter, $window) {

  // Common Variables

  $scope.filters = { ipInterface: null, snmpInterface: null };

  // IP Interfaces Variables

  $scope.ipInterfaces = [];
  $scope.filteredIpInterfaces = [];
  $scope.ipInterfacesPageSize = 10;
  $scope.ipInterfacesMaxSize = 5;
  $scope.ipInterfacesTotalItems = 0;
  $scope.ipOrder = 'ipAddress';
  $scope.ipReverse = false;

  // SNMP Interfaces Variables

  $scope.snmpInterfaces = [];
  $scope.filteredSnmpInterfaces = [];
  $scope.snmpInterfacesPageSize = 10;
  $scope.snmpInterfacesMaxSize = 5;
  $scope.snmpInterfacesTotalItems = 0;
  $scope.snmpOrder = 'ifIndex';
  $scope.snmpReverse = false;
  $scope.ipIfLoaded = false;
  $scope.snmpIfLoaded = false;

  // IP Interfaces

  $scope.loadIpInterfaces = function() {
    $http({
      url: 'rest/nodes/' + $scope.nodeId + '/ipinterfaces',
      method: 'GET',
      params: { limit: 0 }
    }).then(function succeeded(response) {
      $scope.ipIfLoaded = true;
      $scope.ipInterfaces = response.data.ipInterface;
      $scope.setStylesForIpInterfaces();
      $scope.filteredIpInterfaces = $scope.ipInterfaces;
      $scope.updateFilteredIpInterfaces();
    }, function errorCallback(response) {
      $scope.ipIfLoaded = true;
    });
  };

  $scope.updateFilteredIpInterfaces = function() {
    $scope.ipInterfacesCurrentPage = 1;
    $scope.ipInterfacesTotalItems = $scope.filteredIpInterfaces.length;
    $scope.ipInterfacesNumPages = Math.ceil($scope.ipInterfacesTotalItems / $scope.ipInterfacesPageSize);
  };

  $scope.setStylesForIpInterfaces = function() {
    angular.forEach($scope.ipInterfaces, function(intf) {
      var cssClass = undefined;
      if (intf.isManaged === 'U' || intf.isManaged === 'F' || intf.isManaged === 'N' || intf.monitoredServiceCount < 1) {
        cssClass = 'onms-interface-status-unknown';
      } else {
        cssClass = intf.isDown ? 'onms-interface-status-down': 'onms-interface-status-up';
      }
      intf.backgroundClass = cssClass;
    });
  };

  $scope.openIpPage = function(intf) {
    $window.location.href = getBaseHref() + 'element/interface.jsp?ipinterfaceid=' + intf.id;
  };

  $scope.$watch('filters.ipInterface', function() {
    $scope.filteredIpInterfaces = $filter('filter')($scope.ipInterfaces, $scope.filters.ipInterface);
    $scope.updateFilteredIpInterfaces();
  });

  // SNMP Interfaces

  $scope.loadSnmpInterfaces = function() {
    $http({
      url: 'rest/nodes/' + $scope.nodeId + '/snmpinterfaces',
      method: 'GET',
      params: { limit: 0 }
    }).then(function succeeded(response) {
      $scope.snmpIfLoaded = true;
      $scope.snmpInterfaces = response.data.snmpInterface;
      $scope.setStylesForSnmpInterfaces();
      $scope.updateFlowUrlsForSnmpInterfaces();
      $scope.filteredSnmpInterfaces = $scope.snmpInterfaces;
      $scope.updateFilteredSnmpInterfaces();
    }, function errorCallback(response) {
      $scope.snmpIfLoaded = true;
    });
  };

  $scope.updateFilteredSnmpInterfaces = function() {
    $scope.snmpInterfacesCurrentPage = 1;
    $scope.snmpInterfacesTotalItems = $scope.filteredSnmpInterfaces.length;
    $scope.snmpInterfacesNumPages = Math.ceil($scope.snmpInterfacesTotalItems / $scope.snmpInterfacesPageSize);
  };

  $scope.updateFlowUrlsForSnmpInterfaces = function() {
    angular.forEach($scope.snmpInterfaces, function(intf) {
      if (!intf.hasIngressFlows && !intf.hasEgressFlows) {
        // No flows - nothing to do
        return;
      }

      $http({
        url: 'rest/flows/flowGraphUrl',
        method: 'GET',
        params: {
          exporterNode: $scope.nodeId,
          ifIndex: intf.ifIndex
        }
      }).then(function succeeded(response) {
        // Update the flowGraphUrl on the associated interface
        intf.flowGraphUrl = response.data.flowGraphUrl;
      }, function errorCallback(response) {
        // pass
      });
    });
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

  $scope.openSnmpPage = function(intf) {
    $window.location.href = getBaseHref() + 'element/snmpinterface.jsp?node=' + $scope.nodeId + '&ifindex=' + intf.ifIndex;
  };

  $scope.$watch('filters.snmpInterface', function() {
    $scope.filteredSnmpInterfaces = $filter('filter')($scope.snmpInterfaces, $scope.filters.snmpInterface);
    $scope.updateFilteredSnmpInterfaces();
  });

  // Initialize content
  $scope.loadIpInterfaces();
  $scope.loadSnmpInterfaces();

}]);
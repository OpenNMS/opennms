/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');

const mainTemplate = require('../templates/template.main.html');
const sortTemplate = require('../templates/template.sort.html');

require('./styles.css');

angular.module('onms-interfaces', [
  'ui.bootstrap'
])

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

  // IP Interfaces

  $scope.loadIpInterfaces = function() {
    $http({
      url: 'rest/nodes/' + $scope.nodeId + '/ipinterfaces',
      method: 'GET',
      params: { limit: 0 }
    }).success(function(data) {
      $scope.ipInterfaces = data.ipInterface;
      $scope.setStylesForIpInterfaces();
      $scope.filteredIpInterfaces = $scope.ipInterfaces;
      $scope.updateFilteredIpInterfaces();
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
    }).success(function(data) {
      $scope.snmpInterfaces = data.snmpInterface;
      $scope.setStylesForSnmpInterfaces();
      $scope.filteredSnmpInterfaces = $scope.snmpInterfaces;
      $scope.updateFilteredSnmpInterfaces();
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

// Bootstrap to a an element with ID 'onms-interfaces'

angular.element(document).ready(function () {
  var el = document.getElementById('onms-interfaces');
  angular.bootstrap(angular.element(el), ['onms-interfaces']);
});

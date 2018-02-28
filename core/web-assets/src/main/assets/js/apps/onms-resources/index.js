/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const _ = require('underscore');

angular.module('onms-resources', [
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(3000);
  growlProvider.globalPosition('bottom-center');
}])

.filter('startFrom', function() {
  return function(input, _start) {
    const start = Number(_start);
    if (input) {
      return input.length < start ? input : input.slice(start);
    }
    return [];
  };
})

.controller('NodeListCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.endUrl = 'graph/results.htm';
  $scope.resources = [];
  $scope.filteredResources = [];
  $scope.pageSize = 10;
  $scope.maxSize = 5;
  $scope.totalItems = 0;
  $scope.hasResources = false;

  $scope.goTo = function(id) {
    $window.location.href = getBaseHref() + 'graph/chooseresource.jsp?reports=all&parentResourceId=' + id + '&endUrl=' + $scope.endUrl;
  };

  $scope.update = function() {
    $scope.currentPage = 1;
    $scope.totalItems = $scope.filteredResources.length;
    $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
  };

  $http.get('rest/resources?depth=0').success(function(data) {
    $scope.hasResources = data.resource.length > 0;
    $scope.resources = data.resource;
    $scope.filteredResources = $scope.resources;
    $scope.update();
  });

  $scope.$watch('resourceFilter', function() {
    $scope.filteredResources = $filter('filter')($scope.resources, $scope.resourceFilter);
    $scope.update();
  });

}])

.controller('NodeResourcesCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.searchQuery = undefined;
  $scope.resources = {};
  $scope.hasResources = false;
  $scope.filteredResources = {};
  $scope.isCollapsed = {};
  $scope.nodeLink = undefined;
  $scope.nodeLabel = undefined;
  $scope.url = 'graph/results.htm';
  $scope.reports = 'all';

  $scope.init = function(nodeCriteria, reports, endUrl) {
    if (nodeCriteria === null || nodeCriteria === '') {
      return;
    }
    if (reports !== null && reports !== '') {
      $scope.reports = reports;
    }
    if (endUrl !== null && endUrl !== '') {
      $scope.url = endUrl;
    }
    $http.get('rest/resources/fornode/'+nodeCriteria).success(function(data) {
      $scope.nodeLink = data.link;
      $scope.nodeLabel = data.label;
      $scope.hasResources = data.children.resource.length > 0;
      var reduced = _.map(data.children.resource, function(obj) {
        return { id: obj.id, label: obj.label, typeLabel: obj.typeLabel, checked: false };
      });
      $scope.resources = _.groupBy(_.sortBy(reduced, function(r) {
        var type = r['typeLabel'];
        return (type === 'SNMP Node Data' || type === 'SNMP Interface Data') ? Infinity : type; 
      }), 'typeLabel');
      angular.copy($scope.resources, $scope.filteredResources);
    });
  };

  $scope.checkAll = function(check) {
    for (var key in $scope.resources) {
      if ($scope.resources.hasOwnProperty(key)) {
        _.each($scope.filteredResources[key], function(r) {
          r.selected = check;
        });
      }
    }
  };

  $scope.graphSelected = function() {
    var selected = [];
    for (var key in $scope.resources) {
      if ($scope.resources.hasOwnProperty(key)) {
        _.each($scope.filteredResources[key], function(r) {
          if (r.selected) {
            selected.push('resourceId=' + r.id);
          }
        });
      }
    }
    $scope.doGraph(selected);
  };

  $scope.graphAll = function() {
    var selected = [];
    for (var key in $scope.filteredResources) {
      if ($scope.filteredResources.hasOwnProperty(key)) {
        _.each($scope.filteredResources[key], function(r) {
          selected.push('resourceId=' + r.id);
        });
      }
    }
    $scope.doGraph(selected);
  };

  $scope.doGraph = function(selected) {
    if (selected.length > 0) {
      $window.location.href = getBaseHref() + $scope.url + '?' + selected.join('&') + ($scope.reports ? '&reports=' + $scope.reports : '');
    } else {
      growl.error('Please select at least one resource.');
    }
  };

  $scope.$watch('searchQuery', function() {
    $scope.filteredResurces = {};
    for (var key in $scope.resources) {
      if ($scope.resources.hasOwnProperty(key)) {
        $scope.filteredResources[key] = $filter('filter')($scope.resources[key], $scope.searchQuery);
      }
    }
  });

}]);

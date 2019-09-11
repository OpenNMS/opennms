/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-resources', [
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

.controller('NodeListCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.endUrl = 'graph/results.htm';
  $scope.resources = [];
  $scope.filteredResources = [];
  $scope.pageSize = 10;
  $scope.maxSize = 5;
  $scope.totalItems = 0;
  $scope.hasResources = false;
  $scope.loaded = false;

  $scope.goTo = function(id) {
    $window.location.href = getBaseHref() + 'graph/chooseresource.jsp?reports=all&parentResourceId=' + id + '&endUrl=' + $scope.endUrl;
  };

  $scope.update = function() {
    $scope.currentPage = 1;
    $scope.totalItems = $scope.filteredResources.length;
    $scope.numPages = Math.ceil($scope.totalItems / $scope.pageSize);
  };

  $http.get('rest/resources?depth=0').then(function succeeded(response) {
    var data = response.data;
    $scope.loaded = true;
    $scope.hasResources = data.resource.length > 0;
    $scope.resources = data.resource;
    $scope.filteredResources = $scope.resources;
    $scope.update();
  }, function errorCallback(response) {
     $scope.loaded = true;
     growl.error("There was a problem in retrieving resources through ReST", {ttl: 10000});
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
  $scope.nodeCriteria = undefined;
  $scope.url = 'graph/results.htm';
  $scope.reports = 'all';
  $scope.loaded = false;

  $scope.init = function(nodeCriteria, reports, endUrl) {
    if (!nodeCriteria) {
      return;
    }
    // Update node criteria in scope.
    $scope.nodeCriteria = nodeCriteria;
    
    if (reports) {
      $scope.reports = reports;
    }
    if (endUrl) {
      $scope.url = endUrl;
    }

    $http.get('rest/resources/fornode/'+nodeCriteria).then(function succeeded(response) {
      var data = response.data;
      $scope.nodeLink = data.link;
      $scope.nodeLabel = data.label;
      $scope.loaded = true;
      $scope.hasResources = data.children.resource.length > 0;
      var reduced = _.map(data.children.resource, function(obj) {
        return { id: obj.id, label: obj.label, typeLabel: obj.typeLabel, checked: false };
      });
      $scope.resources = _.groupBy(_.sortBy(reduced, function(r) {
        var type = r['typeLabel'];
        return (type === 'SNMP Node Data' || type === 'SNMP Interface Data') ? Infinity : type;
      }), 'typeLabel');
      angular.copy($scope.resources, $scope.filteredResources);
    }, function errorCallback(response) {
       $scope.loaded = true;
       growl.error("There was a problem in retrieving resources through ReST", {ttl: 10000});
    });
  };

  $scope.checkAll = function(check) {
    for (var key in $scope.resources) {
      _.each($scope.filteredResources[key], function(r) {
        r.selected = check;
      });
    }
  };

  $scope.graphSelected = function() {
    var selected = [];
    for (var key in $scope.resources) {
      _.each($scope.filteredResources[key], function(r) {
        if (r.selected) {
          selected.push('resourceId=' + r.id);
        }
      });
    }
    $scope.doGraph(selected);
  };

  $scope.graphAll = function() {
    // Graph All will render all graphs for specific node. Controller will fetch specific resources.
    if ($scope.nodeCriteria) {
      $window.location.href = getBaseHref() + $scope.url + '?nodeCriteria=' + $scope.nodeCriteria + ($scope.reports ? '&reports=' + $scope.reports : '');
    } else {
      growl.error('Invalid node.');
    }
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
      $scope.filteredResources[key] = $filter('filter')($scope.resources[key], $scope.searchQuery);
    }
  });

}]);

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const _ = require('underscore');
require('lib/onms-http');

angular.module('onms-resources', [
  'onms.http',
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
     growl.error('There was a problem in retrieving resources through ReST', {ttl: 10000});
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
  $scope.generatedId = '';

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
      var reduced = _.map(data.children.resource, function (obj) {
        var resource = {
          id: obj.id,
          label: obj.label,
          typeLabel: obj.typeLabel,
          checked: false,
          ifIndex: parseInt(obj.externalValueAttributes.ifIndex, 10), // will return NaN if not set
          hasFlows: typeof obj.externalValueAttributes.hasFlows === 'undefined' ? false : JSON.parse(obj.externalValueAttributes.hasFlows)
        };
        $scope.updateFlowUrlForResource(nodeCriteria, resource);
        return resource;
      });
      $scope.resources = _.groupBy(_.sortBy(reduced, function(r) {
        var type = r['typeLabel'];
        return (type === 'SNMP Node Data' || type === 'SNMP Interface Data') ? Infinity : type;
      }), 'typeLabel');
      // Perform a shallow copy of the resource map - the resources may be updated asynchronously
      // with additional attributes
      $scope.filteredResources = {};
      for (var k in $scope.resources) {
        if (Object.prototype.hasOwnProperty.call($scope.resources, k)) {
          $scope.filteredResources[k] = $scope.resources[k];
        }
      }
    }, function errorCallback(response) {
       $scope.loaded = true;
       growl.error('There was a problem in retrieving resources through ReST', {ttl: 10000});
    });
  };

  $scope.updateFlowUrlForResource = function(nodeCriteria, resource) {
    if (!resource.hasFlows || isNaN(resource.ifIndex)) {
      // No flows, or not an interface, nothing to do
      return;
    }

    $http({
      url: 'rest/flows/flowGraphUrl',
      method: 'GET',
      params: {
        exporterNode: nodeCriteria,
        ifIndex: resource.ifIndex
      }
      }).then(function succeeded(response) {
        // Update the flowGraphUrl on the associated resource
        resource.flowGraphUrl = response.data.flowGraphUrl;
      }, function errorCallback(response) {
        // pass
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
            selected.push(r.id);
          }
        });
      }
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

  $scope.doGraph = function (selected) {
    // Save resources with an ID and form url with generatedId.
    if (selected.length > 0) {
      $http.post('rest/resources/generateId', selected)
        .success(function (response) {
          $scope.generatedId = response;
          if ($scope.generatedId) {
            $window.location.href = getBaseHref() + $scope.url + '?generatedId=' + $scope.generatedId + ($scope.reports ? '&reports=' + $scope.reports : '');
          } else {
            $scope.setResourceIds(selected);
          }
        }).error(function (error, status) {
          $scope.setResourceIds(selected);
        });
    } else {
      growl.error('Please select at least one resource.');
    }
  };

  $scope.setResourceIds = function (selected) {
    for (var i = 0; i < selected.length; i++) {
      selected[i] = 'resourceId=' + selected[i];
    }
    $window.location.href = getBaseHref() + $scope.url + '?' + selected.join('&') + ($scope.reports ? '&reports=' + $scope.reports : '');
  };

  $scope.$watch('searchQuery', function() {
    $scope.filteredResources = {};
    for (var key in $scope.resources) {
      if ($scope.resources.hasOwnProperty(key)) {
        $scope.filteredResources[key] = $filter('filter')($scope.resources[key], $scope.searchQuery);
      }
    }
  });

}]);

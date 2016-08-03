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

.controller('NodeResourcesCtrl', ['$scope', '$filter', '$http', '$window', 'growl', function($scope, $filter, $http, $window, growl) {

  $scope.searchQuery = undefined;
  $scope.resources = {};
  $scope.hasResources = false;
  $scope.filteredResources = {};
  $scope.isCollapsed = {};
  $scope.nodeLink = undefined;
  $scope.nodeLabel = undefined;
  $scope.url = '/opennms/graph/results.htm?reports=all';

  $scope.init = function(criteria) {
    $http.get('/opennms/rest/resources/fornode/'+criteria).success(function(data) {
      $scope.nodeLink = data.link;
      $scope.nodeLabel = data.label;
      $scope.hasResources = data.children.resource.length > 0;
      var reduced = _.map(data.children.resource, function(obj) {
        return { id: obj.id, label: obj.label, typeLabel: obj.typeLabel, checked: false };
      });
      $scope.resources = _.groupBy(_.sortBy(reduced, 'typeLabel'), 'typeLabel');
      angular.copy($scope.resources, $scope.filteredResources);
    });
  };

  $scope.checkAll = function(check) {
    for (var key in $scope.resources) {
      _.each($scope.resources[key], function(r) {
        r.selected = check;
      });
    }
  };

  $scope.graphSelected = function() {
    var selected = [];
    for (var key in $scope.resources) {
      _.each($scope.resources[key], function(r) {
        if (r.selected) {
          selected.push('resourceId=' + r.id);
        }
      });
    }
    $scope.doGraph(selected);
  };

  $scope.graphAll = function() {
    var selected = [];
    for (var key in $scope.resources) {
      selected.concat(_.map($scope.resources[key], function(r) {
        return 'resourceId=' + r.id;
      }));
    }
    $scope.doGraph(selected);
  };

  $scope.doGraph = function(selected) {
    if (selected.length > 0) {
      $window.location.href = $scope.url + '&' + selected.join('&');
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

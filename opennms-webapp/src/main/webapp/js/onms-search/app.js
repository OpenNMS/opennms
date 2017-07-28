/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-search', [
  'ui.bootstrap'
])

.directive('onmsSearchNodes', function() {
  return {
    restrict: 'E',
    transclude: true,
    templateUrl: 'js/onms-search/template.nodes.html',
    controller: 'NodeSearchCtrl'
  };
})

.directive('onmsSearchKsc', function() {
  return {
    restrict: 'E',
    transclude: true,
    templateUrl: 'js/onms-search/template.ksc.html',
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
    $window.location.href = getBaseHref() + 'graph/chooseresource.jsp?node=' + node.id;
  }

}])

.controller('KscSearchCtrl', ['$scope', '$window', '$http', function($scope, $window, $http) {

  $scope.getKscReports = function(criteria) {
    return $http({
      url: 'rest/ksc',
      method: 'GET',
      params: { label: criteria, comparator: 'contains' }
    }).then(function(response) {
      return response.data.kscReport;
    });
  };

  $scope.goToKscReport = function(ksc) {
    $window.location.href = getBaseHref() + 'KSC/customView.htm?type=custom&report=' + ksc.id;
  }

}]);

// Bootstrap to a an element with ID 'onms-search'

angular.element(document).ready(function () {
  var el = document.getElementById('onms-search');
  angular.bootstrap(angular.element(el), ['onms-search']);
});

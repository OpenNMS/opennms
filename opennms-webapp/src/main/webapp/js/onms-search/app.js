/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-search', [
  'ui.bootstrap'
])

.controller('SearchCtrl', ['$scope', '$window', '$http', function($scope, $window, $http) {

  $scope.getNodes = function(criteria) {
    return $http({
      url: 'rest/nodes',
      method: 'GET',
      params: { label: criteria, comparator: 'contains' }
    }).then(function(response) {
      return response.data.node;
    });
  };

  $scope.getKscReports = function(criteria) {
    return $http({
      url: 'rest/ksc',
      method: 'GET',
      params: { label: criteria, comparator: 'contains' }
    }).then(function(response) {
      return response.data.kscReport;
    });
  };

  $scope.goToChooseResources = function(node) {
    $window.location.href = 'graph/chooseresource.jsp?node=' + node.id;
  }

  $scope.goToKscReport = function(ksc) {
    $window.location.href = 'KSC/customView.htm?type=custom&report=' + ksc.id;
  }

}]);

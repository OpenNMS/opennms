/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2017 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const kscTemplate = require('./template.ksc.html');
const nodesTemplate = require('./template.nodes.html');

import Util from 'lib/util';

angular.module('onms-search', [
  'ui.bootstrap'
])

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

.controller('KscSearchCtrl', ['$scope', '$window', '$http', function($scope, $window, $http) {

  $scope.getKscReports = function(criteria) {
    return $http({
      url: 'rest/ksc',
      method: 'GET'
    }).then(function(response) {
        return response.data.kscReport.filter(function(report) {
            return report.label.indexOf(criteria) !== -1
        });
    });
  };

  $scope.goToKscReport = function(ksc) {
    $window.location.href = Util.getBaseHref() + 'KSC/customView.htm?type=custom&report=' + ksc.id;
  }

}]);

// Bootstrap to a an element with ID 'onms-search'

angular.element(document).ready(function () {
  var el = document.getElementById('onms-search');
  angular.bootstrap(angular.element(el), ['onms-search']);
});

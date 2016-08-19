/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

angular.module('onms-assets', [
  'ui.bootstrap',
  'angular-growl'
])

.config(['growlProvider', function(growlProvider) {
  growlProvider.globalTimeToLive(3000);
  growlProvider.globalPosition('bottom-center');
}])

.config(['$uibTooltipProvider', function($uibTooltipProvider) {
  $uibTooltipProvider.setTriggers({
    'mouseenter': 'mouseleave'
  });
  $uibTooltipProvider.options({
    'placement': 'right',
    'trigger': 'mouseenter'
  });
}])

.directive('assetField', function() {
  return {
    restrict: 'E',
    templateUrl: 'js/onms-assets/template.html',
    scope: {
      field: '=',
      asset: '='
    }
  };
})

.controller('NodeAssetsCtrl', ['$scope', '$http', 'growl', function($scope, $http, growl) {

  $scope.infoKeys = [ 'sysObjectId', 'sysName', 'sysLocation', 'sysContact', 'sysDescription' ];
  $scope.config = {};
  $scope.asset = {};
  $scope.nodeId;
  $scope.nodeLabel;
  $scope.foreignSource;
  $scope.foreignId;

  $scope.init = function(nodeId) {
    $scope.nodeId = nodeId;
    $http.get('js/onms-assets/config.json')
      .success(function(config) {
        $scope.config = config;
        $http.get('rest/nodes/' + $scope.nodeId)
          .success(function(node) {
            $scope.nodeLabel = node.label;
            $scope.foreignSource = node.foreignSource;
            $scope.foreignId = node.foreignId;
            $scope.asset = angular.copy(node.assetRecord);
            angular.forEach($scope.infoKeys, function(k) {
              $scope.asset[k] = node[k];
            });
          })
          .error(function(msg) {
            growl.error(msg);
          });
      })
      .error(function(msg) {
        growl.error(msg);
      });
  };

  $scope.save = function() {
    var target = {};
    for (var k in $scope.asset) {
      if ($scope.infoKeys.indexOf(k) == -1 && $scope.asset[k] != '' && $scope.asset[k] != null) {
        target[k] = $scope.asset[k];
      }
    }
    console.log(target);
    $http({
      method: 'PUT',
      url: 'rest/nodes/' + $scope.nodeId + '/assetRecord',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      data: $.param(target)
    }).success(function() {
      // FIXME Update the requisition if foreignSource is not null
      growl.success('The asset record has been successfully updated.');
    }).error(function(msg) {
      growl.error('Cannot update the asset record: ' + msg);
    });
  };

}]);

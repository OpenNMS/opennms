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
    },
    link: function (scope, element, attrs) {
      scope.suggestions = scope.$parent.suggestions;
      scope.getSuggestions = function(field) {
        if (scope.suggestions[field]) {
          return scope.suggestions[field].suggestion;
        }
        return [];
      };
    }
  };
})

.controller('NodeAssetsCtrl', ['$scope', '$http', '$q', 'growl', function($scope, $http, $q, growl) {

  $scope.infoKeys = [ 'sysObjectId', 'sysName', 'sysLocation', 'sysContact', 'sysDescription' ];
  $scope.config = {};
  $scope.asset = {};
  $scope.suggestions = {};
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
    $http.get('rest/assets/suggestions')
      .success(function(suggestions) {
        $scope.suggestions = suggestions
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
      growl.success('The asset record has been successfully updated.');
      $scope.updateRequisition(target);
    }).error(function(msg) {
      growl.error('Cannot update the asset record: ' + msg);
    });
  };

  $scope.updateRequisition = function(assets) {
    if ($scope.foreignSource && $scope.foreignId) {
      bootbox.confirm('This node belongs to the requisition ' + $scope.foreignSource + '.<br/> It is recommended to update the requisition with your asset fields.<br/> Do you want to do that ?', function(ok) {
        if (ok) {
          var promises = [];
          for (var key in assets) {
            var field = { name: key, value: assets[key] };
            promises.push($http.post('rest/requisitions/' + $scope.foreignSource + '/nodes/' + $scope.foreignId + '/assets', field)); 
          }
          $q.all(promises).then(function() {
            growl.success('Requisition ' + $scope.foreignSource + ' has been updated.');
          }, function(error, status) {
            growl.success('Error while updating requisition ' + $scope.foreignSource);
          });
        }
      });
    }
  };

}]);

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const bootbox = require('vendor/bootbox-js');
const defaultConfig = require('./config.json');

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

.controller('NodeAssetsCtrl', ['$scope', '$http', '$q', 'growl', 'uibDateParser', function($scope, $http, $q, growl, uibDateParser) {

  $scope.blackList = [ 'id', 'lastModifiedDate', 'lastModifiedBy', 'lastCapsdPoll', 'createTime' ];
  $scope.infoKeys = [ 'sysObjectId', 'sysName', 'sysLocation', 'sysContact', 'sysDescription' ];
  $scope.dateKeys = [ 'dateInstalled', 'leaseExpires', 'maintContractExpiration' ];

  $scope.dateFormat = 'yyyy-MM-dd';

  $scope.config = {};
  $scope.master = {};
  $scope.asset = {};
  $scope.suggestions = {};
  /*
  $scope.nodeId;
  $scope.nodeLabel;
  $scope.foreignSource;
  $scope.foreignId;
  */

  $scope.init = function(nodeId) {
    $scope.nodeId = nodeId;
    $scope.config = defaultConfig;
    $http.get('rest/nodes/' + $scope.nodeId)
      .success(function(node) {
        $scope.nodeLabel = node.label;
        $scope.foreignSource = node.foreignSource;
        $scope.foreignId = node.foreignId;
        angular.forEach($scope.dateKeys, function(key) {
          node.assetRecord[key] = uibDateParser.parse(node.assetRecord[key], $scope.dateFormat);
        });
        $scope.master = angular.copy(node.assetRecord);
        $scope.asset = angular.copy(node.assetRecord);
        angular.forEach($scope.infoKeys, function(k) {
          $scope.asset[k] = node[k];
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

  $scope.getSuggestions = function(field) {
    if ($scope.suggestions[field]) {
      return $scope.suggestions[field].suggestion;
    }
    return [];
  };

  $scope.reset = function() {
    $scope.asset = angular.copy($scope.master);
    $scope.assetForm.$setPristine();
  };

  $scope.save = function() {
    var target = {};
    for (var k in $scope.asset) {
      if ($scope.infoKeys.indexOf(k) === -1 && $scope.blackList.indexOf(k) === -1 && $scope.asset[k] !== '' && $scope.asset[k] !== null) {
        target[k] = $scope.dateKeys.indexOf(k) === -1 ? $scope.asset[k] : uibDateParser.filter($scope.asset[k], $scope.dateFormat);
      }
    }
    //console.log('Assets to save: ' + angular.toJson(target));
    $http({
      method: 'PUT',
      url: 'rest/nodes/' + $scope.nodeId + '/assetRecord',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      data: $.param(target)
    }).success(function() {
      growl.success('The asset record has been successfully updated.');
      $scope.checkRequisition(target);
    }).error(function(msg) {
      growl.error('Cannot update the asset record: ' + msg);
    });
  };

  $scope.checkRequisition = function(assets) {
    if ($scope.foreignSource && $scope.foreignId) {
      bootbox.confirm('This node belongs to the requisition ' + $scope.foreignSource + '.<br/> It is recommended to update the requisition with your asset fields, but all the existing fields will be overriden.<br/> Do you want to do that ?', function(ok) {
        if (ok) {
          $scope.updateRequisition(assets);
        }
      });
    }
  };

  $scope.updateRequisition = function(assets) {
    var assetFields = [];
    for (var key in assets) {
      if (assets.hasOwnProperty(key)) {
        assetFields.push({ name: key, value: assets[key] });
      }
    }
    $http.get('rest/requisitions/' + $scope.foreignSource + '/nodes/' + $scope.foreignId)
      .success(function(node) {
        node.asset = assetFields;
        $http.post('rest/requisitions/' + $scope.foreignSource + '/nodes', node)
          .success(function() {
            growl.success('Requisition ' + $scope.foreignSource + ' has been updated.');
          })
          .error(function() {
            growl.error('Cannot update requisition ' + $scope.foreignSource);
          });
      })
      .error(function() {
        growl.error('Cannot obtain node data from requisition ' + $scope.foreignSource);
      });
  };

}]);

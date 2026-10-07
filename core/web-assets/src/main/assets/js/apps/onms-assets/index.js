/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2016-2022 The OpenNMS Group, Inc.
*/

'use strict';

const angular = require('vendor/angular-js');
const bootbox = require('vendor/bootbox-js');
require('lib/onms-http');
require('../onms-date-formatter');
require('../onms-default-apps');

const defaultConfig = require('./config.json');

angular.module('onms-assets', [
  'onms.http',
  'onms.default.apps',
  'ui.bootstrap',
  'angular-growl',
  'onmsDateFormatter'
])

.config(['$locationProvider', function($locationProvider) {
  $locationProvider.hashPrefix('');
}])

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
  $scope.writeOnlyKeys = [ 'password', 'enable', 'snmpcommunity' ];
  // Write-only fields the user marked to clear. An empty input keeps the stored value.
  $scope.cleared = {};
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
      .then(function(response) {
        const node = response.data;
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
      }, function(response) {
        growl.error(response.data);
      });
    $http.get('rest/assets/suggestions')
      .then(function(response) {
        $scope.suggestions = response.data
      }, function(response) {
        growl.error(response.data);
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
    $scope.cleared = {};
    $scope.assetForm.$setPristine();
  };

  $scope.save = function() {
    var target = {};
    for (var k in $scope.asset) {
      if ($scope.infoKeys.indexOf(k) === -1 && $scope.blackList.indexOf(k) === -1 && $scope.asset[k] !== '' && $scope.asset[k] !== null) {
        target[k] = $scope.dateKeys.indexOf(k) === -1 ? $scope.asset[k] : uibDateParser.filter($scope.asset[k], $scope.dateFormat);
      }
    }
    // An empty value clears a write-only field in the asset record.
    angular.forEach($scope.writeOnlyKeys, function(k) {
      if ($scope.cleared[k]) {
        target[k] = '';
      }
    });
    //console.log('Assets to save: ' + angular.toJson(target));
    $http({
      method: 'PUT',
      url: 'rest/nodes/' + $scope.nodeId + '/assetRecord',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      data: $.param(target)
    }).then(function() {
      growl.success('The asset record has been successfully updated.');
      $scope.checkRequisition(target);
      // Do not keep typed secrets in the form after the save.
      angular.forEach($scope.writeOnlyKeys, function(k) {
        $scope.asset[k] = null;
      });
      $scope.cleared = {};
    }, function(response) {
      growl.error('Cannot update the asset record: ' + response.data);
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
      // An empty value is a cleared write-only field. Leave it out of the requisition.
      if (assets.hasOwnProperty(key) && assets[key] !== '') {
        assetFields.push({ name: key, value: assets[key] });
      }
    }
    $http.get('rest/requisitions/' + $scope.foreignSource + '/nodes/' + $scope.foreignId)
      .then(function(response) {
        const node = response.data;
        // The REST API never returns write-only fields, so the form only has them when the user typed a new value or cleared them.
        // Keep the requisition's existing entries otherwise, or replacing the asset list would drop them.
        angular.forEach(node.asset, function(entry) {
          if ($scope.writeOnlyKeys.indexOf(entry.name) !== -1 && !assets.hasOwnProperty(entry.name)) {
            assetFields.push(entry);
          }
        });
        node.asset = assetFields;
        $http.post('rest/requisitions/' + $scope.foreignSource + '/nodes', node)
          .then(function() {
            growl.success('Requisition ' + $scope.foreignSource + ' has been updated.');
          }, function() {
            growl.error('Cannot update requisition ' + $scope.foreignSource);
          });
      }, function(response) {
        if (response.status === 403) {
          growl.error('Cannot update requisition ' + $scope.foreignSource + ': reading requisitions requires the ROLE_PROVISION or ROLE_ADMIN role.');
        } else {
          growl.error('Cannot obtain node data from requisition ' + $scope.foreignSource);
        }
      });
  };

}]);

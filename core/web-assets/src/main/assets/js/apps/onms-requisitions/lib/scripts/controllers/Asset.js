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
require('../services/Requisitions');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014-2022 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.hashPrefix('');
  }])

  /**
  * @ngdoc controller
  * @name AssetController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires asset Node asset object
  * @requires assetsBlackList The black list of asset fields
  *
  * @description The controller for manage the modal dialog for add/edit asserts of requisitioned nodes
  */
  .controller('AssetController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'asset', 'assetsBlackList', function($scope, $uibModalInstance, RequisitionsService, asset, assetsBlackList) {

    /**
    * @description The asset object
    *
    * @ngdoc property
    * @name AssetController#asset
    * @propertyOf AssetController
    * @returns {object} The asset object
    */
    $scope.asset = asset;

    /**
    * @description The available asset fields
    *
    * @ngdoc property
    * @name AssetController#$scope.assetFields
    * @propertyOf AssetController
    * @returns {array} List of valid asset fields
    */
    $scope.assetFields = [];

    /**
    * @description The black list of asset fields. 
    *
    * @ngdoc property
    * @name AssetController#assetsBlackList
    * @propertyOf AssetController
    * @returns {array} The black list of asset fields.
    */
    $scope.assetsBlackList = assetsBlackList;

    /**
    * @description Saves the current asset
    *
    * @name AssetController:save
    * @ngdoc method
    * @methodOf AssetController
    */
    $scope.save = function() {
      $uibModalInstance.close($scope.asset);
    };

    /**
    * @description Cancels current operation
    *
    * @name AssetController:cancel
    * @ngdoc method
    * @methodOf AssetController
    */
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    /**
    * @description Get the unused available assets
    *
    * @name AssetController:getAvailableAssetFields
    * @ngdoc method
    * @methodOf AssetController
    * @returns {array} the unused available assets
    */
    $scope.getAvailableAssetFields = function() {
      var assets = [];
      angular.forEach($scope.assetFields, function(asset) {
        if ($scope.assetsBlackList.indexOf(asset) === -1) {
          assets.push(asset);
        }
      });
      return assets;
    };

    // Initialization

    RequisitionsService.getAvailableAssets().then(function(assets) {
      $scope.assetFields = assets;
    });
  }]);

}());

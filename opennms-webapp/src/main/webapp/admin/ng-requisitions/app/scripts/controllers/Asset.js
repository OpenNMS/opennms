/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name AssetController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $modalInstance Angular modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires EmptyTypeaheadService The empty typeahead Service
  * @requires asset Node asset object
  *
  * @description The controller for manage the modal dialog for add/edit asserts of requisitioned nodes
  */
  .controller('AssetController', ['$scope', '$modalInstance', 'RequisitionsService', 'EmptyTypeaheadService', 'asset', function($scope, $modalInstance, RequisitionsService, EmptyTypeaheadService, asset) {

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
    * @description fieldComparator method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name AssetController:fieldComparator
    * @methodOf AssetController
    */
    $scope.fieldComparator = EmptyTypeaheadService.fieldComparator;

    /**
    * @description onFocus method from EmptyTypeaheadService
    *
    * @ngdoc method
    * @name AssetController:onFocus
    * @methodOf AssetController
    */
    $scope.onFocus = EmptyTypeaheadService.onFocus;

    /**
    * @description Saves the current asset
    *
    * @name AssetController:save
    * @ngdoc method
    * @methodOf AssetController
    */
    $scope.save = function() {
      $modalInstance.close($scope.asset);
    };

    /**
    * @description Cancels current operation
    *
    * @name AssetController:cancel
    * @ngdoc method
    * @methodOf AssetController
    */
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  
    // Initialization

    RequisitionsService.getAvailableAssets().then(function(assets) {
      $scope.assetFields = assets;
    });
  }]);

}());
require('../services/Requisitions');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc controller
  * @name ServiceController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires service The service object
  *
  * @description The controller for manage the modal dialog for add/edit services of requisitioned nodes
  */
  .controller('ServiceController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'service', function($scope, $uibModalInstance, RequisitionsService, service) {

    /**
    * @description The service object
    *
    * @ngdoc property
    * @name ServiceController#service
    * @propertyOf ServiceController
    * @returns {object} The service object
    */
    $scope.service = service;

    /**
    * @description Saves the current interface
    *
    * @name ServiceController:save
    * @ngdoc method
    * @methodOf ServiceController
    */
    $scope.save = function () {
      $uibModalInstance.close($scope.service);
    };

    /**
    * @description Cancels the current operation
    *
    * @name ServiceController:cancel
    * @ngdoc method
    * @methodOf ServiceController
    */
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };

    /**
     * @description Adds a new empty meta-data entry
     *
     * @name ServiceController:addMetaData
     * @ngdoc method
     * @methodOf ServiceController
     */
    $scope.addMetaData = function() {
      $scope.service.requisitionMetaData.push({ key: '', value: '' });
    };

    /**
     * @description Removes a meta-data entry
     *
     * @name ServiceController:removeMetaData
     * @ngdoc method
     * @methodOf ServiceController
     * @param {integer} index The index of the meta-data entry to remove
     */
    $scope.removeMetaData= function(index) {
      $scope.service.requisitionMetaData.splice(index, 1);
    };

    // Initialization
  }]);

}());

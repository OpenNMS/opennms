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
  * @name InterfaceController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires RequisitionsService The Requisitions Servive
  * @requires foreignSource The requisition's name (a.k.a. foreign source)
  * @requires foreignId The foreign ID of the container node
  * @requires requisitionInterface The requisition interface object
  * @requires ipBlackList The black list of IP Addresses.
  *
  * @description The controller for manage the modal dialog for add/edit IP interfaces of requisitioned nodes
  */
  .controller('InterfaceController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'foreignSource', 'foreignId', 'requisitionInterface', 'ipBlackList', function($scope, $uibModalInstance, RequisitionsService, foreignSource, foreignId, requisitionInterface, ipBlackList) {

    /**
    * @description The foreign source (a.k.a the name of the requisition).
    *
    * @ngdoc property
    * @name InterfaceController#foreignSource
    * @propertyOf InterfaceController
    * @returns {object} The foreign source
    */
    $scope.foreignSource = foreignSource;

    /**
    * @description The foreign ID of the source container node
    *
    * @ngdoc property
    * @name InterfaceController#foreignId
    * @propertyOf InterfaceController
    * @returns {object} The foreign ID
    */
    $scope.foreignId = foreignId;

    /**
    * @description The interface object
    *
    * @ngdoc property
    * @name InterfaceController#requisitionInterface
    * @propertyOf InterfaceController
    * @returns {object} The interface object
    */
    $scope.requisitionInterface = requisitionInterface;

    /**
    * @description The black list of IP addresses. The IP defined on requisitionInterface should be contained on this black list.
    *
    * @ngdoc property
    * @name InterfaceController#ipBlackList
    * @propertyOf InterfaceController
    * @returns {array} The black list of IP addresses.
    */
    $scope.ipBlackList = ipBlackList;

    /**
    * @description An array map with the valid values for snmp-primary
    *
    * @ngdoc property
    * @name InterfaceController#snmpPrimaryFields
    * @propertyOf InterfaceController
    * @returns {object} The snmp primary fields object
    */
    $scope.snmpPrimaryFields = [
      { id: 'P', title: 'Primary' },
      { id: 'S', title: 'Secondary' },
      { id: 'N', title: 'Not Eligible'}
    ];

    /**
    * @description The available asset fields
    *
    * @ngdoc property
    * @name InterfaceController#availableServices
    * @propertyOf InterfaceController
    * @returns {array} List of available services
    */
    $scope.availableServices = [];

    /**
    * @description Saves the current interface
    *
    * @name InterfaceController:save
    * @ngdoc method
    * @methodOf InterfaceController
    */
    $scope.save = function () {
      $uibModalInstance.close($scope.requisitionInterface);
    };

    /**
    * @description Cancels the current operation
    *
    * @name InterfaceController:cancel
    * @ngdoc method
    * @methodOf InterfaceController
    */
    $scope.cancel = function () {
      $uibModalInstance.dismiss('cancel');
    };

    /**
    * @description Adds a new empty service
    *
    * @name InterfaceController:addService
    * @ngdoc method
    * @methodOf InterfaceController
    */
    $scope.addService = function() {
      $scope.requisitionInterface.services.push({ name: '' });
    };

    /**
    * @description Removes a service
    *
    * @name InterfaceController:removeService
    * @ngdoc method
    * @methodOf InterfaceController
    * @param {integer} index The index of the service to remove
    */
    $scope.removeService = function(index) {
      $scope.requisitionInterface.services.splice(index, 1);
    };

    /**
    * @description Get the unused available services
    *
    * @name InterfaceController:getAvailableServices
    * @ngdoc method
    * @methodOf InterfaceController
    * @returns {array} the unused available services
    */
    $scope.getAvailableServices = function() {
      var services = [];
      angular.forEach($scope.availableServices, function(avail) {
        var found = false;
        angular.forEach($scope.requisitionInterface.services, function(svc) {
          if (svc.name === avail) {
            found = true;
          }
        });
        if (!found) {
          services.push(avail);
        }
      });
      return services;
    };

    // Initialization

    RequisitionsService.getAvailableServices($scope.foreignSource).then(function(services) {
      $scope.availableServices = services;
    });

  }]);

}());

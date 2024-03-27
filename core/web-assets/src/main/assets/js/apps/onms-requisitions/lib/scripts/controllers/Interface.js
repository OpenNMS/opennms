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

const RequisitionService = require('../model/RequisitionService');

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
  .controller('InterfaceController', ['$scope', '$uibModalInstance', 'RequisitionsService', 'foreignSource', 'foreignId', 'requisitionInterface', 'ipBlackList', 'primaryInterface', function($scope, $uibModalInstance, RequisitionsService, foreignSource, foreignId, requisitionInterface, ipBlackList, primaryInterface) {

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


    $scope.primaryInterface = primaryInterface;

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

    $scope.getSnmpPrimaryValues = function(ipAddress) {
      const isPrimaryExists = $scope.primaryInterface !== null ? true : false;
      if(isPrimaryExists && ipAddress !== $scope.primaryInterface) {
        const snmpPrimary = $scope.snmpPrimaryFields.filter(field => field.id !== 'P');
        return snmpPrimary;
      }
      return $scope.snmpPrimaryFields;
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
      $scope.requisitionInterface.services.push(new RequisitionService({ 'service-name': '' }));
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

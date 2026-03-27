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
const RequisitionMetaDataEntry = require('../model/RequisitionMetaDataEntry');
const MetaDataConstants = require('../model/MetaDataConstants');
const Scope = MetaDataConstants.Scope;

(function() {
  'use strict';

  angular.module('onms-requisitions')

  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.hashPrefix('');
  }])

  /**
  * @ngdoc controller
  * @name MetaDataController
  * @module onms-requisitions
  *
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires node related node object
  * @requires entry related meta-data entry object
  *
  * @description The controller for manage the modal dialog for add/edit meta-data entries of requisitioned nodes
  */
  .controller('MetaDataController', ['$scope', '$uibModalInstance', 'node', 'entry', function($scope, $uibModalInstance, node, entry) {

    $scope.node = node;

    $scope.entry = entry;

    /**
     * Lookup the scoped entities based on the index of the selected elements.
     *
     * @param entry meta-data entry on which to resolve the scoped references
     */
    $scope.resolveScopeReferences = function(entry) {
      if (entry.scope === Scope.INTERFACE) {
        entry.scoped_interface = $scope.node.interfaces[entry.interface_idx];
      } else if (entry.scope === Scope.SERVICE) {
        entry.scoped_interface = $scope.interfacesWithServices[entry.interface_idx];
        entry.scoped_service = entry.scoped_interface.services[entry.service_idx];
      }
    };

    $scope.save = function() {
      $scope.resolveScopeReferences($scope.entry);
      $uibModalInstance.close($scope.entry);
    };

    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    $scope.getValueRowCount = function (entry) {
      // Expand size of value textarea to up to 3 rows if user enters 3+ lines
      if (!entry.value || entry.value.indexOf('\n') < 0) {
        return 1;
      }

      const lineBreaks = (entry.value.match(/\n/g) || []).length;

      return Math.min(lineBreaks + 1, 3);
    }

    // Initialization
    $scope.interfacesWithServices = [];
    $scope.availableScopes = {};
    $scope.availableScopes[Scope.NODE] = 'Node';

    if ($scope.node.interfaces && $scope.node.interfaces.length > 0) {
      // There are 1+ interfaces available, so we can set interface level meta-data on these
      $scope.availableScopes[Scope.INTERFACE] = 'Interface';

      // Now filter out the interfaces that have services
      $scope.node.interfaces.forEach(function(iff) {
        if (iff.services && iff.services.length > 0) {
          $scope.interfacesWithServices.push(iff);
        }
      });

      if ($scope.interfacesWithServices.length > 0) {
        // There are 1+ interfaces with services available, so we can set service level meta-data on these
        $scope.availableScopes[Scope.SERVICE] = 'Service';
      }
    }

    $scope.entry.interface_idx = 0;
    $scope.entry.service_idx = 0;

    if ($scope.entry.scope === Scope.INTERFACE && $scope.entry.scoped_interface) {
      // Find the index of the associated interface
      $scope.entry.interface_idx = $scope.node.interfaces ?
         $scope.node.interfaces.findIndex(function(intf) { return intf.ipAddress === $scope.entry.scoped_interface.ipAddress; }) : 0;

      if ($scope.entry.interface_idx < 0) {
        // The referenced interface no longer exists
        $scope.entry.interface_idx = 0;
      }
    } else if ($scope.entry.scope === Scope.SERVICE && $scope.entry.scoped_service && $scope.interfacesWithServices.length > 0) {
      // Find the index of the associated interface
      $scope.entry.interface_idx = $scope.interfacesWithServices ?
       $scope.interfacesWithServices.findIndex(function(intf) { return intf.ipAddress === $scope.entry.scoped_interface.ipAddress; }) : 0;

      if ($scope.entry.interface_idx < 0) {
        // The referenced interface no longer exists
        $scope.entry.interface_idx = 0;
      }
      // Find the index of the associated service
      $scope.entry.service_idx =
        $scope.interfacesWithServices && $scope.interfacesWithServices[$scope.entry.interface_idx] && $scope.interfacesWithServices[$scope.entry.interface_idx].services ?
        $scope.interfacesWithServices[$scope.entry.interface_idx].services.findIndex(function(svc) { return svc.name === $scope.entry.scoped_service.name; }) : 0;

      if ($scope.entry.service_idx < 0) {
        // The referenced service no longer exists
        $scope.entry.service_idx = 0;
      }
    }

    // Cast the indices to strings so that the proper option is automatically selected in the select boxes
    $scope.entry.interface_idx = String($scope.entry.interface_idx);
    $scope.entry.service_idx = String($scope.entry.service_idx);

    // Save the original key so that we can perform proper uniqueness validation
    $scope.originalKey = $scope.entry.key;
  }]);

}());

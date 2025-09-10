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

  const quickAddPanelBasicView = require('../../views/quick-add-panel-basic.html');
  const quickAddPanelSnmpView = require('../../views/quick-add-panel-snmp.html');
  const quickAddPanelCategoriesView = require('../../views/quick-add-panel-categories.html');
  // const quickAddPanelCliView = require('../../views/quick-add-panel-cli.html');
  const quickAddPanelHelpView = require('../../views/quick-add-panel-help.html');

  angular.module('onms-requisitions')

  .config(['$locationProvider', function($locationProvider) {
    $locationProvider.hashPrefix('');
  }])

  /**
  * @ngdoc controller
  * @name QuickAddNodeModalController
  * @module onms-requisitions
  *
  * @requires $controller Angular controller
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires foreignSources The list of available requisitions (a.k.a. foreign source)
  * @requires RequisitionsService The requisitions service
  * @requires growl The growl plugin for instant notifications
  *
  * @description The controller for manage the modal dialog for quick add a node to an existing requisition.
  */
  .controller('QuickAddNodeModalController', ['$controller', '$scope', '$uibModalInstance', 'foreignSources', 'RequisitionsService', 'growl', function($controller, $scope, $uibModalInstance, foreignSources, RequisitionsService, growl) {
    $scope.quickAddPanelBasicView = quickAddPanelBasicView;
    $scope.quickAddPanelSnmpView = quickAddPanelSnmpView;
    $scope.quickAddPanelCategoriesView = quickAddPanelCategoriesView;
    // $scope.quickAddPanelCliView = quickAddPanelCliView;
    $scope.quickAddPanelHelpView = quickAddPanelHelpView;

    /**
    * @description Provision the current node and close the modal operation
    *
    * @name QuickAddNodeModalController:modalProvision
    * @ngdoc method
    * @methodOf QuickAddNodeModalController
    */
    $scope.modalProvision = function() {
      $scope.provision();
      $uibModalInstance.close($scope.node);
    };

    /**
    * @description Cancels current modal operation
    *
    * @name QuickAddNodeModalController:modalCancel
    * @ngdoc method
    * @methodOf QuickAddNodeModalController
    */
    $scope.modalCancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

    // Extending QuickAddNodeController
    // eslint-disable-next-line no-invalid-this
    angular.extend(this, $controller('QuickAddNodeController', {
      $scope: $scope,
      foreignSources: foreignSources,
      RequisitionsService: RequisitionsService,
      growl: growl
    }));

  }]);

}());

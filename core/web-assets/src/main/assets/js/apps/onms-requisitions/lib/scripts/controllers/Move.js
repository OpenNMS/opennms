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
  * @name MoveController
  * @module onms-requisitions
  *
  * @requires $controller Angular controller
  * @requires $scope Angular local scope
  * @requires $uibModalInstance Angular UI modal instance
  * @requires label The label to show in front of the input field
  * @requires position The current value of the position
  * @requires maximum The maximum value allowed
  *
  * @description The controller for manage the modal dialog for move a table row
  */
  .controller('MoveController', ['$controller', '$scope', '$uibModalInstance', 'label', 'position', 'maximum', function($controller, $scope, $uibModalInstance, label, position, maximum) {

    /**
    * @description The label for the input field.
    *
    * @ngdoc property
    * @name MoveController#label
    * @propertyOf MoveController
    * @returns {string} The label
    */
    $scope.label = label;

    /**
    * @description The current position.
    *
    * @ngdoc property
    * @name MoveController#position
    * @propertyOf MoveController
    * @returns {integer} The position value
    */
    $scope.position = position;

    /**
    * @description The maximum value allowed for the position.
    *
    * @ngdoc property
    * @name MoveController#maximum
    * @propertyOf MoveController
    * @returns {integer} The maximum value
    */
    $scope.maximum = maximum;

    /**
    * @description Adds 1 from position
    *
    * @name MoveController:add
    * @ngdoc method
    * @methodOf MoveController
    */
    $scope.add = function() {
      $scope.position++;
    };

    /**
    * @description Substracts 1 from position
    *
    * @name MoveController:substract
    * @ngdoc method
    * @methodOf MoveController
    */
    $scope.substract = function() {
      $scope.position--;
    };

    /**
    * @description Closes the modal operation
    *
    * @name MoveController:move
    * @ngdoc method
    * @methodOf MoveController
    */
    $scope.move = function() {
      $uibModalInstance.close($scope.position);
    };

    /**
    * @description Cancels current modal operation
    *
    * @name MoveController:cancel
    * @ngdoc method
    * @methodOf MoveController
    */
    $scope.cancel = function() {
      $uibModalInstance.dismiss('cancel');
    };

  }]);

}());

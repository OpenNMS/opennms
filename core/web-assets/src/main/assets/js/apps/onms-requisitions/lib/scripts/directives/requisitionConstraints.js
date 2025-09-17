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
const { isValidIPAddress } = require('vendor/ipaddress-js');
const _ = require('lodash');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc directive
  * @name validIpAddress
  * @module onms-requisitions
  *
  * @description A directive to verify IPv4 and IPv6 addresses using a regular expression.
  * Also verifies if the given IP is unique on the node (to avoid duplicates). For this purpose,
  * it requires an array defined on the controller scope called ipBlackList if you want to make
  * the field invalid if the value is listed.
  */
  .directive('validIpAddress', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(ipAddress) {
          var found = scope.ipBlackList && scope.ipBlackList.indexOf(ipAddress) !== -1;
          if (found) { // It has to be unique
            ctrl.$setValidity('valid', false);
            return undefined;
          }
          if (isValidIPAddress(ipAddress)) {
            ctrl.$setValidity('valid', true);
            return ipAddress;
          }
          ctrl.$setValidity('valid', false);
          return undefined;
        });
      }
    };
  })

  /**
  * @ngdoc directive
  * @name validService
  * @module onms-requisitions
  *
  * @description A directive to verify if the given service is unique on the IP Interface.
  * This must be used on interface.html in conjunction with InterfaceController
  */
  .directive('validService', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(serviceName) {
          var found = false;
          if (scope.requisitionInterface && scope.requisitionInterface.services) {
            angular.forEach(scope.requisitionInterface.services, function(s) {
              if (s.$$hashKey !== scope.service.$$hashKey && s.name === serviceName) {
                found = true;
              }
            });
          }
          if (found) {
            ctrl.$setValidity('unique', false);
            return undefined;
          }

          ctrl.$setValidity('unique', true);
          return serviceName;
        });
      }
    };
  })

 /**
  * @ngdoc directive
  * @name validForeignId
  * @module onms-requisitions
  *
  * @description A directive to verify if the given foreign ID is unique on the requisition.
  * This must be used on node.html in conjunction with NodeController.
  * It requires an array defined on the controller scope called foreignIdBlackList if you want to make the field invalid if the value is listed.
  */
  .directive('validForeignId', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(foreignId) {
          var found = scope.foreignIdBlackList && scope.foreignIdBlackList.indexOf(foreignId) !== -1;
          if (found || foreignId.match(/[/\\?:&*'"]/)) {
            ctrl.$setValidity('valid', false);
            return undefined;
          }

          ctrl.$setValidity('valid', true);
          return foreignId;
        });
      }
    };
  })

  /**
  * @ngdoc directive
  * @name validCategory
  * @module onms-requisitions
  *
  * @description A directive to verify if the given category is unique on the node.
  * This must be used on node.html in conjunction with NodeController
  */
  .directive('validCategory', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      transclude: true,
      scope: { 'category': '=validCategory' },
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(categoryName) {
          if (!categoryName || categoryName.trim() === '') {
            ctrl.$setValidity('unique', false);
            return undefined;
          }
          var found = false;
          angular.forEach(scope.$parent.node.categories, function(c) {
            if (c.$$hashKey !== scope.category.$$hashKey && c.name === categoryName) {
              found = true;
            }
          });
          if (found) {
            ctrl.$setValidity('unique', false);
            return undefined;
          }

          ctrl.$setValidity('unique', true);
          return categoryName;
        });
      }
    };
  })

  /**
   * @ngdoc directive
   * @name validMetaDataKey
   * @module onms-requisitions
   *
   * @description A directive to verify if the meta-data key is unique with
   * a specific scope & context.
   *
   * This directive is intended to be used in the modal dialog used to edit
   * meta-data entries.
   *
   */
  .directive('validMetaDataKey', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      transclude: true,
      scope: { 'entry': '=validMetaDataKey' },
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(keyName) {
          let isUnique = true;
          if (keyName !== scope.$parent.originalKey) {
            // The key has changed, we need to validate it's uniqueness
            scope.$parent.resolveScopeReferences(scope.entry);
            const existingKeys = scope.$parent.node.metaData.getKeysInScopeOf(scope.entry);
            isUnique = _.indexOf(existingKeys, keyName) < 0;
          }

          if (!isUnique) {
            ctrl.$setValidity('unique', false);
            return undefined;
          }

          ctrl.$setValidity('unique', true);
          return keyName;
        });
      }
    };
  });



}());

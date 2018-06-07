const ip = require('vendor/ipaddress-js');
const Address4 = ip.Address4;
const Address6 = ip.Address6;

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
          var isValid = false;
          var ipv4 = new Address4(ipAddress);
          if (ipv4.isValid()) {
            isValid = true;
          } else {
            var ipv6 = new Address6(ipAddress);
            if (ipv6.isValid()) {
                isValid = true;
            }
          }
          if (!isValid) {
            ctrl.$setValidity('valid', false);
            return undefined;
          }
          ctrl.$setValidity('valid', true);
          return ipAddress;
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
  });

}());

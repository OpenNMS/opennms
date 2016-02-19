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
  * Also verifies if the given IP is unique on the node (to avoid duplicates).
  */
  .directive('validIpAddress', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(ipAddress) {
          var isValid = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-fA-F]|[a-fA-F][a-fA-F0-9\-]*[a-fA-F0-9])\.)*([A-Fa-f]|[A-Fa-f][A-Fa-f0-9\-]*[A-Fa-f0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(ipAddress);
          if (!isValid) {
            ctrl.$setValidity('valid', false);
            return undefined;
          }
          var found = scope.ipBlackList.indexOf(ipAddress) != -1;
          if (found) {
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
          angular.forEach(scope.requisitionInterface.services, function(s) {
            if (s.$$hashKey != scope.service.$$hashKey && s.name == serviceName) {
              found = true;
            }
          });
          if (found) {
            ctrl.$setValidity('unique', false);
            return undefined;
          } else {
            ctrl.$setValidity('unique', true);
            return serviceName;
          }
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
  * This must be used on node.html in conjunction with NodeController
  */
  .directive('validForeignId', function() {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function(scope, element, attrs, ctrl) {
        ctrl.$parsers.unshift(function(foreignId) {
          var found = scope.foreignIdBlackList.indexOf(foreignId) != -1;
          if (found) {
            ctrl.$setValidity('unique', false);
            return undefined;
          } else {
            ctrl.$setValidity('unique', true);
            return foreignId;
          }
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
          var found = false;
          angular.forEach(scope.$parent.node.categories, function(c) {
            if (c.$$hashKey != scope.category.$$hashKey && c.name == categoryName) {
              found = true;
            }
          });
          if (found) {
            ctrl.$setValidity('unique', false);
            return undefined;
          } else {
            ctrl.$setValidity('unique', true);
            return categoryName;
          }
        });
      }
    };
  });

}());

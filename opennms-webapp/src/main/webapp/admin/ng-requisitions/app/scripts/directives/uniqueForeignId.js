/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc directive
  * @name uniqueForeignId
  * @module onms-requisitions
  *
  * @description A directive to verify a foreign-id within a requisition.
  */
  .directive('uniqueForeignId', function() {
    return {
      restrict: 'A',
      transclude: true,
      require: 'ngModel',
      scope: { blackList: '=uniqueForeignId' },
      link: function(scope, elm, attrs, ctrl) {
        ctrl.$parsers.unshift(function(viewValue) {
          var found = false;
          angular.forEach(scope.blackList, function(foreignId) {
            if (foreignId == viewValue) {
              found = true;
            }
          });
          if (found) {
            ctrl.$setValidity('uniqueForeignId', false);
            return undefined;
          } else {
            ctrl.$setValidity('uniqueForeignId', true);
            return viewValue;
          }
        });
      }
    };
  });

}());

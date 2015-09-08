/*global $:true */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*
* Inspired By: http://plnkr.co/edit/bZMEOx0Qwo6VzW7oSuEE?p=preview
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

 /**
  * @ngdoc service
  * @name EmptyTypeaheadService
  * @module onms-requisitions
  *
  * @requires $timeout Angular timeout
  *
  * @description Additional functions required to be applied to the typeahead element in order to make it work
  */
  .factory('EmptyTypeaheadService', ['$timeout', function($timeout) {

    var emptyTypeaheadService = {};

    emptyTypeaheadService.secretEmptyKey = '[$empty$]';

    emptyTypeaheadService.fieldComparator = function(field, viewValue) {
      return viewValue === emptyTypeaheadService.secretEmptyKey || ('' + field).toLowerCase().indexOf(('' + viewValue).toLowerCase()) > -1;
    };

    emptyTypeaheadService.onFocus = function(e) {
      $timeout(function() {
        $(e.target).trigger('input');
      });
    };

    return emptyTypeaheadService;
  }])

  /**
  * @ngdoc directive
  * @name emptyTypeahead
  * @module onms-requisitions
  *
  * @requires EmptyTypeaheadService The empty typeahead Service
  *
  * @description A directive to show all options when focus on a typeahead element.
  */
  .directive('emptyTypeahead', ['EmptyTypeaheadService', function(EmptyTypeaheadService) {
    return {
      require: 'ngModel',

      link: function (scope, element, attrs, modelCtrl) {
        // this parser run before typeahead's parser
        modelCtrl.$parsers.unshift(function(inputValue) {
          var value = (inputValue ? inputValue : EmptyTypeaheadService.secretEmptyKey); // replace empty string with secretEmptyKey to bypass typeahead-min-length check
          modelCtrl.$viewValue = value; // this $viewValue must match the inputValue pass to typehead directive
          return value;
        });

        // this parser run after typeahead's parser
        modelCtrl.$parsers.push(function(inputValue) {
          return inputValue === EmptyTypeaheadService.secretEmptyKey ? '' : inputValue; // set the secretEmptyKey back to empty string
        });
      }
    };
  }]);

}());

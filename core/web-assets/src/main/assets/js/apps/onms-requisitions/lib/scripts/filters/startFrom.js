/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

(function() {

  'use strict';

  angular.module('onms-requisitions')

  /**
  * @ngdoc filter
  * @name startFrom
  * @module onms-requisitions
  *
  * @description A filter for paginated content
  *
  * @param {array} input The source array to filter
  * @param {integer} start The position index to start from
  *
  * @returns {array} the filtered array
  */
  .filter('startFrom', function() {
    return function(input, start) {
      const s = Number(start);
      if (input) {
        return input.length < s ? input : input.slice(s);
      }
      return [];
    };
  });

}());

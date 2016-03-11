/*jshint unused: false, undef:false */

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name RequisitionsData
* @module onms-requisitions
* @constructor
*/
function RequisitionsData() {

  'use strict';

  var self = this;

  /**
   * @description The configured requisitions.
   * @ngdoc property
   * @name RequisitionsData#requisitions
   * @propertyOf RequisitionsData
   * @returns {array} The requisitions array
   */
  self.requisitions = [];

  /**
  * @description Gets the array possition for a particular node
  *
  * @name RequisitionsData:indexOf
  * @ngdoc method
  * @param {string} foreignSource The foreign source (a.k.a requisition name)
  * @methodOf RequisitionsData
  * @returns {integer} the index (-1 if the foreign source doesn't exist)
  */
  self.indexOf = function(foreignSource) {
    for(var i = 0; i < self.requisitions.length; i++) {
      if (self.requisitions[i].foreignSource === foreignSource) {
        return i;
      }
    }
    return -1;
  };

  /**
  * @description Gets the requisition object for a given foreign source.
  *
  * @name RequisitionsData:getRequisition
  * @ngdoc method
  * @param {string} foreignSource The foreign source (a.k.a requisition name)
  * @methodOf RequisitionsData
  * @returns {object} the requisition object.
  */
  self.getRequisition = function(foreignSource) {
    var idx = self.indexOf(foreignSource);
    return idx < 0 ? null : self.requisitions[idx];
  };

  self.className = 'RequisitionsData';

  return self;
}

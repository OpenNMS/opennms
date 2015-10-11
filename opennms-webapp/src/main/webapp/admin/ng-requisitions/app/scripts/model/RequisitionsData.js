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
   * @description The status object, to obtain the number of deployed (status.deployed) and pending  (status.pending) requisitions.
   * @ngdoc property
   * @name RequisitionsData#status
   * @propertyOf RequisitionsData
   * @returns {object} The status Object
   */
  self.status = {
    deployed: 0,
    pending: 0
  };

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

  self.className = 'RequisitionsData';

  return self;
}

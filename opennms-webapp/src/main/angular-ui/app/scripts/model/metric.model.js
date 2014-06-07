/* global moment: true */

/**
 * @ngdoc object
 * @name Metric
 * @param {Object} metric an metric JSON object
 * @constructor
 */
function Metric(metric) {
  'use strict';

  var self = this;
  //console.log('metric:', metric);

  /**
   * @description
   * @ngdoc property
   * @name Metric#resourceid
   * @propertyOf Metric
   * @returns {number} Metric Resource ID
   */
  self.resourceid   = metric['resourceId'];

  /**
   * @description
   * @ngdoc property
   * @name Metric#resourceLabel
   * @propertyOf Metric
   * @returns {number} Metric Resource Label
   */
  self.resourceLabel   = metric['resourceLabel'];

  /**
   * @description
   * @ngdoc property
   * @name Metric#typeName
   * @propertyOf Metric
   * @returns {number} Metric Resource Type Name
   */
  self.typeName   = metric['typeName'];

  /**
   * @description
   * @ngdoc property
   * @name Metric#typeLabel
   * @propertyOf Metric
   * @returns {number} Metric Resource Type Label
   */
  self.typeLabel   = metric['typeLabel'];

  /**
   * @description
   * @ngdoc property
   * @name Metric#className
   * @propertyOf Metric
   * @returns {string} the name of this object class, used for troubleshooting and testing.
   */
  self.className = 'Metric';

}
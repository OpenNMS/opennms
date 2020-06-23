/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name RequisitionService
* @module onms-requisitions
* @param {Object} svc an OpenNMS service JSON object
* @constructor
*/
const RequisitionService = function RequisitionService(svc) {
  'use strict';

  const self = this;

  /**
   * @description The name of the service
   * @ngdoc property
   * @name RequisitionService#name
   * @propertyOf RequisitionService
   * @returns {string} The name of the service
   */
  self.name = svc['service-name'];

  self.className = 'RequisitionService';

  return self;
};

module.exports = RequisitionService;

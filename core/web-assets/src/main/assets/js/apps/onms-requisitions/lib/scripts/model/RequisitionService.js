/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

const RequisitionMetaData = require('./RequisitionMetaData');

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

  /**
   * @description The meta-data entries
   * @ngdoc property
   * @name RequisitionNode#metaData
   * @propertyOf RequisitionNode
   * @returns {object} The meta-data entries
   */
  self.metaData = new RequisitionMetaData(svc['meta-data']);

  self.className = 'RequisitionService';

  return self;
};

module.exports = RequisitionService;

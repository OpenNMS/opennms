/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

const _ = require('lodash');

/**
* @ngdoc object
* @name RequisitionInterface
* @module onms-requisitions
* @param {Object} intf an OpenNMS interface JSON object
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
   * @description The array of requisition metaData entries
   * @ngdoc property
   * @name RequisitionNode#requisitionMetaData
   * @propertyOf RequisitionNode
   * @returns {object} The requisition metaData entries
   */
  self.requisitionMetaData = [];

  /**
   * @description The array of other metaData entries
   * @ngdoc property
   * @name RequisitionNode#otherMetaData
   * @propertyOf RequisitionNode
   * @returns {object} The other metaData entries
   */
  self.otherMetaData = {};

  angular.forEach(svc['meta-data'], function(entry) {
    if (entry.context === 'requisition') {
      self.requisitionMetaData.push({
        'key': entry.key,
        'value': entry.value,
      });
    } else {
      if (!_.has(self.otherMetaData, entry.context)) {
        self.otherMetaData[entry.context] = []
      }
      self.otherMetaData[entry.context].push({
        'key': entry.key,
        'value': entry.value,
      });
    }
  });

  self.className = 'RequisitionService';

  return self;
};

module.exports = RequisitionService;

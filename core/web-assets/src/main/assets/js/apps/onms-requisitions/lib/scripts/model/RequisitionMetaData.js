/**
* @author Dustin Frisch <dustin@opennms.org>
* @copyright 2019 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name RequisitionMetaData
* @module onms-requisitions
* @param {Object} metaData an OpenNMS meta-data JSON list
* @constructor
*/
const RequisitionMetaData = function RequisitionMetaData(metaData) {
  'use strict';

  const self = this;

  /**
   * @description The array of requisition meta-data entries
   * @ngdoc property
   * @name RequisitionMetaData#requisition
   * @propertyOf RequisitionMetaData
   * @returns {object} The requisition meta-data entries
   */
  self.requisition = [];

  /**
   * @description The array of other meta-data entries
   * @ngdoc property
   * @name RequisitionMetaData#other
   * @propertyOf RequisitionMetaData
   * @returns {object} The other meta-data entries
   */
  self.other = {};

  angular.forEach(metaData, function(entry) {
    if (entry.context === 'requisition') {
      self.requisition.push({
        'key': entry.key,
        'value': entry.value
      });
    } else {
      if (!self.other.hasOwnProperty(entry.context)) {
        self.other[entry.context] = []
      }
      self.other[entry.context].push({
        'key': entry.key,
        'value': entry.value
      });
    }
  });

  /**
  * @description Gets the OpenNMS representation of the requisitioned meta-data
  *
  * @name RequisitionNode:getOnmsMetaData
  * @ngdoc method
  * @methodOf RequisitionMetaData
  * @returns {object} the meta-data Object
  */
  self.getOnmsMetaData = function() {
    var metaDataObject = [];

    angular.forEach(self.requisition, function(entry) {
      metaDataObject.push({
          'context': 'requisition',
          'key': entry.key,
          'value': entry.value
      });
    });

    angular.forEach(self.other, function(entries, context) {
      angular.forEach(entries, function(entry) {
        metaDataObject.push({
            'context': context,
            'key': entry.key,
            'value': entry.value
        });
      });
    });

    return metaDataObject;
  };

  self.className = 'RequisitionMetaData';

  return self;
};

module.exports = RequisitionMetaData;

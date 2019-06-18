/**
* @author Jesse White <jesse@opennms.org>
* @copyright 2019 The OpenNMS Group, Inc.
*/
const MetaDataConstants = require('./MetaDataConstants');
const Scope = MetaDataConstants.Scope;

/**
* @ngdoc object
* @name RequisitionMetaDataEntry
* @module onms-requisitions
* @constructor
*/
const RequisitionMetaDataEntry = function RequisitionMetaDataEntry() {
  'use strict';

  const self = this;

  // Default to the 'node' scope
  self.scope = Scope.NODE;

  // Must be set to the related RequisitionInterface object when the scope is set to 'interface' or 'service'
  self.scoped_interface = null;

  // Must be set to the related RequisitionService object when the scope is set to 'service'
  self.scoped_service = null;

  // Default to using the 'requisition' context
  self.context = MetaDataConstants.RequisitionContext;

  self.key = null;

  self.value = null;

  self.displayScope = function() {
    if (self.scope === Scope.INTERFACE) {
      return 'Interface';
    } else if (self.scope === Scope.SERVICE) {
      return 'Service';
    }
    return 'Node';
  };

  self.displayInterface = function() {
    if (self.scoped_interface !== null) {
      return self.scoped_interface.ipAddress;
    }
    return null;
  };

  self.displayService = function() {
    if (self.scoped_service !== null) {
      return self.scoped_service.name;
    }
    return null;
  };

  self.className = 'RequisitionMetaDataEntry';

  return self;
};

module.exports = RequisitionMetaDataEntry;

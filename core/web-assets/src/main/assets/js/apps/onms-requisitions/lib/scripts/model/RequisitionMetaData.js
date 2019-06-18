/**
* @author Dustin Frisch <dustin@opennms.org>
* @copyright 2019 The OpenNMS Group, Inc.
*/
const RequisitionMetaDataEntry = require('./RequisitionMetaDataEntry');
const RequisitionInterface = require('./RequisitionInterface');
const RequisitionService = require('./RequisitionService');
const MetaDataConstants = require('./MetaDataConstants');
const Scope = MetaDataConstants.Scope;

/**
* Container for all of the meta-data entries related to a specific node.
*
* @ngdoc object
* @name RequisitionMetaData
* @module onms-requisitions
* @param {Object} node an OpenNMS node JSON object
* @param {Object} requisitionNode RequisitionNode object
* @constructor
*/
const RequisitionMetaData = function RequisitionMetaData(node, requisitionNode) {
  'use strict';

  const self = this;

  /**
   * Contains all meta-data entries on the node that have context = 'requisition'
   */
  self.requisition = [];

  /**
   * Contains all meta-data entries on the node that have context != 'requisition'
   */
  self.other = [];

  const toMetaDataEntry = function(entry) {
    const entryObj = new RequisitionMetaDataEntry();
    entryObj.context = entry.context;
    entryObj.key = entry.key;
    entryObj.value = entry.value;
    return entryObj;
  };

  // Flatten all the meta-data entries into a single list while preserving
  // the scope and objects they relate to
  const allEntries = [];
  angular.forEach(node['meta-data'], function(entry) {
    const entryObj = toMetaDataEntry(entry);
    entryObj.scope = Scope.NODE;
    allEntries.push(entryObj);
  });

  angular.forEach(node.interface, function(iff) {
    const ri = new RequisitionInterface(iff);
    // Interface level
    angular.forEach(iff['meta-data'], function(entry) {
      const entryObj = toMetaDataEntry(entry);
      entryObj.scope = Scope.INTERFACE;
      entryObj.scoped_interface = ri;
      allEntries.push(entryObj);
    });

    angular.forEach(iff['monitored-service'], function(svc) {
      // Service level
      angular.forEach(svc['meta-data'], function(entry) {
        const entryObj = toMetaDataEntry(entry);
        entryObj.scope = Scope.SERVICE;
        entryObj.scoped_interface = ri;
        entryObj.scoped_service = new RequisitionService(svc);

        allEntries.push(entryObj);
      });
    });
  });

  /**
   * Remove meta-data entries for any referenced entities
   * that no longer exist.
   */
  self.removeEntriesForMissingScopedEntities = function() {
    _.remove(self.requisition, function(entry) {
      return !self.doesReferencedEntityExist(entry);
    });
    _.remove(self.other, function(entry) {
      return !self.doesReferencedEntityExist(entry);
    });
  };

  /**
   * @param entry meta-data entry to verify
   */
  self.doesReferencedEntityExist = function(entry) {
    if (entry.scope === Scope.INTERFACE || entry.scope === Scope.SERVICE) {
      // Does an interface exist with the given IP address
      let iff = _.find(requisitionNode.interfaces, function(iff) { return iff.ipAddress === entry.scoped_interface.ipAddress; });
      if (iff === undefined) {
        return false;
      }

      if (entry.scope === Scope.SERVICE) {
        // Does a service exist with the given name?
        let svc = _.find(iff.services, function(svc) { return svc.name === entry.scoped_service.name; });
        if (svc === undefined) {
          return false;
        }
      }
    }
    return true;
  };

  self.addEntry = function(entry) {
    if (entry.context === MetaDataConstants.RequisitionContext) {
      self.requisition.push(entry);
    } else {
      self.other.push(entry);
    }
  };

  self.removeEntry = function(entry) {
    _.remove(self.requisition, function(existingEntry) {
      return existingEntry === entry;
    });
    _.remove(self.other, function(existingEntry) {
      return existingEntry === entry;
    });
  };

  angular.forEach(allEntries, function(entry) {
    self.addEntry(entry);
  });

  self.getOnmsMetaData = function(predicate) {
    var metaDataObject = [];

    angular.forEach(self.requisition, function(entry) {
      if (!predicate(entry)) {
        return;
      }
      metaDataObject.push({
        'context': MetaDataConstants.RequisitionContext,
        'key': entry.key,
        'value': entry.value
      });
    });

    angular.forEach(self.other, function(entry) {
      if (!predicate(entry)) {
        return;
      }
      metaDataObject.push({
        'context': entry.context,
        'key': entry.key,
        'value': entry.value
      });
    });

    return metaDataObject;
  };

  self.getOnmsMetaDataForNode = function() {
    return self.getOnmsMetaData(function(entry) {
      return entry.scope === Scope.NODE;
    });
  };

  self.getOnmsMetaDataForInterface = function(intf) {
    return self.getOnmsMetaData(function(entry) {
      return entry.scope === Scope.INTERFACE
          && entry.scoped_interface.ipAddress === intf.ipAddress;
    });
  };

  self.getOnmsMetaDataForService = function(intf, svc) {
    return self.getOnmsMetaData(function(entry) {
      return entry.scope === Scope.SERVICE
          && entry.scoped_interface.ipAddress === intf.ipAddress
          && entry.scoped_service.name === svc.name
    });
  };

  /**
   * Used by the form validation constraint to return the set of keys
   * that exist in the same scope as the given entry
   *
   * @param lookup
   * @returns {string[]}
   */
  self.getKeysInScopeOf = function(lookup) {
    const metaData = self.getOnmsMetaData(function(entry) {
      // Start by matching both the scope and context
      if (entry.scope !== lookup.scope || entry.context !== lookup.context) {
        return false;
      }

      if (lookup.scope === Scope.INTERFACE || lookup.scope === Scope.SERVICE) {
        // The interfaces must match
        if (entry.scoped_interface.ipAddress !== lookup.scoped_interface.ipAddress) {
          return false;
        }

        if (lookup.scope === Scope.SERVICE) {
          // The services must match
          if (entry.scoped_service.name !== lookup.scoped_service.name) {
            return false;
          }
        }
      }

      return true;
    });

    return _.map(metaData, function(entry) {
      return entry.key;
    });
  };

  self.className = 'RequisitionMetaData';

  return self;
};

module.exports = RequisitionMetaData;

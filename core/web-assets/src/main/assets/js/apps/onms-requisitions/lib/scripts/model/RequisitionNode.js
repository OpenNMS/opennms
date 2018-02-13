/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

const RequisitionInterface = require('./RequisitionInterface');

// Internal function for initialization purposes
const isEmpty = function(str) {
  return (str === null || str === undefined || str.length === 0);
};

/**
* @ngdoc object
* @name RequisitionNode
* @module onms-requisitions
* @param {string} foreignSource the name of the foreign source (a.k.a. provisioning group)
* @param {Object} node an OpenNMS node JSON object
* @param {boolean} isDeployed true if the node has been deployed
* @constructor
*/
const RequisitionNode = function RequisitionNode(foreignSource, node, isDeployed) {

  'use strict';

  const self = this;

  /**
   * @description the foreign source
   * @ngdoc property
   * @name RequisitionNode#foreignSource
   * @propertyOf RequisitionNode
   * @returns {string} the foreign source
   */
  self.foreignSource = foreignSource;

  /**
   * @description The deployed flag
   * @ngdoc property
   * @name RequisitionNode#deployed
   * @propertyOf RequisitionNode
   * @returns {boolean} true, if the node has been deployed
   */
  self.deployed = isDeployed;

  /**
   * @description The modified flag
   * @ngdoc property
   * @name RequisitionNode#modified
   * @propertyOf RequisitionNode
   * @returns {boolean} true, if the node has been modified
   */
  self.modified = false;

  /**
   * @description The foreign Id
   * @ngdoc property
   * @name RequisitionNode#foreignId
   * @propertyOf RequisitionNode
   * @returns {string} The foreign Id
   */
  self.foreignId = node['foreign-id'];
  if (isEmpty(self.foreignId)) {
    self.foreignId = String(new Date().getTime());
  }

  /**
   * @description The node's label
   * @ngdoc property
   * @name RequisitionNode#nodeLabel
   * @propertyOf RequisitionNode
   * @returns {string} the node's label
   */
  self.nodeLabel = node['node-label'];

  /**
   * @description The location of the node
   * @ngdoc property
   * @name RequisitionNode#location
   * @propertyOf RequisitionNode
   * @returns {string} The location
   */
  self.location = node['location'];

  /**
   * @description The city where the node is located
   * @ngdoc property
   * @name RequisitionNode#city
   * @propertyOf RequisitionNode
   * @returns {string} The city
   */
  self.city = node['city'];

  /**
   * @description The building where the node is located
   * @ngdoc property
   * @name RequisitionNode#building
   * @propertyOf RequisitionNode
   * @returns {string} The building
   */
  self.building = node['building'];

  /**
   * @description The parent foreign source (for path outages), required if the parent node exist on a different requisition.
   * @ngdoc property
   * @name RequisitionNode#parentForeignSource
   * @propertyOf RequisitionNode
   * @returns {string} The parent foreign source
   */
  self.parentForeignSource = node['parent-foreign-source'];

  /**
   * @description The parent foreign ID (for path outages), to uniquely identify the parent node (can not be used if parentNodeLabel is defined)
   * @ngdoc property
   * @name RequisitionNode#parentForeignId
   * @propertyOf RequisitionNode
   * @returns {string} The parent foreign ID
   */
  var _parentForeignId = node['parent-foreign-id'];
  self.parentForeignId = isEmpty(_parentForeignId) ? null : _parentForeignId;

  /**
   * @description The parent node label (for path outages), to uniquely identify the parent node (can not be used if parentForeignId is defined)
   * @ngdoc property
   * @name RequisitionNode#parentNodeLabel
   * @propertyOf RequisitionNode
   * @returns {string} The parent foreign Label
   */
  var _parentNodeLabel = node['parent-node-label'];
  self.parentNodeLabel = isEmpty(_parentNodeLabel) ? null : _parentNodeLabel;

  /**
   * @description The array of interfaces
   * @ngdoc property
   * @name RequisitionNode#interfaces
   * @propertyOf RequisitionNode
   * @returns {array} The interfaces
   */
  self.interfaces = [];

  /**
   * @description The array of categories
   * @ngdoc property
   * @name RequisitionNode#categories
   * @propertyOf RequisitionNode
   * @returns {array} The categories
   */
  self.categories = [];

  /**
   * @description The array of assets
   * @ngdoc property
   * @name RequisitionNode#assets
   * @propertyOf RequisitionNode
   * @returns {array} The assets
   */
  self.assets = [];

  angular.forEach(node['interface'], function(intf) {
    self.interfaces.push(new RequisitionInterface(intf));
  });

  angular.forEach(node['asset'], function(asset) {
    self.assets.push(asset);
  });

  angular.forEach(node['category'], function(category) {
    self.categories.push(category);
  });

  /**
  * @description Check if the node has been changed
  *
  * @name RequisitionNode:isModified
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {boolean} true if the node has been changed or modified.
  */
  self.isModified = function() {
    if (self.modified) {
      return true;
    }
    return ! self.deployed;
  };

  /**
  * @description Adds a new interface to the node
  *
  * @name RequisitionNode:addNewInterface
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {object} the new interface Object
  */
  self.addNewInterface = function() {
    var found = false;
    angular.forEach(self.interfaces, function(intf) {
      if (intf.snmpPrimary === 'P') {
        found = true;
      }
    });
    self.interfaces.push(new RequisitionInterface({
      'snmp-primary': (found ? 'N' : 'P')
    }));
    return self.interfaces.length - 1;
  };

  /**
  * @description Adds a new asset to the node
  *
  * @name RequisitionNode:addNewAsset
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {object} the new service Object
  */
  self.addNewAsset = function() {
    self.assets.push({
      name: '',
      value: ''
    });
    return self.assets.length -1;
  };

  /**
  * @description Adds a new category to the node
  *
  * @name RequisitionNode:addNewCategory
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {object} the new service Object
  */
  self.addNewCategory = function() {
    self.categories.push({
      name: ''
    });
    return self.categories.length -1;
  };

  /**
  * @description Gets the primary IP address if exist.
  *
  * @name RequisitionNode:getPrimaryIpAddress
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {string} the primary IP address (null if it doesn't exist)
  */
  self.getPrimaryIpAddress = function() {
    var ip = null;
    angular.forEach(self.interfaces, function(intf) {
      if (intf.snmpPrimary === 'P') {
        ip = intf.ipAddress;
      }
    });
    return ip;
  };

  /**
  * @description Checks if the node has parent information (for path outages).
  *
  * @name RequisitionNode:hasParentInformation
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {boolean} true, if the node has parent information.
  */
  self.hasParentInformation = function() {
    if (self.parentForeignSource && self.parentForeignSource.trim() !== '') {
      return true;
    }
    if (self.parentForeignId && self.parentForeignId.trim() !== '') {
      return true;
    }
    if (self.parentNodeLabel && self.parentNodeLabel.trim() !== '') {
      return true;
    }
    return false;
  };

  /**
  * @description Gets the OpenNMS representation of the requisitioned node
  *
  * @name RequisitionNode:getOnmsRequisitionNode
  * @ngdoc method
  * @methodOf RequisitionNode
  * @returns {object} the requisition Object
  */
  self.getOnmsRequisitionNode = function() {
    var nodeObject = {
      'foreign-id': self.foreignId,
      'node-label': self.nodeLabel,
      'location': self.location,
      'city': self.city,
      'building': self.building,
      'interface': [],
      'parent-foreign-source': self.parentForeignSource,
      'parent-foreign-id': self.parentForeignId,
      'parent-node-label': self.parentNodeLabel,
      'asset': [],
      'category': []
    };

    angular.forEach(self.interfaces, function(intf) {
      var interfaceObject = {
        'ip-addr': intf.ipAddress,
        'descr': intf.description,
        'snmp-primary': intf.snmpPrimary,
        'status': (intf.status || intf.status === 'managed') ? '1' : '3',
        'monitored-service': []
      };
      angular.forEach(intf.services, function(service) {
        interfaceObject['monitored-service'].push({
          'service-name': service.name
        });
      });

      nodeObject['interface'].push(interfaceObject);
    });

    angular.forEach(self.assets, function(asset) {
      nodeObject['asset'].push(asset);
    });

    angular.forEach(self.categories, function(category) {
      nodeObject['category'].push(category);
    });

    return nodeObject;
  };

  self.className = 'RequisitionNode';

  return self;
}

module.exports = RequisitionNode;
const RequisitionNode = require('./RequisitionNode');

/**
* @author Alejandro Galue <agalue@opennms.org>
* @copyright 2014 The OpenNMS Group, Inc.
*/

/**
* @ngdoc object
* @name Requisition
* @module onms-requisitions
* @param {object} requisition an OpenNMS requisition JSON object
* @param {boolean} isDeployed true if the requisition has been deployed
* @constructor
*/
const Requisition = function Requisition(requisition, isDeployed) {

  'use strict';

  const self = this;

  /**
   * @description The deployed flag
   * @ngdoc property
   * @name Requisition#deployed
   * @propertyOf Requisition
   * @returns {boolean} true, if the requisition has been deployed
   */
  self.deployed = isDeployed;

  /**
   * @description The modified flag
   * @ngdoc property
   * @name Requisition#modified
   * @propertyOf Requisition
   * @returns {boolean} true, if the requisition has been modified
   */
  self.modified = false;

  /**
   * @description The name of the requisition (the foreign source)
   * @ngdoc property
   * @name Requisition#foreignSource
   * @propertyOf Requisition
   * @returns {string} the foreign source
   */
  self.foreignSource = requisition['foreign-source'];

  /**
   * @description The last modication date of the requisition
   * @ngdoc property
   * @name Requisition#dateStamp
   * @propertyOf Requisition
   * @returns {string} the last modification date
   */
  self.dateStamp = requisition['date-stamp'] || Date.now();

  /**
   * @description The last import date of the requisition
   * @ngdoc property
   * @name Requisition#lastImport
   * @propertyOf Requisition
   * @returns {string} the last import date
   */
  self.lastImport = requisition['last-import'];

  /**
   * @description The configured nodes array
   * @ngdoc property
   * @name Requisition#nodes
   * @propertyOf Requisition
   * @returns {array} The nodes array
   */
  self.nodes = [];

  angular.forEach(requisition.node, function(node) {
    var requisitionNode = new RequisitionNode(self.foreignSource, node, isDeployed);
    self.nodes.push(requisitionNode);
  });

  /**
   * @description The number of nodes stored on the database
   * @ngdoc property
   * @name Requisition#nodesInDatabase
   * @propertyOf Requisition
   * @returns {interger} number of nodes in the database
   */
  self.nodesInDatabase = 0;

  /**
   * @description The number of nodes defined on the requisition
   * @ngdoc property
   * @name Requisition#nodesDefined
   * @propertyOf Requisition
   * @returns {interger} number of nodes defined
   */
  self.nodesDefined = self.nodes.length;

  /**
  * @description Checks if the requisition has been changed
  *
  * @name Requisition:isModified
  * @ngdoc method
  * @methodOf Requisition
  * @returns {boolean} true if the requisition has been changed or modified.
  */
  self.isModified = function() {
    if (self.modified) {
      return true;
    }
    return ! self.deployed;
  };

  /**
  * @description Gets the array possition for a particular node
  *
  * @name Requisition:indexOf
  * @ngdoc method
  * @param {string} foreignId The foreign ID of the node
  * @methodOf Requisition
  * @returns {integer} the index (-1 if the foreign ID doesn't exist)
  */
  self.indexOf = function(foreignId) {
    for(var i = 0; i < self.nodes.length; i++) {
      if (self.nodes[i].foreignId === foreignId) {
        return i;
      }
    }
    return -1;
  };

  /**
  * @description Gets a specific node object.
  *
  * @name Requisition:getNode
  * @ngdoc method
  * @param {string} foreignId The foreign ID of the node
  * @methodOf Requisition
  * @returns {object} the node object.
  */
  self.getNode = function(foreignId) {
    var idx = self.indexOf(foreignId);
    return idx < 0 ? null : self.nodes[idx];
  };

  /**
  * @description Adds or replaces a node object.
  *
  * @name Requisition:setNode
  * @ngdoc method
  * @param {object} node The RequisitionNode object
  * @methodOf Requisition
  */
  self.setNode = function(node) {
    var idx = self.indexOf(node.foreignId);
    if (idx < 0) {
      self.nodes.push(node);
      self.nodesDefined++;
    } else {
      self.nodes[idx] = node;
    }
    self.modified = true;
    self.dateStamp = Date.now();
  };

  /**
  * @description Marks the requisition as deployed
  *
  * @name Requisition:setDeployed
  * @ngdoc method
  * @param {boolean} deployed true, if the requisition has been deployed
  * @methodOf Requisition
  */
  self.setDeployed = function(deployed) {
    self.deployed = deployed;
    self.modified = false;
    angular.forEach(self.nodes, function(node) {
      node.deployed = deployed;
      node.modified = false;
    });
    self.lastImport = deployed ? Date.now() : null;
  };

  /**
  * @description Resets the content of the requisition
  *
  * @name Requisition:reset
  * @ngdoc method
  * @methodOf Requisition
  */
  self.reset = function() {
    self.nodes = [];
    self.nodesDefined = 0;
    self.nodesInDatabase = 0;
    self.modified = true;
    self.dateStamp = Date.now();
  };

  self.className = 'Requisition';

  return self;
}

module.exports = Requisition;
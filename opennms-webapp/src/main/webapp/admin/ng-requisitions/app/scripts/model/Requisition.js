/*global RequisitionNode:true */
/*jshint unused: false, undef:false */

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
function Requisition(requisition, isDeployed) {

  'use strict';

  var self = this;

  /**
   * @description The deployed flag
   * @ngdoc property
   * @name Requisition#deployed
   * @propertyOf Requisition
   * @returns {boolean} true, if the requisition has been deployed
   */
  self.deployed = isDeployed;

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
  self.nodesDefined = 0;

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
  * @description Updates the internal statistics of nodes defined/deployed
  *
  * @name Requisition:updateStats
  * @ngdoc method
  * @methodOf Requisition
  */
  self.updateStats = function() {
    if (self.deployed) {
      self.nodesInDatabase = self.nodes.length;
    } else {
      self.nodesDefined = self.nodes.length;
    }
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
    angular.forEach(self.nodes, function(node) {
      node.deployed = deployed;
    });
    self.updateStats();
  };

  self.updateStats();

  self.className = 'Requisition';

  return self;
}

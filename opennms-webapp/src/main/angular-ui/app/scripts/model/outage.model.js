/* global moment: true */

/**
 * @ngdoc object
 * @name Outage
 * @param {Object} outage an outage JSON object
 * @constructor
 */
function Outage(outage) {
  'use strict';

  var self = this;

  /**
   * @description
   * @ngdoc property
   * @name Outage#id
   * @propertyOf Outage
   * @returns {number} The unique outage ID
   */
  self.id = Number(outage['_id']);

  /**
   * @description
   * @ngdoc property
   * @name Outage#nodeid
   * @propertyOf Outage
   * @returns {number} Unique integer identifier for node
   */
  self.nodeId = Number(outage['nodeId']);

  /**
   * @description
   * @ngdoc property
   * @name Outage#ipAddress
   * @propertyOf Outage
   * @returns {string} IP Address of node's interface
   */
  self.ipAddress = outage['ipAddress'];

  /**
   * @description
   * @ngdoc property
   * @name Outage#ifLostService
   * @propertyOf Outage
   * @returns {*|Date} The time the service was lost.
   */
  self.ifLostService = moment(outage['_ifLostService']);

  if (outage['_ifRegainedService']) {
    /**
     * @description
     * @ngdoc property
     * @name Outage#ifRegainedService
     * @propertyOf Outage
     * @returns {*|Date} The time the service was regained. Property is undefined if the service hasn't been regained.
     */
    self.ifRegainedService = moment(outage['_ifRegainedService']);
  }

  /**
   * @description
   * @ngdoc property
   * @name Outage#monitoredService
   * @propertyOf Outage
   * @returns {MonitoredService} The monitored service related to this outage.
   */
  self.monitoredService = new MonitoredService(outage['monitoredService']);

  /**
   * @description
   * @ngdoc property
   * @name Outage#serviceLostEvent
   * @propertyOf Outage
   * @returns {Event} The event that was emitted when the service was lost.
   */
  self.serviceLostEvent = new Event(outage['serviceLostEvent']);
  //self.node = new Node(self.nodeId);

  /**
   * @description
   * @ngdoc property
   * @name Outage#nodeLabel
   * @propertyOf Outage
   * @returns {string} The human-readable name of the node of this alarm.
   */
  self.nodeLabel = self.serviceLostEvent.nodeLabel;

  /**
   * @description Helper method to get a friendly node label. It will generate
   *              a node label based on the node ID if the nodeLabel property
   *              is not defined or is empty.
   * @ngdoc method
   * @name Outage#getNodeName
   * @methodOf Outage
   * @returns {string} a formatted node label using the nodeLabel or the nodeId formatted into a string.
   */
  self.getNodeName = function() {
    if (self.nodeLabel === undefined || self.nodeLabel === '') {
      return 'Node ' + self.nodeId;
    } else {
      return self.nodeLabel;
    }
  };

  /**
   * @description Provides a formatted severity CSS class
   * @ngdoc method
   * @name Outage#getSeverityClass
   * @methodOf Outage
   * @returns {string} formatted CSS class name
   */
  self.getSeverityClass = function() {
    if (this.serviceLostEvent.severity !== null && angular.isString(this.serviceLostEvent.severity) && this.serviceLostEvent.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.serviceLostEvent.severity);
    }
    return '';
  };

}
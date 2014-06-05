/* global moment: true */

/**
 * @ngdoc object
 * @name Event
 * @param {Object} event an event JSON object
 * @constructor
 */
function Event(event) {
  'use strict';
  //console.log('new Event():', event);

  var self = this;

  /**
   * @description
   * @ngdoc property
   * @name Event#eventId
   * @propertyOf Event
   * @returns {number} Unique identifier for the event
   */
  self.eventId = Number(event['_id']);

  /**
   * @description
   * @ngdoc property
   * @name Event#uei
   * @propertyOf Event
   * @returns {string} Universal Event Identifer (UEI) for this event
   */
  self.uei = event['uei'];

  /**
   * @description
   * @ngdoc property
   * @name Event#nodeId
   * @propertyOf Event
   * @returns {number} Unique integer identifier for node
   */
  self.nodeId = Number(event['nodeId']);

  /**
   * @description
   * @ngdoc property
   * @name Event#nodeLabel
   * @propertyOf Event
   * @returns {string} The human-readable name of the node of this event.
   */
  self.nodeLabel = event['nodeLabel'];

  /**
   * @description
   * @ngdoc property
   * @name Event#ipAddress
   * @propertyOf Event
   * @returns {string} IP Address of node's interface
   */
  self.ipAddress = event['ipAddress'];

  /**
   * @description
   * @ngdoc property
   * @name Event#severity
   * @propertyOf Event
   * @returns {string} Severity the of event.
   */
  self.severity = event['_severity'];

  /**
   * @description
   * @ngdoc property
   * @name Event#createTime
   * @propertyOf Event
   * @returns {*|Date} Creation time of event in database
   */
  self.createTime = moment(event['createTime']);

  /**
   * @description
   * @ngdoc property
   * @name Event#time
   * @propertyOf Event
   * @returns {*|Date} The &lt;time&gt; element from the Event Data Stream DTD, which is the time the event was received by the source process.
   */
  self.time = moment(event['time']);

  /**
   * @description
   * @ngdoc property
   * @name Event#source
   * @propertyOf Event
   * @returns {string} The subsystem the event originated from.
   */
  self.source = event['source'];

  /**
   * @description
   * @ngdoc property
   * @name Event#log
   * @propertyOf Event
   * @returns {boolean} Whether the event was logged but not displayed.
   */
  self.log = event['_log'] === 'Y';

  /**
   * @description
   * @ngdoc property
   * @name Event#display
   * @propertyOf Event
   * @returns {boolean} Whether the event was both logged and displayed.
   */
  self.display = event['_display'] === 'Y';

  /**
   * @description
   * @ngdoc property
   * @name Event
   * @propertyOf Event
   * @returns {string} Free-form textual description of the event
   */
  self.description = event['description'];

  /**
   * @description
   * @ngdoc property
   * @name Event#logMessage
   * @propertyOf Event
   * @returns {string} Formatted display text to control how the event will appear in the browser.
   */
  self.logMessage = event['logMessage'];

  // Check to see if the event JSON has the 'serviceType' property before parsing it.
  if (event.hasOwnProperty('serviceType')) {

    /**
     * @description
     * @ngdoc property
     * @name Event#serviceType
     * @propertyOf Event
     * @returns {number} Unique integer identifier of service/poller package
     */
    self.serviceType = event['serviceType']['_id'];

    /**
     * @description
     * @ngdoc property
     * @name Event#serviceName
     * @propertyOf Event
     * @returns {string} Human-readable name of the service
     */
    self.serviceName = event['serviceType']['name'];
  }
  // XXX: convert event['parms'] into an object for parms.
  self.parms = {};

  /**
   * @description Provides a formatted severity CSS class
   * @ngdoc method
   * @name Event#getSeverityClass
   * @methodOf Event
   * @returns {string} formatted CSS class name
   */
  self.getSeverityClass = function() {
    if (this.severity !== null && angular.isString(this.severity) && this.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.severity);
    }
    return '';
  };

}
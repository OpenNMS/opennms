/* global moment: true */

/**
 * @ngdoc object
 * @name models.Event
 * @param {Object} alarm an event JSON object
 * @constructor
 */
function Event(event) {
  'use strict';
  //console.log('new Event():', event);

  var self = this;

  /**
   * @description
   * @ngdoc property
   * @name models.Event#eventId
   * @propertyOf models.Event
   * @returns {number} Unique identifier for the event
   */
  self.eventId = Number(event['_id']);
  self.uei = event['uei'];

  self.nodeId = Number(event['nodeId']);
  self.nodeLabel = event['nodeLabel'];
  self.ipAddress = event['ipAddress'];

  self.severity = event['_severity'];

  self.createTime = moment(event['createTime']);
  self.time = moment(event['time']);

  self.source = event['source'];
  self.log = event['_log'] === 'Y';
  self.display = event['_display'] === 'Y';
  self.description = event['description'];
  self.logMessage = event['logMessage'];
  if (event.hasOwnProperty('serviceType')) {
    self.serviceType = event['serviceType']['_id'];
    self.serviceName = event['serviceType']['name'];
  }
  // XXX: convert event['parms'] into an object for parms.
  self.parms = {};

  self.getSeverityClass = function() {
    if (this.severity !== null && angular.isString(this.severity) && this.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.severity);
    }
    return '';
  };

}
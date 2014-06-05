/* global moment: true */

/**
 * @ngdoc object
 * @name models.Alarm
 * @param {Object} alarm an alarm JSON object
 * @constructor
 */
function Alarm(alarm) {
  'use strict';

  var self = this;
  //console.log('alarm:', alarm);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#alarmId
   * @propertyOf models.Alarm
   * @returns {number} Alarm ID
   */
  self.alarmId   = Number(alarm['_id']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#uei
   * @propertyOf models.Alarm
   * @returns {string} Universal Event Identifier for the alarm.
   */
  self.uei   = alarm['uei'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#severity
   * @propertyOf models.Alarm
   * @returns {string} Severity the of alarm.
   */
  self.severity   = alarm['_severity'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#type
   * @propertyOf models.Alarm
   * @returns {number} Alarm type ID, see {@link http://www.opennms.org/wiki/Configuring_alarms#Alarm_Types alarm types}
   */
  self.type   = Number(alarm['_type']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#description
   * @propertyOf models.Alarm
   * @returns {string} The description of the alarm
   */
  self.description   = alarm['description'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#firstTimeEvent
   * @propertyOf models.Alarm
   * @returns {*|Date} The first time an event was reduced by this alarm
   */
  self.firstEventTime   = moment(alarm['firstEventTime']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#lastEventTime
   * @propertyOf models.Alarm
   * @returns {*|Date} The last time an event was reduced by this alarm
   */
  self.lastEventTime   = moment(alarm['lastEventTime']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#lastEvent
   * @propertyOf models.Alarm
   * @returns {Event} The last event to be reduced by this alarm
   */
  self.lastEvent   = new Event(alarm['lastEvent']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#logMessage
   * @propertyOf models.Alarm
   * @returns {string} Formatted display text to control how the alarm will appear in the browser.
   */
  self.logMessage   = alarm['logMessage'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#reductionKey
   * @propertyOf models.Alarm
   * @returns {string} Reduction key for this alarm
   */
  self.reductionKey   = alarm['reductionKey'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#nodeid
   * @propertyOf models.Alarm
   * @returns {number} Unique integer identifier for node
   */
  self.nodeid   = Number(alarm['nodeId']);

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#nodeLabel
   * @propertyOf models.Alarm
   * @returns {string} The human-readable name of the node of this alarm.
   */
  self.nodeLabel   = alarm['nodeLabel'];

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#parms
   * @propertyOf models.Alarm
   * @returns {object} The &lt;parms&gt; element for this alarm.
   */
  self.parms   = {};
  // alarm['parms'];

  /**
   * @description Provides a formatted severity CSS class
   * @ngdoc method
   * @name models.Alarm#getSeverityClass
   * @methodOf models.Alarm
   * @returns {string} formatted CSS class name
   */
  self.getSeverityClass = function() {
    if (this.severity !== null && angular.isString(this.severity) && this.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.severity);
    }
    return '';
  };

  /**
   * @description
   * @ngdoc property
   * @name models.Alarm#className
   * @propertyOf models.Alarm
   * @returns {string} the name of this object class, used for troubleshooting and testing.
   */
  self.className = 'Alarm';

}
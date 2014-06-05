/* global moment: true */

/**
 * @ngdoc object
 * @name Alarm
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
   * @name Alarm#alarmId
   * @propertyOf Alarm
   * @returns {number} Alarm ID
   */
  self.alarmId   = Number(alarm['_id']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#uei
   * @propertyOf Alarm
   * @returns {string} Universal Event Identifier for the alarm.
   */
  self.uei   = alarm['uei'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#severity
   * @propertyOf Alarm
   * @returns {string} Severity the of alarm.
   */
  self.severity   = alarm['_severity'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#type
   * @propertyOf Alarm
   * @returns {number} Alarm type ID, see {@link http://www.opennms.org/wiki/Configuring_alarms#Alarm_Types alarm types}
   */
  self.type   = Number(alarm['_type']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#description
   * @propertyOf Alarm
   * @returns {string} The description of the alarm
   */
  self.description   = alarm['description'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#firstTimeEvent
   * @propertyOf Alarm
   * @returns {*|Date} The first time an event was reduced by this alarm
   */
  self.firstEventTime   = moment(alarm['firstEventTime']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#lastEventTime
   * @propertyOf Alarm
   * @returns {*|Date} The last time an event was reduced by this alarm
   */
  self.lastEventTime   = moment(alarm['lastEventTime']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#lastEvent
   * @propertyOf Alarm
   * @returns {Event} The last event to be reduced by this alarm
   */
  self.lastEvent   = new Event(alarm['lastEvent']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#logMessage
   * @propertyOf Alarm
   * @returns {string} Formatted display text to control how the alarm will appear in the browser.
   */
  self.logMessage   = alarm['logMessage'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#reductionKey
   * @propertyOf Alarm
   * @returns {string} Reduction key for this alarm
   */
  self.reductionKey   = alarm['reductionKey'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#nodeid
   * @propertyOf Alarm
   * @returns {number} Unique integer identifier for node
   */
  self.nodeid   = Number(alarm['nodeId']);

  /**
   * @description
   * @ngdoc property
   * @name Alarm#nodeLabel
   * @propertyOf Alarm
   * @returns {string} The human-readable name of the node of this alarm.
   */
  self.nodeLabel   = alarm['nodeLabel'];

  /**
   * @description
   * @ngdoc property
   * @name Alarm#parms
   * @propertyOf Alarm
   * @returns {object} The &lt;parms&gt; element for this alarm.
   */
  self.parms   = {};
  // alarm['parms'];

  /**
   * @description Provides a formatted severity CSS class
   * @ngdoc method
   * @name Alarm#getSeverityClass
   * @methodOf Alarm
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
   * @name Alarm#className
   * @propertyOf Alarm
   * @returns {string} the name of this object class, used for troubleshooting and testing.
   */
  self.className = 'Alarm';

}
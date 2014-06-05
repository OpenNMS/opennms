/* global moment: true */

function Alarm(alarm) {
  'use strict';

  var self = this;
  //console.log('alarm:', alarm);

  self.alarmId   = Number(alarm['_id']);
  self.uei   = alarm['uei'];
  self.severity   = alarm['_severity'];
  self.type   = Number(alarm['_type']);
  self.description   = alarm['description'];
  self.firstEventTime   = moment(alarm['firstEventTime']);
  self.lastEventTime   = moment(alarm['lastEventTime']);
  self.lastEvent   = new Event(alarm['lastEvent']);

  self.logMessage   = alarm['logMessage'];
  self.reductionKey   = alarm['reductionKey'];

  self.nodeid   = Number(alarm['nodeId']);
  self.nodeLabel   = alarm['nodeLabel'];

  self.parms   = {};
  // alarm['parms'];

  self.getSeverityClass = function() {
    if (this.severity !== null && angular.isString(this.severity) && this.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.severity);
    }
    return '';
  };

  self.className = 'Alarm';

}
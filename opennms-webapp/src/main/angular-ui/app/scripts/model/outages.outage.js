/* global moment: true */

function Outage(outage) {
  'use strict';

  var self = this;

  self.id = Number(outage['_id']);
  self.nodeId = Number(outage['nodeId']);
  self.ipAddress = outage['ipAddress'];
  self.ifLostService = moment(outage['_ifLostService']);
  if (outage['_ifRegainedService']) {
    self.ifRegainedService = moment(outage['_ifRegainedService']);
  }
  self.monitoredService = new MonitoredService(outage['monitoredService']);
  self.serviceLostEvent = new Event(outage['serviceLostEvent']);
  //self.node = new Node(self.nodeId);
  self.nodeLabel = self.serviceLostEvent.nodeLabel;

  self.getNodeName = function() {
    if (self.nodeLabel === undefined || self.nodeLabel === '') {
      return 'Node ' + self.nodeId;
    } else {
      return self.nodeLabel;
    }
  };

  self.getSeverityClass = function() {
    if (this.serviceLostEvent.severity !== null && angular.isString(this.serviceLostEvent.severity) && this.serviceLostEvent.severity.length != 0) {
      return 'severity-'+angular.uppercase(this.serviceLostEvent.severity);
    }
    return '';
  };

}
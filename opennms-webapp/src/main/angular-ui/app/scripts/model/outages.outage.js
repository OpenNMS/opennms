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

  self.getNodeName = function() {
    if (self.nodeLabel === undefined || self.nodeLabel === '') {
      return 'Node ' + self.nodeId;
    } else {
      return self.nodeLabel;
    }
  };
}
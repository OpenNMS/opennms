/* global moment: true */

function MonitoredService(svc) {
  'use strict';

  var self = this;

  self.id = svc['_id'];
  self.ipInterfaceId = svc['ipInterfaceId'];
  self.serviceId = svc['serviceType']['_id'];
  self.serviceName = svc['serviceType']['name'];
  self.status = svc['_status'];
}
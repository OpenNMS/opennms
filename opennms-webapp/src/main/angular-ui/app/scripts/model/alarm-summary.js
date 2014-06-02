/* global moment: true */

function AlarmSummary(data) {
  'use strict';

  var self = this;
  
  self.nodeId    = data['_node-id'];
  self.nodeLabel = data['_node-label'];
  self.severity  = data._severity;
  self.date      = data._date;
  self.count     = data._count;

  if (typeof self.count === 'string' || self.count instanceof String) {
    self.count = parseInt(self.count, 10);
  }
  
  /*
  if (typeof self.date === 'string' || self.date instanceof String) {
    self.date = moment(self.date);
  }
  */

  self.getNodeName = function() {
    if (self.nodeLabel === undefined || self.nodeLabel === '') {
      return 'Node ' + self.nodeId;
    } else {
      return self.nodeLabel;
    }
  };
}
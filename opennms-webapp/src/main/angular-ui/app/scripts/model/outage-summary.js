/* global moment: true */

function OutageSummary(data) {
	'use strict';

	var self = this;
	
	self.nodeId    = data['_node-id'];
	self.nodeLabel = data['_node-label'];
	self.down      = data['_time-down'];
	self.up        = data['_time-up'];
	self.now       = data['_time-now'];

	self.getNodeName = function() {
		if (self.nodeLabel === undefined || self.nodeLabel === '') {
			return 'Node ' + self.nodeId;
		} else {
			return self.nodeLabel;
		}
	};
}